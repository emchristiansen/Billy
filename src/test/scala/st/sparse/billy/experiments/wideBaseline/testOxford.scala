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
class TestOxford extends FunGeneratorSuite with st.sparse.billy.experiments.TestUtil {
  test("pickling", InstantTest) {
    val experiment = Oxford(
      "bikes",
      2,
      DoublyBoundedPairDetector(2, 10, 100, OpenCVDetector.FAST),
      OpenCVExtractor.SIFT,
      VectorMatcher.L1)
    val pickled = experiment.pickle
    val unpickled = pickled.unpickle[Oxford[DoublyBoundedPairDetector[OpenCVDetector.FAST.type], OpenCVExtractor.SIFT.type, VectorMatcher.L1.type, IndexedSeq[Double]]]
    assert(unpickled == experiment)
  }

  test("run FAST SIFT L1", MediumTest) {
    val experiment = Oxford(
      "boat",
      2,
      DoublyBoundedPairDetector(2, 10, 100, OpenCVDetector.FAST),
      OpenCVExtractor.SIFT,
      VectorMatcher.L1)

    val results = experiment.run
    assert(results.distances.rows == results.distances.cols)
    results.distances.foreachValue(distance => assert(distance >= 0))

    assert(results == results.pickle.unpickle[Results])
  }

  test("matching an image to itself should produce perfect performance", MediumTest) {
    val experiment = Oxford(
      "boat",
      1,
      DoublyBoundedPairDetector(2, 10, 100, OpenCVDetector.FAST),
      OpenCVExtractor.SIFT,
      VectorMatcher.L1)

    val results = experiment.run
    for (index <- 0 until results.distances.rows) {
      assert(results.distances(index, index) == 0)
    }
  }
}