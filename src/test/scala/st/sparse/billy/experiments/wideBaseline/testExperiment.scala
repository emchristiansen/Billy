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

////////////////////////////////////////////////////////////////////////////////

case class Foo[A](data: Array[A])
case class MyResults(data: Foo[Double])

@RunWith(classOf[JUnitRunner])
class TestExperiment extends FunGeneratorSuite with st.sparse.billy.experiments.TestUtil {
  test("simple caching", MediumTest) {
    val oxford = Oxford(
      "boat",
      4,
      BoundedDetector(OpenCVDetector.SIFT, 20),
      OpenCVExtractor.SIFT,
      VectorMatcher.L0)

    val experiment = Experiment.cached(oxford)

    val results1 = experiment.run
    val results2 = experiment.run
    assert(results1 == results2)
  }

  test("multiple types entries per table", MediumTest) {
    val oxford1 = Oxford(
      "boat",
      4,
      BoundedDetector(OpenCVDetector.FAST, 20),
      OpenCVExtractor.ORB,
      VectorMatcher.L0)
    val experiment1 = Experiment.cached(oxford1)
    val results1 = experiment1.run

    val oxford2 = Oxford(
      "boat",
      5,
      BoundedDetector(OpenCVDetector.SIFT, 20),
      OpenCVExtractor.SIFT,
      VectorMatcher.L1)
    val experiment2 = Experiment.cached(oxford2)
    val results2 = experiment2.run

    val oxford3 = Oxford(
      "boat",
      4,
      BoundedDetector(OpenCVDetector.SIFT, 20),
      OpenCVExtractor.SIFT,
      VectorMatcher.L2)
    val experiment3 = Experiment.cached(oxford3)
    val results3 = experiment3.run
    
    assert(results1 == experiment1.run)
    assert(results2 == experiment2.run)
    assert(results3 == experiment3.run)
  }
}