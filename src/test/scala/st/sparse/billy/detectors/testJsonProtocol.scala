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
import com.sksamuel.scrimage._
import org.opencv.core.KeyPoint
import java.io.File
import spray.json._
import DefaultJsonProtocol._

////////////////////////////////////////////////////////////////////////////////

@RunWith(classOf[JUnitRunner])
class TestJsonProtocol extends FunGeneratorSuite with st.sparse.billy.experiments.TestUtil with Logging {
  test("Detector", InstantTest) {
    val detector = OpenCVDetector.FAST

    checkJsonSerialization(detector)
  }

  test("BoundedDetector", InstantTest) {
    val detector = BoundedDetector(100, OpenCVDetector.FAST)

    checkJsonSerialization(detector)
  }

  test("DoublyBoundedPairDetector", InstantTest) {
    val detector = DoublyBoundedPairDetector(2, 10, 100, OpenCVDetector.FAST)

    checkJsonSerialization(detector)
  }
}