package billy

import nebula._
import nebula.imageProcessing._
import nebula.util._

import billy._
import billy.brown._

import billy.smallBaseline._
import billy.wideBaseline._
import billy.summary._

import billy.detectors._
import billy.extractors._
import billy.matchers._

import org.scalatest._
import org.opencv.features2d._
import javax.imageio.ImageIO
import java.io.File
import org.opencv.core.MatOfKeyPoint
import org.opencv.features2d.{ FeatureDetector, KeyPoint }
import nebula._
import org.opencv.core.Mat
import java.awt.Color 
import org.apache.commons.math3.linear.Array2DRowRealMatrix
import nebula.util.Homography
import nebula.util.OpenCVUtil
import nebula.util.KeyPointUtil
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.ConfigMapWrapperSuite
import scalatestextra._

///////////////////////////////////////////////////////////

@RunWith(classOf[JUnitRunner])
class TestExtractor extends FunGeneratorSuite {
  //  test("uniformRank") {
  //    val pixels = IndexedSeq(2, 2, 3, 3, 3)
  //    val uniformRank = Extractor.uniformRank(pixels)
  //    assert(uniformRank === IndexedSeq(1, 1, 3, 3, 3))
  //  }

  test("BRISK", FastTest) {
    loadOpenCV

    val image = ImageIO.read(getResource("/goldfish_girl.png"))

    val detector = FeatureDetector.create(FeatureDetector.BRISK)
    val matImage = OpenCVUtil.bufferedImageToMat(image)
    val keyPoints = new MatOfKeyPoint
    detector.detect(matImage, keyPoints)

    val keyPoint = keyPoints.toArray.sortBy(_.response).reverse.head

    val homography =
      Homography(new Array2DRowRealMatrix(Array(
        Array(1.0, 0.0, 0.0),
        Array(0.0, 1.0, 0.0),
        Array(0.0, 0.0, 1.0))))

    val rightKeyPoint = KeyPointUtil.transform(homography)(keyPoint)
  }
}
