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
import st.sparse.sundry.ExpectyOverrides._
import breeze.linalg._
import scala.reflect.ClassTag

////////////////////////////////////////////////////////////////////////////////

@RunWith(classOf[JUnitRunner])
class TestBRIEFSExtractor extends FunGeneratorSuite with st.sparse.billy.TestUtil {
  val image = palmTree
  val detector = BoundedDetector(16, OpenCVDetector.FAST)
  val keyPoints = detector.detect(image)
  assert(keyPoints.size > 0)

  test("correct length", SlowTest, MatlabTest) {
    val extractor = BRIEFSExtractor(32, 64, 2)
    val descriptors = extractor.extract(image, keyPoints)
    assert(descriptors.flatten.size >= 8)
    assert(descriptors.flatten.forall(_.size == 32))
  }
}