package heist

import com.google.api.client.googleapis.auth.oauth2.{GoogleAuthorizationCodeFlow, GoogleCredential}
import com.google.api.client.auth.oauth2.{TokenResponse, Credential}
import com.google.api.client.http.HttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.{ Drive, DriveScopes }
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets

import scala.collection.JavaConversions._

import java.nio.file.{ FileSystems, Files, Path }
import java.nio.charset.StandardCharsets.UTF_8
import java.io.{ IOException, FileReader }

import cats._, cats.data.Xor, cats.std.all._

object Auth {

  val scopes = List(DriveScopes.DRIVE, DocScopes.SHEETS)

  object DocScopes {
    val SHEETS = "https://spreadsheets.google.com/feeds"
  }

  def simple: Credential = {
    import Disk._
    token.fold(_ => (requestTokens _ andThen saveTokens)(secrets), tokensAsCredentials(secrets))
  }

  def requestTokens(secrets: GoogleClientSecrets) = {

    val flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, jsonFactory, secrets, scopes)
      .setAccessType("offline")
      .setApprovalPrompt("force")
      .build()

    val url = flow.newAuthorizationUrl().setRedirectUri(secrets.redirectUri).build()
    print(s"\nVisit this URL and enter the code you are given:\n\t$url\n\nEnter Code: ")
    val code = io.StdIn.readLine

    val tokenResponse = flow.newTokenRequest(code).setRedirectUri(secrets.redirectUri).execute()

    val credential = new GoogleCredential.Builder()
      .setTransport(httpTransport)
      .setJsonFactory(jsonFactory)
      .setClientSecrets(secrets)
      .build()
      .setFromTokenResponse(tokenResponse)

    credential
  }

  def tokensAsCredentials(secrets: GoogleClientSecrets)(tokenResponse: TokenResponse) =
    new GoogleCredential.Builder()
      .setTransport(httpTransport)
      .setJsonFactory(jsonFactory)
      .setClientSecrets(secrets)
      .build()
      .setFromTokenResponse(tokenResponse)

  implicit class SecretsOps(secrets: GoogleClientSecrets) {
    def redirectUri = secrets.getDetails().getRedirectUris().head
  }

  private object Disk {
    val fs = FileSystems.getDefault
    val clientJsonFile = fs.getPath("private/client_secret.json")
    val accessFile     = fs.getPath("private/access")
    val refreshFile    = fs.getPath("private/refresh")

    lazy val secrets = GoogleClientSecrets.load(jsonFactory, new FileReader(clientJsonFile.toFile))

    def token: Throwable Xor TokenResponse =
      for {
        access  <- read(accessFile)
        refresh <- read(refreshFile)
      } yield new TokenResponse().setAccessToken(access).setRefreshToken(refresh)

    private def read(path: Path): Throwable Xor String = Xor.fromTryCatch[IOException] {
      Files.readAllLines(path, UTF_8).mkString
    }

    def saveTokens(c: Credential): Credential = {
      val aPath = Files.write(accessFile,  Seq(c.getAccessToken), UTF_8)
      val rPath = Files.write(refreshFile, Seq(c.getRefreshToken), UTF_8)
      c
    }
  }

  lazy val httpTransport = new NetHttpTransport()
  lazy val jsonFactory = new JacksonFactory()
}