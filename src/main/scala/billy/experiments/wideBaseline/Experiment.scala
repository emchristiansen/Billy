package billy.experiments.wideBaseline

import billy._
import billy.experiments._

import java.io.File

import st.sparse.sundry._

import com.sksamuel.scrimage.Image

import scala.pickling._
import binary._

///////////////////////////////////////////////////////////

/**
 * Represents experiments on the Oxford image dataset.
 */
case class Experiment[D <% Detector, E <% Extractor[F], M <% Matcher[F], F](
  imageClass: String,
  otherImage: Int,
  detector: D,
  extractor: E,
  matcher: M) extends Logging {
  def groundTruthHomography(implicit runtimeConfig: RuntimeConfig) =
    Homography.fromFile(ExistingFile(new File(
      runtimeConfig.dataRoot,
      s"oxfordImages/${imageClass}/homographies/H1to${otherImage}p")))

  def leftImage(implicit runtimeConfig: RuntimeConfig) =
    Image(ExistingFile(new File(
      runtimeConfig.dataRoot,
      s"oxfordImages/${imageClass}/images/img1.bmp")))

  def rightImage(implicit runtimeConfig: RuntimeConfig) =
    Image(ExistingFile(new File(
      runtimeConfig.dataRoot,
      s"oxfordImages/${imageClass}/images/img${otherImage}.bmp")))

  def run(implicit runtimeConfig: RuntimeConfig): Results = {
    logger.info(s"Running ${this}")

    val pairDetector = PairDetector(2, detector)
    val (leftKeyPoints, rightKeyPoints) = pairDetector.detectPair(
      groundTruthHomography,
      leftImage,
      rightImage) unzip

    logger.info(s"Number of KeyPoints: ${leftKeyPoints.size}")

    val (leftDescriptors, rightDescriptors) = {
      val leftDescriptors = extractor.extract(leftImage, leftKeyPoints)
      val rightDescriptors = extractor.extract(rightImage, rightKeyPoints)

      for (
        (Some(left), Some(right)) <- leftDescriptors.zip(rightDescriptors)
      ) yield (left, right)
    } unzip

    logger.info(s"Number of surviving KeyPoints: ${leftDescriptors.size}")

    val distances = matcher.matchAll(
      leftDescriptors,
      rightDescriptors)

    Results(distances)
  }
}
