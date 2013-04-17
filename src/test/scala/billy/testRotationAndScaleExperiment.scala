package billy

import nebula._
import nebula.imageProcessing._
import nebula.util._

import billy._
import billy.brown._
import billy.mpie._
import billy.smallBaseline._
import billy.wideBaseline._
import billy.summary._

import billy.detectors._
import billy.extractors._
import billy.matchers._

import org.scalatest._
import org.opencv.features2d._
import javax.imageio.ImageIO
import java.io.File
import org.opencv.core.MatOfKeyPoint
import org.opencv.features2d.{ FeatureDetector, KeyPoint }
import nebula._
import org.opencv.core.Mat
import java.awt.Color
import java.awt.image.BufferedImage
import org.apache.commons.math3.linear.Array2DRowRealMatrix
import nebula.util.Homography
import nebula.util.OpenCVUtil
import nebula.util.KeyPointUtil
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.ConfigMapWrapperSuite

///////////////////////////////////////////////////////////

@RunWith(classOf[JUnitRunner])
@WrapWith(classOf[ConfigMapWrapperSuite])
class TestRotationAndScaleExperiment(
  val configMap: Map[String, Any]) extends ConfigMapFunSuite {
  val imagePath = getResource("/iSpy.png")

  test("on sample image", FastTest) {
    loadOpenCV

    implicit val runtimeConfig = RuntimeConfig(
      homeDirectory + "data",
      homeDirectory + "Dropbox/t/2013_q1/LUCID",
      None,
      false,
      true)

    val detector = BoundedPairDetector(
      BoundedDetector(OpenCVDetector.SIFT, 5000),
      100)
    val extractor = OpenCVExtractor.SIFT
    val matcher = VectorMatcher.L2

    for (
      scaleFactor <- List(0.5);
      angle <- List(math.Pi / 8)
    ) {
      def rotationExperiment = RotationAndScaleExperiment(
        imagePath,
        detector,
        extractor,
        matcher,
        scaleFactor,
        angle)

      val summary: ExperimentSummary = rotationExperiment.run
      asserty(summary.summaryNumbers.size == 1)
      val recognitionRate = summary.summaryNumbers("recognitionRate")
      println(scaleFactor)
      println(angle)
      println(recognitionRate)
    }
  }
}