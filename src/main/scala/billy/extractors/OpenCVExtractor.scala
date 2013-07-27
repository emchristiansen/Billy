package billy.extractors

import nebula._
import nebula.imageProcessing._
import nebula.util._

import billy._

import nebula._
import org.opencv.core.Mat
import org.opencv.core.MatOfKeyPoint
import org.opencv.features2d.DescriptorExtractor
import org.opencv.features2d.KeyPoint

import breeze.linalg.DenseMatrix
import breeze.linalg.DenseVector

import util.OpenCVUtil
import util.Util
import nebula.util._
import com.sksamuel.scrimage._

///////////////////////////////////////////////////////////

/**
 * Represents descriptor extraction algorithms available in OpenCV.
 */
object OpenCVExtractor {
  object BRISK extends BooleanExtractorFromEnum(DescriptorExtractor.BRISK)
  object FREAK extends BooleanExtractorFromEnum(DescriptorExtractor.FREAK)
  object BRIEF extends BooleanExtractorFromEnum(DescriptorExtractor.BRIEF)
  object ORB extends BooleanExtractorFromEnum(DescriptorExtractor.ORB)
  object SIFT extends DoubleExtractorFromEnum(DescriptorExtractor.SIFT)
  object SURF extends DoubleExtractorFromEnum(DescriptorExtractor.SURF)

  class DoubleExtractorFromEnum(
    extractorType: Int) extends ExtractSeveral[IndexedSeq[Double]] {
    override def extract = (image: Image, keyPoints: Seq[KeyPoint]) => {
      val extractor = DescriptorExtractor.create(extractorType)
      val imageMat = OpenCVUtil.bufferedImageToMat(image.awt)
      val descriptor = new Mat

      val markedKeyPointsMat = {
        val marked =
          for ((keyPoint, index) <- keyPoints.zipWithIndex) yield {
            new KeyPoint(
              keyPoint.pt.x.toFloat,
              keyPoint.pt.y.toFloat,
              keyPoint.size,
              keyPoint.angle,
              keyPoint.response,
              keyPoint.octave,
              index)
          }
        new MatOfKeyPoint(marked: _*)
      }

      // Apparent concurrency bug that causes random crashes. Sigh.
      //      OpenCVLock.synchronized {
      extractor.compute(
        imageMat,
        markedKeyPointsMat,
        descriptor)
      //      }

      val descriptorsOption =
        DenseMatrixUtil.matToMatrixDoubleSingleChannel(descriptor)
      if (!descriptorsOption.isDefined) keyPoints.size times None
      else {
        val descriptors: IndexedSeq[IndexedSeq[Double]] = ???
        //        descriptorsOption.get.toSeqSeq
        val markedKeyPoints = markedKeyPointsMat.toArray

        assert(descriptors.size == markedKeyPoints.size)

        val descriptorOptions =
          Array[Option[IndexedSeq[Double]]](keyPoints.size times None: _*)
        for ((descriptor, keyPoint) <- descriptors.zip(markedKeyPoints)) {
          val index = keyPoint.class_id
          descriptorOptions(index) = Some(descriptor)
        }
        descriptorOptions
      }
    }
  }

  class BooleanExtractorFromEnum(
    extractorType: Int) extends ExtractSeveral[IndexedSeq[Boolean]] {
    override def extract = (image, keyPoints) => {
      val doubles =
        new DoubleExtractorFromEnum(extractorType).extract(image, keyPoints)
      doubles.map(_.map(_.map {
        x =>
          assert(x == 0 || x == 1)
          if (x == 0) false
          else true        
      }))
    }
  }
}
