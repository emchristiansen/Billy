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
class TestExperiment extends FunGeneratorSuite with billy.experiments.TestUtil  {
  test("pickling FAST SIFT L0", InstantTest) {
    val experiment = Experiment(
      "boat",
      4,
      OpenCVDetector.FAST,
      OpenCVExtractor.SIFT,
      VectorMatcher.L0)
    val pickle = experiment.pickle
    val unpickled = pickle.unpickle[Experiment[OpenCVDetector.FAST.type, OpenCVExtractor.SIFT.type, VectorMatcher.L0.type, IndexedSeq[Double]]]

    assert(experiment == unpickled)
  }
  
  // TODO: A pickling bug means this isn't working.
//  test("pickling SIFT PatchExtractor L0", InstantTest) {
//    val experiment = Experiment(
//      "boat",
//      4,
//      OpenCVDetector.SIFT,
//      PatchExtractor(Gray, 2, 3),
//      VectorMatcher.L0)
//    val pickle = experiment.pickle
//    val unpickled = pickle.unpickle[Experiment[OpenCVDetector.SIFT.type, PatchExtractor, VectorMatcher.L0.type, DenseMatrix[IndexedSeq[Int]]]]
//
//    assert(experiment == unpickled)
//  }
//
//  test("pickling SIFT PatchExtractor L1", InstantTest) {
//    val experiment = Experiment(
//      "boat",
//      4,
//      OpenCVDetector.SIFT,
//      PatchExtractor(Gray, 2, 3),
//      VectorMatcher.L1)
//    val pickle = experiment.pickle
//    val unpickled = pickle.unpickle[Experiment[OpenCVDetector.SIFT.type, PatchExtractor, VectorMatcher.L1.type, DenseMatrix[IndexedSeq[Int]]]]
//
//    assert(experiment == unpickled)
//  }

  test("pickling FAST SIFT L2", InstantTest) {
    val experiment = Experiment(
      "boat",
      4,
      OpenCVDetector.FAST,
      OpenCVExtractor.SIFT,
      VectorMatcher.L2)
    val pickle = experiment.pickle
    val unpickled = pickle.unpickle[Experiment[OpenCVDetector.FAST.type, OpenCVExtractor.SIFT.type, VectorMatcher.L2.type, IndexedSeq[Double]]]

    assert(experiment == unpickled)
  }

  test("pickling FAST SIFT KendallTau", InstantTest) {
    val experiment = Experiment(
      "boat",
      4,
      OpenCVDetector.FAST,
      OpenCVExtractor.SIFT,
      VectorMatcher.KendallTau)
    val pickle = experiment.pickle
    val unpickled = pickle.unpickle[Experiment[OpenCVDetector.FAST.type, OpenCVExtractor.SIFT.type, VectorMatcher.KendallTau.type, IndexedSeq[Double]]]

    assert(experiment == unpickled)
  }

  test("run FAST SIFT L1", MediumTest) {   
    val experiment = Experiment(
      "boat",
      2,
      BoundedDetector(OpenCVDetector.FAST, 100),
      OpenCVExtractor.SIFT,
      VectorMatcher.L1)
    
    experiment.groundTruthHomography
    experiment.leftImage
    experiment.rightImage
    
    experiment.run
  }
}