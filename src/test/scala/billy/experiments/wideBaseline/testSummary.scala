package billy.experiments.wideBaseline

import billy._
import billy.experiments._
import billy.detectors._
import billy.extractors._
import billy.matchers._

import org.scalatest.fixture
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalacheck.Gen
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.pickling._
import scala.pickling.binary._

import st.sparse.sundry._
import breeze.linalg._
import scala.reflect.ClassTag

////////////////////////////////////////////////////////////////////////////////

@RunWith(classOf[JUnitRunner])
class TestSummary extends FunGeneratorSuite with billy.experiments.TestUtil {
  test("matching an image to itself should yield a perfect recognition rate", MediumTest) {
    val experiment = Experiment(
      "boat",
      1,
      BoundedDetector(OpenCVDetector.BRISK, 20),
      PatchExtractor(Gray, 4, 4),
      VectorMatcher.L1)

    val results = experiment.run
    
    SummaryUtil.precisionRecall(results)
    assert(SummaryUtil.recognitionRate(results) == 1)
  }
}