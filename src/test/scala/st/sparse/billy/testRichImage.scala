package st.sparse.billy

import org.scalatest.fixture
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalacheck.Gen
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import breeze.linalg._
import st.sparse.sundry.FunGeneratorSuite
import st.sparse.sundry._
import com.sksamuel.scrimage.Image
import com.sksamuel.scrimage.PixelTools

////////////////////////////////////////////////////////////////////////////////

@RunWith(classOf[JUnitRunner])
class TestRichImage extends FunGeneratorSuite with st.sparse.billy.MatlabTestUtil {
  ignore("edge preserving smoothing", MediumTest, MatlabTest, InteractiveTest) {
    val scaleFactor = 0.25

    val smallDisp = moebiusDisp1.scale(scaleFactor)
    val smallView = moebiusView1.scale(scaleFactor)
    val smallBoundaries = moebiusBoundaries1.scale(scaleFactor)

    //    logger.info("Getting boundaries")
    //    val boundaries = MatlabGPbSegmenter.boundariesImageScaling(smallDisp)
    logger.info("Getting segmentation")
    val segmentation = Segmentation.fromBoundariesImage(smallBoundaries)

    def smooth(image: Image) = {
      logger.info("Doing edge preserving smoothing")
      RichImage.edgePreservingSmoothing(
        2,
        segmentation)(image)
    }

    val stream = Stream.iterate(smallView)(smooth)
    val smoothed = stream(20)

    logImage("smallDisp", smallDisp)
    logImage("smallView", smallView)
    logImage("smoothed", smoothed)
  }

  test("mapStencil", MediumTest, InteractiveTest) {
    val blurred = goldfishGirl.mapStencil(1) {
      case ((x, y), _, patch) =>
        val sum = patch.argb.map(DenseVector.apply).reduce(_ + _)
        val mean = sum.map(_.toDouble) / patch.argb.size.toDouble
        val Array(alpha, red, green, blue) = mean.map(_.round.toInt).data
        PixelTools.argb(alpha, red, green, blue)
    }

    logImage("orig", goldfishGirl)
    logImage("blurred", blurred)
  }

  test("anisotropic diffusion", MediumTest, InteractiveTest) {
    val scaleFactor = 0.25

    val smallView = moebiusView1.scale(scaleFactor)

    logger.info("Inpainting")
    val dispInpainted = moebiusDisp1.inpaintBlackPixels.scale(scaleFactor)

    def smooth(image: Image) = {
      logger.info("Doing anisotropic diffusion")
      image.anisotropicDiffusion(
        2.002,
        dispInpainted)
    }

    logImage("dispInpainted", dispInpainted)
    logImage("view", smallView)

    val stream = Stream.iterate(smallView)(smooth)
    for ((smoothed, index) <- stream.take(10).zipWithIndex) {
      logImage(s"smoothed_$index", smoothed)
    }
  }
}