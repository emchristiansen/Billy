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
  theta: Double)

object RotationAndScaleExperiment {
  implicit class RotationAndScaleExperiment2ExperimentRunner[D <% PairDetector, E <% Extractor[F], M <% Matcher[F], F](
    self: RotationAndScaleExperiment[D, E, M, F]) extends ExperimentRunner[WideBaselineExperimentResults[D, E, M, F]] {
    override def run = {
      val scaleTransform =
        AffineTransform.getScaleInstance(self.scaleFactor, self.scaleFactor)
      val rotationTransform =
        AffineTransform.getRotateInstance(self.theta)
      val similarityTransform = {
        val scaleClone = scaleTransform.clone.asInstanceOf[AffineTransform]
        scaleClone.concatenate(rotationTransform)
        scaleClone
      }

      val similarityOp = new AffineTransformOp(
        similarityTransform,
        AffineTransformOp.TYPE_BILINEAR)

      val groundTruth: Homography = {
        val affineMatrix = Array[Double]()
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

      //////////////////////

      val leftImage = ImageIO.read(self.imagePath)
      val rightImage = {
        similarityOp.filter(leftImage, null)
      }

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
