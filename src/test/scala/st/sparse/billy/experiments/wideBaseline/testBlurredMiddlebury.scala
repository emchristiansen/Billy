package st.sparse.billy.experiments.wideBaseline

import st.sparse.billy._
import st.sparse.billy.experiments._
import st.sparse.billy.detectors._
import st.sparse.billy.extractors._
import st.sparse.billy.matchers._
import org.scalatest.fixture
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalacheck.Gen
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import scala.pickling._
import scala.pickling.binary._
import st.sparse.sundry._
import breeze.linalg.DenseMatrix
import scala.reflect.ClassTag
import com.sksamuel.scrimage._
import org.opencv.core.KeyPoint
import java.io.File

////////////////////////////////////////////////////////////////////////////////

@RunWith(classOf[JUnitRunner])
class TestBlurredMiddlebury extends FunGeneratorSuite with st.sparse.billy.experiments.TestUtil with Logging {
  test("smooth is memoized", MediumTest) {
    val experiment = BlurredMiddlebury(
      2.002,
      0,
      Middlebury(
        2005,
        "Moebius",
        10,
        DoublyBoundedPairDetector(2, 10, 100, OpenCVDetector.FAST),
        OpenCVExtractor.SIFT,
        VectorMatcher.L1))

    val (leftImage0, time0) = time(experiment.leftImage)
    val (leftImage1, time1) = time(experiment.leftImage)

    assert(leftImage0 == leftImage1)
    logger.debug((time0 / 1000000).toString)
    logger.debug((time1 / 1000000).toString)

    assert(time1 / 1000000 < 10000)

    val pickled = leftImage0.pickle
    val unpickled = pickled.unpickle[Image]
    assert(leftImage0 == unpickled)
  }

  ignore("run", SlowTest) {
    val experiment = BlurredMiddlebury(
      2.002,
      0,
      Middlebury(
        2005,
        "Moebius",
        10,
        DoublyBoundedPairDetector(2, 10, 100, OpenCVDetector.FAST),
        OpenCVExtractor.BRIEF,
        VectorMatcher.L0))

    experiment.run
  }
}
