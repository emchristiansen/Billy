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
class TestOpenCVDetector extends FunGeneratorSuite with st.sparse.billy.TestUtil { 
  val image = goldfishGirl
   
  test("FAST", FastTest) {
    val detector = OpenCVDetector.FAST
    assert(detector.detect(image).size > 0)
  }
  
  test("BRISK", FastTest) {
    val detector = OpenCVDetector.BRISK
    assert(detector.detect(image).size > 0)
  }
  
  test("SIFT", FastTest) {
    val detector = OpenCVDetector.SIFT
    assert(detector.detect(image).size > 0)
  }
  
  test("SURF", FastTest) {
    val detector = OpenCVDetector.SURF
    assert(detector.detect(image).size > 0)
  }
  
  test("ORB", FastTest) {
    val detector = OpenCVDetector.ORB
    assert(detector.detect(image).size > 0)
  }
}