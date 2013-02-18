package billy

import nebula._
import nebula.imageProcessing._
import nebula.util._

import billy._
import billy.brown._
import billy.mpie._
import billy.smallBaseline._
import billy.wideBaseline._
import billy.summary._

import org.scalatest._
import org.opencv.features2d._
import javax.imageio.ImageIO
import java.io.File
import org.opencv.core.MatOfKeyPoint
import org.opencv.features2d.{ FeatureDetector, KeyPoint }
import nebula._
import org.opencv.core.Mat
import java.awt.Color
import java.awt.image.BufferedImage
import org.apache.commons.math3.linear.Array2DRowRealMatrix
import nebula.util.Homography
import nebula.util.OpenCVUtil
import nebula.util.KeyPointUtil
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.ConfigMapWrapperSuite

///////////////////////////////////////////////////////////

@RunWith(classOf[JUnitRunner])
@WrapWith(classOf[ConfigMapWrapperSuite])
class TestExtractor(val configMap: Map[String, Any]) extends ConfigMapFunSuite {
  //  test("uniformRank") {
  //    val pixels = IndexedSeq(2, 2, 3, 3, 3)
  //    val uniformRank = Extractor.uniformRank(pixels)
  //    asserty(uniformRank === IndexedSeq(1, 1, 3, 3, 3))
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

  test("elucid", FastTest) {
    val extractor = ELUCIDExtractor(true, true, 4, 2, 2, 5, "Gray")

    val url = getClass.getResource("/goldfish_girl.png")
    val image = ImageIO.read(new File(url.getFile))

    assert(extractor.extractSingle(image, new KeyPoint(0, 0, 1, 0, 1, 1, 1)) === None)
    asserty(extractor.extractSingle(image, new KeyPoint(5, 5, 1, 0, 1, 1, 1)).isDefined)
  }

  test("elucid bikes", FastTest, DatasetTest, InteractiveTest) {
    loadOpenCV

    def drawPointsOnBikes(image: BufferedImage, file: File) {
      val detector = BoundedDetector(OpenCVDetector.BRISK, 60)
      val keyPoints = detector.detect(image)

      val extractor = ELUCIDExtractor(
        true,
        true,
        16,
        0.3,
        4,
        5,
        "Gray")

      //      val blurred = ImageUtil.boxBlur(5, image)
      //      val graphics = blurred.getGraphics
      //      for (keyPoint <- keyPoints) {
      //        val samplePoints = extractor.samplePoints(keyPoint)
      //        for ((point, index) <- samplePoints.zipWithIndex) {
      //          val percentDone = index.toFloat / (samplePoints.size - 1)
      //          val color = Color.getHSBColor(percentDone, 1, 1)
      //          graphics.setColor(color)
      //          graphics.fillOval(point(0).round.toInt - 1, point(1).round.toInt - 1.toInt, 2, 2)
      //        }
      //      }
      //
      //      ImageIO.write(blurred, "bmp", file)
    }

    val image1 = ImageIO.read(datasetRoot + "oxfordImages/boat/images/img1.bmp")
    val image2 = ImageIO.read(datasetRoot + "oxfordImages/boat/images/img2.bmp")
    val image3 = ImageIO.read(datasetRoot + "oxfordImages/boat/images/img3.bmp")

    drawPointsOnBikes(image1, new File("/tmp/test_elucid_bikes1.bmp"))
    drawPointsOnBikes(image2, new File("/tmp/test_elucid_bikes2.bmp"))
    drawPointsOnBikes(image3, new File("/tmp/test_elucid_bikes3.bmp"))
  }
}