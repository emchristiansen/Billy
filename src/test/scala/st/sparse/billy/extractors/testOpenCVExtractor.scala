package st.sparse.billy.extractors

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
class TestOpenCVExtractor extends FunGeneratorSuite with st.sparse.billy.TestUtil {
  val image = goldfishGirl
  val detector = BoundedDetector(10, OpenCVDetector.FAST)
  val keyPoints = detector.detect(image)
  assert(keyPoints.size > 0)

  test("BRISK", FastTest) {
    val extractor = OpenCVExtractor.BRISK

    assert(extractor.extract(image, keyPoints).flatten.size > 0)
  }

  test("FREAK", FastTest) {
    val extractor = OpenCVExtractor.FREAK

    assert(extractor.extract(image, keyPoints).flatten.size > 0)
  }

  test("BRIEF", FastTest) {
    val extractor = OpenCVExtractor.BRIEF

    assert(extractor.extract(image, keyPoints).flatten.size > 0)
  }

  test("ORB", FastTest) {
    val extractor = OpenCVExtractor.ORB

    assert(extractor.extract(image, keyPoints).flatten.size > 0)
  }

  test("SIFT", FastTest) {
    val extractor = OpenCVExtractor.SIFT

    assert(extractor.extract(image, keyPoints).flatten.size > 0)
  }

  test("SURF", FastTest) {
    val extractor = OpenCVExtractor.SURF

    assert(extractor.extract(image, keyPoints).flatten.size > 0)
  }
}