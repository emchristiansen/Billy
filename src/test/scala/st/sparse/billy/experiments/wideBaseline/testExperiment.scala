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
  test("caching", InstantTest) {
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
}