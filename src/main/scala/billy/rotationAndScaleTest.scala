package billy

import java.io.File

import nebula._
import nebula.imageProcessing._
import nebula.util._

import billy._
import billy.brown._
import billy.mpie._
import billy.smallBaseline._
import billy.wideBaseline._
import billy.summary._

import org.opencv.features2d.DMatch

import javax.imageio.ImageIO

import nebula.util.Homography
import nebula.util.JSONUtil
import nebula.util.Logging
import nebula.util.Memoize
import spray.json.JsonFormat
import spray.json.pimpAny
import spray.json.pimpString
import spray.json._
import nebula.util.JSONUtil._
import nebula.util.DMatchJsonProtocol._

import java.awt.image.AffineTransformOp.TYPE_BILINEAR
import java.awt.geom.AffineTransform
import java.awt.image.{ AffineTransformOp, BufferedImage, ColorConvertOp, ConvolveOp, DataBufferInt, Kernel }
import org.apache.commons.math3.linear._

///////////////////////////////////////////////////////////

// TODO: Clean this up
case class RotationAndScaleExperiment[D <% PairDetector, E <% Extractor[F], M <% Matcher[F], F](
  imagePath: ExistingFile,
  detector: D,
  extractor: E,
  matcher: M,
  scaleFactor: Double,
  theta: Double) {
  asserty(scaleFactor > 0)
  asserty(theta >= 0)
  asserty(theta < 2 * math.Pi)
}

object RotationAndScaleExperiment {
  implicit class RotationAndScaleExperiment2ExperimentRunner[D <% PairDetector, E <% Extractor[F], M <% Matcher[F], F](
    self: RotationAndScaleExperiment[D, E, M, F]) extends ExperimentRunner[WideBaselineExperimentResults[D, E, M, F]] {
    override def run = {
      val leftImage: BufferedImage = ImageIO.read(self.imagePath)

      val translationTransform =
        AffineTransform.getTranslateInstance(
          -leftImage.getWidth / 2.0,
          -leftImage.getHeight / 2.0)
      val inverseTranslationTransform =
        AffineTransform.getTranslateInstance(
          leftImage.getWidth / 2.0,
          leftImage.getHeight / 2.0)
      val scaleTransform =
        AffineTransform.getScaleInstance(self.scaleFactor, self.scaleFactor)
      val rotationTransform =
        AffineTransform.getRotateInstance(self.theta)
      val similarityTransform = {
        val identity = new AffineTransform
        identity.concatenate(inverseTranslationTransform)
        identity.concatenate(scaleTransform)
        identity.concatenate(rotationTransform)
        identity.concatenate(translationTransform)
        identity
      }

      val similarityOp = new AffineTransformOp(
        similarityTransform,
        AffineTransformOp.TYPE_BILINEAR)

      val groundTruth: Homography = {
        val affineMatrix = Array(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
        similarityTransform.getMatrix(affineMatrix)
        asserty(affineMatrix.size == 6)
        val homographyData = new Array2DRowRealMatrix(3, 3)
        homographyData.setEntry(0, 0, affineMatrix(0))
        homographyData.setEntry(1, 0, affineMatrix(1))
        homographyData.setEntry(2, 0, 0)
        homographyData.setEntry(0, 1, affineMatrix(2))
        homographyData.setEntry(1, 1, affineMatrix(3))
        homographyData.setEntry(2, 1, 0)
        homographyData.setEntry(0, 2, affineMatrix(4))
        homographyData.setEntry(1, 2, affineMatrix(5))
        homographyData.setEntry(2, 2, 1)
        Homography(homographyData)
      }

      println(groundTruth)

      //////////////////////

      val rightImage: BufferedImage = {
        val image = new BufferedImage(
          leftImage.getWidth,
          leftImage.getHeight,
          leftImage.getType)
        similarityOp.filter(leftImage, image)
        image
        //        leftImage
      }

//      nebula.TestUtil.dumpImage("imageLeft", leftImage)
//      nebula.TestUtil.dumpImage("imageRight", rightImage)
      
      //      println(leftImage.getType)
      //      println(rightImage.getType)
      //      
      //      println(similarityTransform)
      //      println(similarityOp)
      //      println(rightImage.getWidth)
      //      println(rightImage.getHeight)

      val (leftKeyPoints, rightKeyPoints) = self.detector.detectPair(
        groundTruth,
        leftImage,
        rightImage) unzip

      println(s"Number of KeyPoints: ${leftKeyPoints.size}")

      val (leftDescriptors, rightDescriptors) = {
        val leftDescriptors = self.extractor.extract(leftImage, leftKeyPoints)
        val rightDescriptors = self.extractor.extract(rightImage, rightKeyPoints)

        for ((Some(left), Some(right)) <- leftDescriptors.zip(rightDescriptors)) yield (left, right)
      } unzip

      println(s"Number of surviving KeyPoints: ${leftDescriptors.size}")

      val dmatches = self.matcher.doMatch(true, leftDescriptors, rightDescriptors)

      val dummyExperiment = WideBaselineExperiment("dummyExperimentDeleteMe", 2, self.detector, self.extractor, self.matcher)

      WideBaselineExperimentResults(dummyExperiment, dmatches)
    }
  }
}
