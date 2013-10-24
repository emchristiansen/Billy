package st.sparse.billy.detectors

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
class TestDoublyBoundedPairDetector extends FunGeneratorSuite with st.sparse.billy.TestUtil {
  test("pickling", FastTest) {
    val detector = DoublyBoundedPairDetector(2, 10, 100, OpenCVDetector.SIFT)
    val pickled = detector.pickle
    val unpickled = pickled.unpickle[DoublyBoundedPairDetector[OpenCVDetector.SIFT.type]]
    assert(detector == unpickled)
  }
}