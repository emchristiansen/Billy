package st.sparse.billy

import java.io.File
import st.sparse.sundry._
import com.sksamuel.scrimage._
import st.sparse.billy.experiments.wideBaseline.Homography
import scala.slick.session.Database
import st.sparse.billy.experiments._
import java.nio.file.Files
import st.sparse.sundry._

trait TestUtil extends Logging {
  lazy val configureLogger = {
    // Must be one of: "trace", "debug", "info", "warn", or "error".
    System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug")
  }

  loadOpenCV
  configureLogger

  val random = new util.Random(0)

  val resourceRoot = ExistingDirectory(
    new File(getClass.getResource("/").getPath))

  val goldfishGirl = Image(ExistingFile(new File(
    resourceRoot,
    "/goldfish_girl.png")))

  val flowerpots = Image(ExistingFile(new File(
    resourceRoot,
    "/flowerpots.png")))

  val moebiusView1 = Image(ExistingFile(new File(
    resourceRoot,
    "/data/middleburyStereo/2005/Moebius/view1.png")))

  val moebiusDisp1 = Image(ExistingFile(new File(
    resourceRoot,
    "/data/middleburyStereo/2005/Moebius/disp1.png")))
    
  val moebiusBoundaries1 = Image(ExistingFile(new File(
    resourceRoot,
    "/moebiusBoundaries1.png")))    

  val moebiusView5 = Image(ExistingFile(new File(
    resourceRoot,
    "/data/middleburyStereo/2005/Moebius/view5.png")))
    
  val moebiusDisp5 = Image(ExistingFile(new File(
    resourceRoot,
    "/data/middleburyStereo/2005/Moebius/disp5.png")))

  val palmTree = Image(ExistingFile(new File(
    resourceRoot,
    "/palmTree.jpg")))

  val boat1 = Image(ExistingFile(new File(
    resourceRoot,
    "/data/oxfordImages/boat/images/img1.bmp")))

  val boat2 = Image(ExistingFile(new File(
    resourceRoot,
    "/data/oxfordImages/boat/images/img2.bmp")))

  val boat12Homography = Homography.fromFile(ExistingFile(new File(
    resourceRoot,
    "/data/oxfordImages/boat/homographies/H1to2p")))

  val outputRoot =
    ExistingDirectory(Files.createTempDirectory("TestBillyOutputRoot").toFile)

  implicit val logRoot = {
    val directory = new File(outputRoot, "log")
    if (!directory.isDirectory) directory.mkdir()
    LogRoot(ExistingDirectory(directory))
  }
}