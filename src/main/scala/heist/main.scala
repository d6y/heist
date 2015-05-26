package heist

object Main extends App {

  import com.google.api.services.drive.model.FileList
  import com.google.api.services.drive.{ Drive, DriveScopes }
  import scala.collection.JavaConversions._
  import com.google.api.client.auth.oauth2.{TokenResponse, Credential}

  // Spreadsheet access...
  import com.google.gdata.client.spreadsheet._
  import com.google.gdata.data.spreadsheet._
  import com.google.gdata.util._

  val credentials = Auth.simple

  val ss = new SpreadsheetService("Wibble")
  ss.setOAuth2Credentials(credentials)

  val url = new java.net.URL("https://spreadsheets.google.com/feeds/spreadsheets/private/full")

  val query = new SpreadsheetQuery(url)
  query.setTitleQuery("UK Jan Rainfall")

  val feed = ss.query(query, classOf[SpreadsheetFeed])

  def dump(sheet: SpreadsheetEntry) = {
    println(sheet.getTitle.getPlainText)

    val firstWorksheet =
      ss.getFeed(sheet.getWorksheetFeedUrl(), classOf[WorksheetFeed])
      .getEntries()
      .get(0)

    val listFeedUrl = firstWorksheet.getListFeedUrl()
    val listFeed = ss.getFeed(listFeedUrl, classOf[ListFeed])

    listFeed.getEntries.foreach { row: ListEntry =>
      row.getCustomElements.getTags.foreach { tag: String =>
        print(row.getCustomElements.getValue(tag) + "\t")
      }
      println("")
    }

      /*
    val cellFeedUrl = new java.net.URI(firstWorksheet.getCellFeedUrl.toString).toURL()
    val cellFeed = ss.getFeed(cellFeedUrl, classOf[CellFeed])
    cellFeed.getEntries.foreach { cell: CellEntry =>
      println(
        cell.getTitle().getPlainText() + "\t" +
        cell.getCell().getInputValue()
        )
      }
      */


  }


  val sheets: Seq[SpreadsheetEntry] = feed.getEntries
  sheets.foreach(dump)


  // Drive access ...
  /* def client = new Drive.Builder(httpTransport, jsonFactory, credentials)
    .setApplicationName("wibble")
    .build()

  println("Listing files...")
  val request = client.files().list()
  val files: FileList = request.execute()
  files.getItems().head.foreach(println)
 */
}