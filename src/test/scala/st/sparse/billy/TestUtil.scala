package st.sparse.billy

import java.io.File
import st.sparse.sundry._
import com.sksamuel.scrimage._
import st.sparse.billy.experiments.wideBaseline.Homography
import com.typesafe.scalalogging.slf4j.Logging
import scala.slick.session.Database
import st.sparse.billy.experiments._

trait TestUtil extends RichLogging {
  lazy val configureLogger = {
    // Must be one of: "trace", "debug", "info", "warn", or "error".
    System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "info")
  }

  loadOpenCV
  configureLogger

  val resourceRoot = ExistingDirectory(
    new File(getClass.getResource("/").getPath))

  val goldfishGirl = Image(ExistingFile(new File(
    resourceRoot,
    "/goldfish_girl.png")))

  val boat1 = Image(ExistingFile(new File(
    resourceRoot,
    "/data/oxfordImages/boat/images/img1.bmp")))

  val boat2 = Image(ExistingFile(new File(
    resourceRoot,
    "/data/oxfordImages/boat/images/img2.bmp")))

  val boat12Homography = Homography.fromFile(ExistingFile(new File(
    resourceRoot,
    "/data/oxfordImages/boat/homographies/H1to2p")))

  // TODO: Hack alert: These should be moved down the hierarchy.
  val database: Database = {
    val tempFile = File.createTempFile("TestBilly", "sqlite")
    tempFile.deleteOnExit
    Database.forURL(s"jdbc:sqlite:$tempFile", driver = "org.sqlite.JDBC")
  }

  implicit val runtimeConfig = RuntimeConfig(
    ExistingDirectory(new File(resourceRoot, "/data")),
    database,
    ExistingDirectory(Files.createTempDirectory("TestBillyOutputRoot").toFile),
    None,
    false,
    true)
}