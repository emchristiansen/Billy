package st.sparse.billy

import org.scalatest.fixture
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalacheck.Gen
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import breeze.linalg.DenseMatrix
import st.sparse.sundry.FunGeneratorSuite
import st.sparse.sundry._
import com.sksamuel.scrimage.Image

////////////////////////////////////////////////////////////////////////////////

@RunWith(classOf[JUnitRunner])
class TestRichImage extends FunGeneratorSuite with st.sparse.billy.MatlabTestUtil {
  test("edge preserving smoothing", MediumTest, MatlabTest) {
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
}