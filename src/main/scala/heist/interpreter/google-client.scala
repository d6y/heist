package heist
package interpreter

import scala.collection.JavaConversions._

import com.google.api.client.auth.oauth2.{TokenResponse, Credential}
import com.google.gdata.client.spreadsheet._
import com.google.gdata.data.spreadsheet._
import com.google.gdata.util._

case class GoogleClientInterpreter(credentials: Credential) {

  import spreadsheet._
  import cats.{Id, ~>}

  lazy val sheetService = {
    val ss = new SpreadsheetService("Wibble")
    ss.setOAuth2Credentials(credentials)
    ss
  }

  def run[T](prog: FreeSheetOp[T]): T =
    prog.foldMap(impureCompiler)

  def impureCompiler = new (SheetOp ~> Id) {
    def apply[A](op: SheetOp[A]): Id[A] = op match {

      case OpenSheet(name, withSheet) =>
        val url = new java.net.URL("https://spreadsheets.google.com/feeds/spreadsheets/private/full")
        val query = new SpreadsheetQuery(url)
        query.setTitleQuery(name)
        val feed = sheetService.query(query, classOf[SpreadsheetFeed])
        val entry: SpreadsheetEntry = feed.getEntries.head // Invalid assumption of 1 result
        withSheet(Sheet(entry))

      case OpenWorksheet(sheet, index, withWork) =>
        val worksheet =
          sheetService.getFeed(sheet.entry.getWorksheetFeedUrl(), classOf[WorksheetFeed])
            .getEntries()
            .get(index)
        val listFeedUrl = worksheet.getListFeedUrl()
        val listFeed = sheetService.getFeed(listFeedUrl, classOf[ListFeed])
        withWork(Worksheet(listFeed))

      case Rows(worksheet: Worksheet, withRows) =>
        withRows(worksheet.feed.getEntries.map(SingleRow.apply))

      case Cells(rows: Seq[SingleRow], withRow) =>
        val values = for {
          row <- rows
          tag <- row.row.getCustomElements.getTags.iterator.toSeq
        } yield row.row.getCustomElements.getValue(tag)
        withRow(values)
    }
  }

}