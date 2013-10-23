package st.sparse.billy.experiments.wideBaseline

import st.sparse.billy._
import st.sparse.billy.experiments._
import java.io.File
import st.sparse.sundry._
import com.sksamuel.scrimage.Image
import scala.pickling._
import binary._
import st.sparse.billy.internal._

///////////////////////////////////////////////////////////

/**
 * Represents experiments on the Oxford image dataset.
 */
case class Oxford[D <% Detector, E <% Extractor[F], M <% Matcher[F], F](
  imageClass: String,
  otherImage: Int,
  override val detector: D,
  override val extractor: E,
  override val matcher: M) extends ExperimentImplementation[D, E, M, F] with Logging {
  def databaseRoot(implicit runtimeConfig: RuntimeConfig) =
    ExistingDirectory(new File(
      runtimeConfig.dataRoot,
      s"oxfordImages/$imageClass"))

  override def leftImage(implicit runtimeConfig: RuntimeConfig) =
    Image(ExistingFile(new File(
      databaseRoot,
      "images/img1.bmp")))

  override def rightImage(implicit runtimeConfig: RuntimeConfig) =
    Image(ExistingFile(new File(
      databaseRoot,
      s"images/img${otherImage}.bmp")))

  def groundTruthHomography(implicit runtimeConfig: RuntimeConfig) =
    Homography.fromFile(ExistingFile(new File(
      databaseRoot,
      s"homographies/H1to${otherImage}p")))

  override def correspondenceMap(implicit runtimeConfig: RuntimeConfig) =
    groundTruthHomography
    
  override def experimentParametersString = s"${imageClass}_$otherImage"
}
