package heist

import cats._, cats.Functor
import cats.free.Free, cats.free.Free.liftF

object spreadsheet {

import com.google.gdata.client.spreadsheet._
import com.google.gdata.data.spreadsheet._
import com.google.gdata.util._


case class Sheet(entry: SpreadsheetEntry)
case class Worksheet(feed: ListFeed)
case class SingleRow(row: ListEntry)
case class Cell(value: String)

sealed trait SheetOp[+Next]
case class OpenSheet[Next](name: String, next: Sheet => Next) extends SheetOp[Next]
case class OpenWorksheet[Next](sheet: Sheet, index: Int, next: Worksheet => Next) extends SheetOp[Next]
case class Rows[Next](worksheet: Worksheet, next: Seq[SingleRow] => Next) extends SheetOp[Next]
case class Cells[Next](rows: Seq[SingleRow], next: Seq[String] => Next) extends SheetOp[Next]

type FreeSheetOp[A] = Free[SheetOp, A]

implicit val functor: Functor[SheetOp] = new Functor[SheetOp] {
  def map[A, B](op: SheetOp[A])(f: A => B): SheetOp[B] = op match {
    case OpenSheet(name, withSheet)    => OpenSheet(name, withSheet andThen f)
    case OpenWorksheet(s, i, withWork) => OpenWorksheet(s, i, withWork andThen f)
    case Rows(rows, next)              => Rows(rows, next andThen f)
    case Cells(row, next)               => Cells(row, next andThen f)
  }
}

def openSheet(name: String): FreeSheetOp[Sheet] =
  liftF(OpenSheet(name, identity))

def openWorksheet(sheet: Sheet, index: Int): FreeSheetOp[Worksheet] =
  liftF(OpenWorksheet(sheet, index, identity))

def rows(worksheet: Worksheet): FreeSheetOp[Seq[SingleRow]] =
  liftF(Rows(worksheet, identity))

def cells(row: Seq[SingleRow]): FreeSheetOp[Seq[String]] =
  liftF(Cells(row, identity))

}