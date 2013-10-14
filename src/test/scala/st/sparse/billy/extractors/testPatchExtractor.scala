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
class TestPatchExtractor extends FunGeneratorSuite with st.sparse.billy.TestUtil  {
  val image = goldfishGirl
  val detector = BoundedDetector(OpenCVDetector.FAST, 10)
  val keyPoints = detector.detect(image)
  assert(keyPoints.size > 0)

  test("Gray", FastTest) {
    val patchWidth = 4
    
    val extractor = PatchExtractor(
      Gray,
      patchWidth,
      1)

    val descriptors = extractor.extract(image, keyPoints).flatten
    assert(descriptors.size > 0)
    assert(descriptors.head.rows == patchWidth)
    assert(descriptors.head.cols == patchWidth)
    assert((descriptors.head)(0, 0).size == 1)
  }

  test("RGB", FastTest) {
    val patchWidth = 4
    
    val extractor = PatchExtractor(
      RGB,
      patchWidth,
      1)

    val descriptors = extractor.extract(image, keyPoints).flatten
    assert(descriptors.size > 0)
    assert(descriptors.head.rows == patchWidth)
    assert(descriptors.head.cols == patchWidth)
    assert((descriptors.head)(0, 0).size == 3)
  }
}