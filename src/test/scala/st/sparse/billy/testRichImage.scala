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

////////////////////////////////////////////////////////////////////////////////

@RunWith(classOf[JUnitRunner])
class TestRichImage extends FunGeneratorSuite with st.sparse.billy.MatlabTestUtil {
  test("edge preserving smoothing", MediumTest) {
    val smallDisp = moebiusDisp1.scale(0.25)
    val smallView = moebiusView1.scale(0.25)

    val boundaries = MatlabGPbSegmenter.boundariesImageScaling(smallDisp)
    val segmentation = Segmentation.fromBoundariesImage(boundaries)

    val smoothed = RichImage.edgePreservingSmoothing(
      5,
      segmentation)(smallView)

    logImage("smallDisp", smallDisp)
    logImage("smallView", smallView)
    logImage("smoothed", smoothed)
  }
}