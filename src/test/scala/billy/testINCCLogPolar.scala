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

import nebula._
import org.scalatest._
import javax.imageio.ImageIO
import java.io.File
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import nebula.util._
import billy.JsonProtocols._
import spray.json._
import com.sun.xml.internal.bind.v2.model.runtime.RuntimeClassInfo
import breeze.linalg._
import breeze.math._
import grizzled.math.stats
import org.scalacheck._
import org.scalatest.prop._
import org.scalatest._
import DenseMatrixUtil._
import org.opencv.features2d.{ DMatch, KeyPoint }
import org.opencv.highgui.Highgui
import org.opencv.core._
import org.opencv.core
import org.opencv.imgproc.Imgproc
import org.opencv.imgproc.Imgproc._
import nebula.imageProcessing.RichImage._

///////////////////////////////////////////////////////////

@RunWith(classOf[JUnitRunner])
@WrapWith(classOf[ConfigMapWrapperSuite])
class TestINCCLogPolar(
  val configMap: Map[String, Any]) extends ConfigMapFunSuite with GeneratorDrivenPropertyChecks with ShouldMatchers {
  import TestUtil._

  loadOpenCV

  val image = ImageIO.read(new File(
    getClass.getResource("/iSpy.png").getFile).mustExist)

  val imageMat = Highgui.imread(new File(
    getClass.getResource("/iSpy.png").getFile).mustExist.toString)

  val random = new scala.util.Random(0)

  def randomPoint(width: Int, height: Int, buffer: Int): KeyPoint = {
    val x = random.nextFloat * (width - 2 * buffer) + buffer
    val y = random.nextFloat * (height - 2 * buffer) + buffer
    //    KeyPointUtil(x, y)
    KeyPointUtil(x.floor + 0.5.toFloat, y.floor + 0.5.toFloat)
  }

  val genRandomPoint = Gen(_ => Some(randomPoint(
    image.getWidth,
    image.getHeight,
    100)))

  ignore("scale algorithms", FastTest, InteractiveTest) {
    val methodMap = Map(
      INTER_LINEAR -> "linear",
      INTER_AREA -> "area",
      INTER_CUBIC -> "cubic",
      INTER_LANCZOS4 -> "lanczos4")

    for (
      factor <- Seq(0.25, 0.5, 1, 2, 4);
      method <- Seq(INTER_LINEAR, INTER_AREA, INTER_CUBIC, INTER_LANCZOS4)
    ) {
      val resized = new Mat
      val newWidth = (image.getWidth * factor).round.toInt
      val newHeight = (image.getHeight * factor).round.toInt
      Imgproc.resize(imageMat, resized, new core.Size(newWidth, newHeight))
      dumpImage(
        f"openCVScaleAlgorithms_${factor}%.2f_${methodMap(method)}.png",
        OpenCVUtil.matToBufferedImage(resized))
    }
  }

  ignore("getScaleFactors", FastTest) {
    for (
      samplingRadius <- Seq(1, 2, 4, 5);
      minRadius <- Seq(1, 3, 8);
      maxRadius <- Seq(10, 11, 22);
      numScales <- Seq(2, 4, 8, 16)
    ) {
      val scala = LogPolar.getScaleFactors(
        samplingRadius,
        minRadius,
        maxRadius,
        numScales)

      val openCV = Highgui.getScaleFactors(
        samplingRadius,
        minRadius,
        maxRadius,
        numScales)

      assertNear(scala, openCV.toArray.toIndexedSeq)
    }
  }

  ignore("getRealScaleTargetsMat", FastTest) {
    forAll(Gen.listOf(Gen.posNum[Double])) { idealScalingFactors =>
      val scala = LogPolar.getRealScaleTargets(
        idealScalingFactors.toIndexedSeq,
        image.getWidth,
        image.getHeight)

      val openCVMat = Highgui.getRealScaleTargetsMat(
        new MatOfDouble(idealScalingFactors: _*),
        image.getWidth,
        image.getHeight)

      if (openCVMat.rows > 0) {
        asserty(openCVMat.cols == 4)

        val scalaMatrix = {
          val seqSeq = scala map {
            case ((targetSizeX, targetSizeY), (targetScaleX, targetScaleY)) =>
              Seq(targetSizeX, targetSizeY, targetScaleX, targetScaleY)
          }
          seqSeq.toMatrix
        }

        val openCVMatrix = DenseMatrixUtil.matToMatrixDouble(openCVMat).get

        assertNear(scalaMatrix, openCVMatrix)
      }
    }
  }

  ignore("scaleImagesOnly", SlowTest, InteractiveTest) {
    for (
      samplingRadius <- Seq(4);
      minRadius <- Seq(1, 2);
      maxRadius <- Seq(10, 22);
      numScales <- Seq(4);
      blurWidth <- Seq(2, 5)
    ) {
      val openCV = Highgui.scaleImagesOnly(
        samplingRadius,
        minRadius,
        maxRadius,
        numScales,
        blurWidth / 2,
        imageMat).toArray.map(_.asInstanceOf[Mat]).toIndexedSeq

      val openCVMatrices = openCV map { image =>
        val openCVGray = new Mat
        Imgproc.cvtColor(image, openCVGray, COLOR_BGR2GRAY)
        DenseMatrixUtil.matToMatrixDouble(openCVGray).get
      }

      val scala = LogPolar.scaleImage(
        samplingRadius,
        minRadius,
        maxRadius,
        numScales,
        blurWidth,
        image)._3

      val scalaMatrices = scala map (_.toMatrix mapValues (_.toDouble))

      assert(openCVMatrices.size == scalaMatrices.size)
      val differences = for ((o, s) <- openCVMatrices zip scalaMatrices) yield {
        asserty(o.rows == s.rows)
        asserty(o.cols == s.cols)

        val time = System.currentTimeMillis
        dumpImage(s"scaleImagesOnly_${time}_openCV", o.toImage)
        dumpImage(s"scaleImagesOnly_${time}_scala", s.toImage)

        val meanDifference =
          ((o - s) mapValues (_.abs)).sum / (o.rows * o.cols)
        meanDifference
      }

      val meanDifference = differences.sum / differences.size
      asserty(meanDifference <= 20)
    }
  }

  ignore("sampleSubPixelGray", FastTest) {
    forAll(genRandomPoint) { keyPoint =>
      val x = keyPoint.pt.x
      val y = keyPoint.pt.y
      val scala = image.getSubPixel(x, y).get.gray.head
      val openCV = Highgui.sampleSubPixelGray(imageMat, x, y)

      asserty(scala == openCV)
    }
  }

  ignore("samplePoint", FastTest) {
    for (
      samplingRadius <- Seq(1, 2, 5);
      numAngles <- Seq(2, 8, 32);
      realScaleFactorX <- Seq(0.4, 1.1, 2.3);
      realScaleFactorY <- Seq(0.3, 0.9, 1.0);
      angleIndex <- 0 until numAngles;
      x <- Seq(10, 13.5, 71);
      y <- Seq(11, 53, 210.2)
    ) {
      val scala = LogPolar.samplePoint(
        samplingRadius,
        numAngles,
        realScaleFactorX,
        realScaleFactorY,
        angleIndex,
        KeyPointUtil(x.toFloat, y.toFloat))

      val openCV = Highgui.samplePoint(
        samplingRadius,
        numAngles,
        realScaleFactorX,
        realScaleFactorY,
        angleIndex,
        new Point(x.toFloat, y.toFloat))

      val scalaPoints = IndexedSeq(scala.pt.x, scala.pt.y)
      val openCVPoints = IndexedSeq(openCV.x, openCV.y)

      assertNear(scalaPoints, openCVPoints)
    }
  }

  ignore("extract raw log polar pattern", FastTest) {
    implicit val generatorDrivenConfig =
      PropertyCheckConfig(minSuccessful = 20, maxSize = 5)
    forAll(Gen.listOf(genRandomPoint)) { keyPoints =>
      asserty(keyPoints.size <= 5)
      val numScales = 16
      val numAngles = 32

      val openCVDescriptors = {
        val keyPointsMat = new MatOfKeyPoint(keyPoints: _*)
        val descriptorList = Highgui.rawLogPolarSeq(
          8,
          32,
          numScales,
          numAngles,
          2,
          imageMat,
          keyPointsMat)

        val mats = descriptorList.toArray.toIndexedSeq.map(_.asInstanceOf[Mat])
        val doubleMatrices = mats map
          DenseMatrixUtil.matToMatrixDouble
        doubleMatrices.map(_.map(_ mapValues (_.toInt)))
      }

      val scalaDescriptors = LogPolar.rawLogPolarSeq(
        true,
        8,
        32,
        numScales,
        numAngles,
        2)(
          image,
          keyPoints)

      asserty(openCVDescriptors.size == scalaDescriptors.size)
      asserty(openCVDescriptors.flatten.size == scalaDescriptors.flatten.size)

      for ((openCV, scala) <- openCVDescriptors.flatten zip scalaDescriptors.flatten) {
        //        val time = System.currentTimeMillis
        //        dumpImage(s"rawLogPolar_${time}_openCV", TestUtil.scale100(openCV.toImage))
        //        dumpImage(s"rawLogPolar_${time}_scala", TestUtil.scale100(scala.toImage))

        val correlation = FFT.correlateSameSize(
          openCV mapValues (x => Complex(x, 0)),
          scala mapValues (x => Complex(x, 0)))

        asserty(correlation.argmax == (0, 0))

        //        implicit val epsilon = Epsilon(20)
        //        assertNear(openCV, scala)
      }
    }
  }

  test("fft2DInteger on simple example", FastTest, SlowTest) {
    val mat = new Mat(2, 4, CvType.CV_16SC1)
    mat.put(0, 0, -100)
    mat.put(0, 1, 630)
    mat.put(0, 2, -100)
    mat.put(0, 3, 30)
    mat.put(1, 0, -120)
    mat.put(1, 1, -20)
    mat.put(1, 2, 50)
    mat.put(1, 3, -160)

    println(DenseMatrixUtil.matToMatrixDouble(mat))
    val fourier = Highgui.fft2DInteger(mat)
    println(DenseMatrixUtil.matToMatrixDouble(fourier))

    val inverted = Highgui.ifft2DInteger(fourier)
    println(DenseMatrixUtil.matToMatrixDouble(inverted))
    
    //    val data = IndexedSeq(0.1, 0.0, 0.2, 0.3, 0.2, 0.1, 0.0, 0.8)
    //    val spatial = data.grouped(4).toIndexedSeq.toMatrix
    //    
    //    val fourier = FFT.fft2(spatial mapValues (r => Complex(r, 0)))
    //    println(fourier.t)
    //    
    //    val mat = DenseMatrixUtil.matrixDoubleToMat(spatial)
    //    println(DenseMatrixUtil.matToMatrixDouble(mat).get.t)
    //    println(DenseMatrixUtil.matToMatrixDouble(mat).get.t)
    //    val ocvFourier = Highgui.fft2DDouble(mat)
    //    println(DenseMatrixUtil.matToMatrixDouble(ocvFourier).get.t)

    //    val mat = new Mat(1, 4, CvType.CV_64FC1)
    //    mat.put(0, 0, -10)
    //    mat.put(0, 1, 63)
    //    mat.put(0, 2, 7)
    //    mat.put(0, 3, -2)
    //
    //    println(DenseMatrixUtil.matToMatrixDouble(mat))
    //    val fourier = Highgui.fft2DDouble(mat)
    //    println(DenseMatrixUtil.matToMatrixDouble(fourier))

  }
}

