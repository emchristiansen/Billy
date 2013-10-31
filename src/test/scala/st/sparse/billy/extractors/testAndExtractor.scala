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
import st.sparse.billy._
import java.io.File

////////////////////////////////////////////////////////////////////////////////

@RunWith(classOf[JUnitRunner])
class TestAndExtractor extends FunGeneratorSuite with st.sparse.billy.TestUtil {
  val image = palmTree
  val detector = BoundedDetector(32, OpenCVDetector.FAST)
  val keyPoints = detector.detect(image)
  assert(keyPoints.size > 0)

  test("Patch and ForegroundMask", SlowTest, MatlabTest) {
    val patchWidth = 64
    val extractor = AndExtractor(
      PatchExtractor(Gray, patchWidth, 1),
      ForegroundMaskExtractor(patchWidth))

    val descriptors = extractor.extract(image, keyPoints)
    assert(descriptors.flatten.size >= 8)

    descriptors.flatten foreach {
      case (patch, foregroundMask) => {       
        val patchImage = patch.mapValues(_.head.toDouble / 255).toImage

        val foregroundMaskImage = foregroundMask.toImage
        
        logDirectory("PatchAndMask") { directory: ExistingDirectory =>
          patchImage.write(new File(directory, "patch.png"))
          foregroundMaskImage.write(new File(directory, "foregroundMask.png"))
        }
      }
    }
  }
}