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
import breeze.linalg._
import scala.reflect.ClassTag

////////////////////////////////////////////////////////////////////////////////

@RunWith(classOf[JUnitRunner])
class TestSummary extends FunGeneratorSuite with st.sparse.billy.experiments.TestUtil {
  test("matching an image to itself should yield a perfect recognition rate", MediumTest) {
    val experiment = OxfordExperiment(
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