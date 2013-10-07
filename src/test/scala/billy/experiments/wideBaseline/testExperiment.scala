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

import scalatestextra._
import breeze.linalg._
import scala.reflect.ClassTag

////////////////////////////////////////////////////////////////////////////////

@RunWith(classOf[JUnitRunner])
class TestWideBaselineExperiment extends FunGeneratorSuite {
  test("pickling", InstantTest) {
    val experiment = Experiment(
      "bikes",
      4,
      OpenCVDetector.FAST,
      OpenCVExtractor.SIFT,
      VectorMatcher.L0)
    val pickle = experiment.pickle
    val unpickled = pickle.unpickle[Experiment[OpenCVDetector.FAST.type, OpenCVExtractor.SIFT.type, VectorMatcher.L0.type, IndexedSeq[Double]]]

    assert(experiment == unpickled)
  }

  //  test("acts like a map", InstantTest) {
  //
  //  }
  //
  //  test("a generator driven test", InstantTest) {
  //    val evenInts = for (n <- Gen.choose(-1000, 1000)) yield 2 * n
  //    forAll(evenInts) { x =>
  //      assert(x % 2 == 0)
  //    }
  //  }
}