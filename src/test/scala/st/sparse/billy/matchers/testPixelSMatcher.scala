package st.sparse.billy.matchers

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
class TestPixelSMatcher extends FunGeneratorSuite with st.sparse.billy.TestUtil {
  val image = palmTree
  val detector = BoundedDetector(32, OpenCVDetector.FAST)
  val keyPoints = detector.detect(image)
  assert(keyPoints.size > 0)

  test("weighted mean and std", FastTest) {
    val data = 100 times (random.nextDouble)
    val weights = 100 times (random.nextDouble)
    
    val mean = PixelSMatcher.weightedMean(data.zip(weights))
    val dataCentered = data map(_ - mean)
    val centeredMean = PixelSMatcher.weightedMean(dataCentered.zip(weights))
    assert(centeredMean.abs <= 0.001)
    
    val std = PixelSMatcher.weightedSTD(data.zip(weights))
    val dataScaled = data map (_ / std)
    val scaledSTD = PixelSMatcher.weightedSTD(dataScaled.zip(weights))
    assert(scaledSTD == 1.0)
  }

  ignore("distances are sane", SlowTest) {
    val patchWidth = 16
    val extractor = AndExtractor(
      PatchExtractor(Gray, patchWidth, 2),
      ForegroundMaskExtractor(patchWidth))

    val descriptors = extractor.extract(image, keyPoints)
    val descriptorsFlat = descriptors.flatten

    val matcher = PixelSMatcher(1.0, 1.0)

    val distances = matcher.matchAll(descriptorsFlat, descriptorsFlat)

    assert(distances.min >= 0)
    diag(distances).foreach(element => assert(element == 0))
  }
}