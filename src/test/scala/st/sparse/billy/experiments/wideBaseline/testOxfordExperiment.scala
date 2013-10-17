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
class TestOxfordExperiment extends FunGeneratorSuite with st.sparse.billy.experiments.TestUtil {
  test("pickling FAST SIFT L0", InstantTest) {
    val experiment = OxfordExperiment(
      "boat",
      4,
      OpenCVDetector.FAST,
      OpenCVExtractor.SIFT,
      VectorMatcher.L0)
    val pickle = experiment.pickle
    val unpickled = pickle.unpickle[OxfordExperiment[OpenCVDetector.FAST.type, OpenCVExtractor.SIFT.type, VectorMatcher.L0.type, IndexedSeq[Double]]]

    assert(experiment == unpickled)
  }

  test("pickling SIFT PatchExtractor L0", InstantTest) {
    val experiment = OxfordExperiment(
      "boat",
      4,
      OpenCVDetector.SIFT,
      PatchExtractor(Gray, 2, 3),
      VectorMatcher.L0)
    val pickle = experiment.pickle
    val unpickled = pickle.unpickle[OxfordExperiment[OpenCVDetector.SIFT.type, PatchExtractor, VectorMatcher.L0.type, DenseMatrix[IndexedSeq[Int]]]]

    assert(experiment == unpickled)
  }

  test("pickling SIFT PatchExtractor L1", InstantTest) {
    val experiment = OxfordExperiment(
      "boat",
      4,
      OpenCVDetector.SIFT,
      PatchExtractor(Gray, 2, 3),
      VectorMatcher.L1)
    val pickle = experiment.pickle
    val unpickled = pickle.unpickle[OxfordExperiment[OpenCVDetector.SIFT.type, PatchExtractor, VectorMatcher.L1.type, DenseMatrix[IndexedSeq[Int]]]]

    assert(experiment == unpickled)
  }

  test("pickling FAST SIFT L2", InstantTest) {
    val experiment = OxfordExperiment(
      "boat",
      4,
      OpenCVDetector.FAST,
      OpenCVExtractor.SIFT,
      VectorMatcher.L2)
    val pickle = experiment.pickle
    val unpickled = pickle.unpickle[OxfordExperiment[OpenCVDetector.FAST.type, OpenCVExtractor.SIFT.type, VectorMatcher.L2.type, IndexedSeq[Double]]]

    assert(experiment == unpickled)
  }

  test("pickling FAST SIFT KendallTau", InstantTest) {
    val experiment = OxfordExperiment(
      "boat",
      4,
      OpenCVDetector.FAST,
      OpenCVExtractor.SIFT,
      VectorMatcher.KendallTau)
    val pickle = experiment.pickle
    val unpickled = pickle.unpickle[OxfordExperiment[OpenCVDetector.FAST.type, OpenCVExtractor.SIFT.type, VectorMatcher.KendallTau.type, IndexedSeq[Double]]]

    assert(experiment == unpickled)
  }

  test("run FAST SIFT L1", MediumTest) {
    val experiment = OxfordExperiment(
      "boat",
      2,
      BoundedDetector(OpenCVDetector.FAST, 20),
      OpenCVExtractor.SIFT,
      VectorMatcher.L1)

    experiment.groundTruthHomography
    experiment.leftImage
    experiment.rightImage

    val results = experiment.run
    assert(results.distances.rows == results.distances.cols)
    results.distances.foreachValue(distance => assert(distance >= 0))
  }

  test("matching an image to itself should produce perfect performance", MediumTest) {
    val experiment = OxfordExperiment(
      "boat",
      1,
      BoundedDetector(OpenCVDetector.FAST, 20),
      OpenCVExtractor.SIFT,
      VectorMatcher.L1)

    val results = experiment.run
    for (index <- 0 until results.distances.rows) {
      assert(results.distances(index, index) == 0)
    }
  }
}