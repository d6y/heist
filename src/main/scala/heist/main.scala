package heist

object Main extends App {

  val credentials = Auth.simple

  import spreadsheet._

  val prog = for {
    sheet <- openSheet("UK Jan Rainfall")
    work  <- openWorksheet(sheet, 0)
    rows  <- rows(work)
    xs    <- cells(rows)
  } yield xs

  val interp = new interpreter.GoogleClientInterpreter(credentials)

  val result = interp.run(prog)
  println(result.toList)

}