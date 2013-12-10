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
class TestBlurredMiddlebury extends FunGeneratorSuite with st.sparse.billy.experiments.MatlabTestUtil with Logging {
  test("smooth is memoized", MediumTest) {
    val experiment = BlurredMiddlebury(
      2.002,
      0,
      0.5,
      Middlebury(
        2005,
        "Moebius",
        10,
        DoublyBoundedPairDetector(2, 10, 100, OpenCVDetector.FAST),
        OpenCVExtractor.SIFT,
        VectorMatcher.L1))

    val (leftImage0, time0) = time(experiment.leftImage)
    val (leftImage1, time1) = time(experiment.leftImage)

    logger.info(s"time0: $time0")
    logger.info(s"time1: $time1")
    assert(leftImage0 == leftImage1)

    assert(time1 / 1000000 < 10000)

    val pickled = leftImage0.pickle
    val unpickled = pickled.unpickle[Image]
    assert(leftImage0 == unpickled)
  }

  test("run cached", MediumTest) {
    val experiment = BlurredMiddlebury(
      2.002,
      1,
      0.5,
      Middlebury(
        2005,
        "Moebius",
        10,
        DoublyBoundedPairDetector(2, 10, 100, OpenCVDetector.FAST),
        OpenCVExtractor.BRIEF,
        VectorMatcher.L0))

    checkPickle(experiment)

    val cached = Experiment.cached(experiment)
    val (results0, time0) = time(cached.run)
    val (results1, time1) = time(cached.run)

    logger.info(s"time0: $time0")
    logger.info(s"time1: $time1")
    assert(results0 == results1)
  }

  test("run cached shape", SlowTest) {
    val detector = DoublyBoundedPairDetector(2, 10, 100, OpenCVDetector.FAST)
    val extractor = AndExtractor(
      PatchExtractor(Gray, 24, 1),
      ForegroundMaskExtractor(24))
    val matcher = PixelSMatcher(0, 0, 0, 1)
    val middlebury = Middlebury(
      2005,
      "Moebius",
      10,
      detector,
      extractor,
      matcher)

    val experiment = BlurredMiddlebury(
      2.002,
      1,
      0.5,
      middlebury)

//    checkPickle(detector)
//    checkPickle(extractor)
//    checkPickle(matcher)
//    checkPickle(middlebury)
//    checkJson(experiment)
    
    checkJson(detector)
    checkJson(extractor)
    checkJson(matcher)
    checkJson(middlebury)
    checkJson(experiment)

    val cached = Experiment.cached(experiment)
    val (results0, time0) = time(cached.run)
    val (results1, time1) = time(cached.run)

    logger.info(s"time0: $time0")
    logger.info(s"time1: $time1")
    assert(results0 == results1)
  }
}
