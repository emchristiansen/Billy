package st.sparse.billy.experiments.wideBaseline

import st.sparse.billy._
import st.sparse.billy.experiments._
import java.io.File
import st.sparse.sundry._
import com.sksamuel.scrimage._
import scala.pickling._
import binary._
import st.sparse.sundry._
import breeze.linalg._
import org.opencv.core.KeyPoint
import thirdparty.jhlabs.image.PixelUtils

///////////////////////////////////////////////////////////

/**
 * Represents experiments on the Oxford image dataset.
 */
case class BlurredMiddlebury[D <% PairDetector, E <% Extractor[F], M <% Matcher[F], F](
  similarityThreshold: Double,
  numSmoothingIterations: Int,
  middlebury: Middlebury[D, E, M, F]) extends ExperimentImplementation[D, E, M, F] with Logging {
  override val maxPairedDescriptors = middlebury.maxPairedDescriptors
  override val detector = middlebury.detector
  override val extractor = middlebury.extractor
  override val matcher = middlebury.matcher

  def smooth: (Image, Image) => Image = (image, disparity) => {
    val smoothed = Stream.iterate(image) { image =>
      image.anisotropicDiffusion(
        similarityThreshold,
        disparity)
    }

    smoothed(numSmoothingIterations)
  }

  override def leftImage(implicit runtimeConfig: RuntimeConfig) = {
    val disparity = Image(ExistingFile(new File(
      middlebury.databaseRoot,
      "disp1.png")))
    smooth(middlebury.leftImage, disparity)
  }
  override def rightImage(implicit runtimeConfig: RuntimeConfig) = {
    val disparity = Image(ExistingFile(new File(
      middlebury.databaseRoot,
      "disp5.png")))
    smooth(middlebury.rightImage, disparity)
  }
  override def correspondenceMap(implicit runtimeConfig: RuntimeConfig) = 
    middlebury.correspondenceMap
  override def experimentParametersString = 
    s"${similarityThreshold}_${numSmoothingIterations}_${middlebury.experimentParametersString}"
}

object BlurredMiddlebury