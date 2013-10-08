package billy.extractors

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
class TestOpenCVExtractor extends FunGeneratorSuite {
  init

  val image = billy.TestUtil.goldfishGirl
  val detector = BoundedDetector(OpenCVDetector.FAST, 10)
  val keyPoints = detector.detect(image)
  assert(keyPoints.size > 0)

  test("BRISK", FastTest) {
    val extractor = OpenCVExtractor.BRISK

    assert(extractor.extract(image, keyPoints).size > 0)
  }

  test("FREAK", FastTest) {
    val extractor = OpenCVExtractor.FREAK

    assert(extractor.extract(image, keyPoints).size > 0)
  }

  test("BRIEF", FastTest) {
    val extractor = OpenCVExtractor.BRIEF

    assert(extractor.extract(image, keyPoints).size > 0)
  }

  test("ORB", FastTest) {
    val extractor = OpenCVExtractor.ORB

    assert(extractor.extract(image, keyPoints).size > 0)
  }

  test("SIFT", FastTest) {
    val extractor = OpenCVExtractor.SIFT

    assert(extractor.extract(image, keyPoints).size > 0)
  }

  test("SURF", FastTest) {
    val extractor = OpenCVExtractor.SURF

    assert(extractor.extract(image, keyPoints).size > 0)
  }
}