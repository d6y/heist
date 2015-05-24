package heist

import com.google.api.client.googleapis.auth.oauth2
import oauth2.{GoogleAuthorizationCodeFlow, GoogleCredential, GoogleTokenResponse}
import com.google.api.client.http.HttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.{ Drive, DriveScopes }
import com.google.api.services.drive.model.FileList

import java.io.{InputStreamReader, BufferedReader}

import scala.collection.JavaConversions._

import scalaz._, Scalaz._

object Secrets {

  import argonaut._, Argonaut._

  case class ClientKeys(clientID: String, clientSecret: String, tokenURI: String)
  case class Token(value: String)

  type ErrMsg = String
  type FileContent = String

  private lazy val secretsPath =
    "private/client_secret_742641857475-2lhpir55gsmki51qi9s5l1jauhj16n6p.apps.googleusercontent.com.json"

  private lazy val tokenPath =
      "private/token"

  lazy val keys: ErrMsg \/ ClientKeys = for {
      text <- readFile(secretsPath) leftMap {_.getMessage}
      keys <- Parse.decodeEither[ClientKeys](text)
    } yield keys

  lazy val token: ErrMsg \/ Token = for {
    text <- readFile(tokenPath) leftMap { _.getMessage }
  } yield Token(text)

  def promptForToken(url: String): Token = {
   println("Please open the following URL in your browser then type the authorization code:")
   println(s"  $url")
   val br = new BufferedReader(new InputStreamReader(System.in))
   val token = br.readLine()
   Token(token)
 }

  private val readFile: String => Throwable \/ FileContent =
    name => \/.fromTryCatchNonFatal {
      scala.io.Source.fromFile(new java.io.File(name)).mkString
    }

  implicit def ClientKeysDecodeJson: DecodeJson[ClientKeys] =
    DecodeJson(c => for {
      secret <- (c --\ "installed" --\ "client_secret").as[String]
      uri    <- (c --\ "installed" --\ "token_uri").as[String]
      id     <- (c --\ "installed" --\ "client_id").as[String]
  } yield ClientKeys(id, secret, uri) )
}

object Main extends App {

  val CLIENT_ID = "742641857475-2lhpir55gsmki51qi9s5l1jauhj16n6p.apps.googleusercontent.com"
  val CLIENT_SECRET = "aN6K1kd_ibTI1P-bjEMKsI84"
  val REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob"

  val httpTransport = new NetHttpTransport()
  val jsonFactory = new JacksonFactory()

  val flow = new GoogleAuthorizationCodeFlow.Builder(
      httpTransport, jsonFactory, CLIENT_ID, CLIENT_SECRET, List(DriveScopes.DRIVE))
      .setAccessType("online")
      .setApprovalPrompt("auto").build()

  println(Secrets.keys)
  println(Secrets.token)


  val token = Secrets.token getOrElse Secrets.promptForToken(flow.newAuthorizationUrl().setRedirectUri(REDIRECT_URI).build())

  val response = flow.newTokenRequest(token.value).setRedirectUri(REDIRECT_URI).execute()
  val credential = new GoogleCredential().setFromTokenResponse(response)

  val client = new Drive.Builder(httpTransport, jsonFactory, credential).build()

  // https://developers.google.com/drive/v2/reference/files/list#examples
  println("Listing files...")
  val request = client.files().list()
  val files: FileList = request.execute()
  files.getItems().foreach(println)


}