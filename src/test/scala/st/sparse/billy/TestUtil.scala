package st.sparse.billy

import java.io.File

import st.sparse.sundry._

import com.sksamuel.scrimage._

trait TestUtil {
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
}