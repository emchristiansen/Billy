package st.sparse.billy.experiments.wideBaseline

import st.sparse.billy._
import st.sparse.billy.experiments._
import java.io.File
import st.sparse.sundry._
import com.sksamuel.scrimage._
import scala.pickling._
import binary._
import st.sparse.billy.internal._
import breeze.linalg._
import org.opencv.core.KeyPoint
import thirdparty.jhlabs.image.PixelUtils

///////////////////////////////////////////////////////////

/**
 * Represents experiments on the Oxford image dataset.
 */
case class Middlebury[D <% PairDetector, E <% Extractor[F], M <% Matcher[F], F](
  databaseYear: Int,
  imageClass: String,
  override val detector: D,
  override val extractor: E,
  override val matcher: M) extends ExperimentImplementation[D, E, M, F] with Logging {
  // For now we only support one database year.
  require(databaseYear == 2006)

  def databaseRoot(implicit runtimeConfig: RuntimeConfig) =
    ExistingDirectory(new File(
      runtimeConfig.dataRoot,
      s"middleburyStereo/$databaseYear/$imageClass"))

  override def leftImage(implicit runtimeConfig: RuntimeConfig) =
    Image(ExistingFile(new File(databaseRoot, "view1.png")))

  override def rightImage(implicit runtimeConfig: RuntimeConfig) =
    Image(ExistingFile(new File(databaseRoot, "view5.png")))

  def stereoDisparity(implicit runtimeConfig: RuntimeConfig) =
    StereoDisparity.fromImage(Image(ExistingFile(new File(
      databaseRoot,
      "disp1.png"))))

  override def correspondenceMap(implicit runtimeConfig: RuntimeConfig) =
    stereoDisparity
    
  override def experimentParametersString = s"${databaseYear}_$imageClass"
}
