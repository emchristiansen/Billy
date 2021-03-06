package st.sparse.billy.extractors

import st.sparse.billy._
import org.opencv.core.Mat
import org.opencv.core.MatOfKeyPoint
import org.opencv.features2d.DescriptorExtractor
import org.opencv.core.KeyPoint
import breeze.linalg.DenseMatrix
import breeze.linalg.DenseVector
import st.sparse.sundry._
import com.sksamuel.scrimage._


///////////////////////////////////////////////////////////

/**
 * Represents descriptor extraction algorithms available in OpenCV.
 */
object OpenCVExtractor {
  case object BRISK extends BooleanExtractorFromEnum(DescriptorExtractor.BRISK)
  case object FREAK extends BooleanExtractorFromEnum(DescriptorExtractor.FREAK)
  case object BRIEF extends BooleanExtractorFromEnum(DescriptorExtractor.BRIEF)
  case object ORB extends BooleanExtractorFromEnum(DescriptorExtractor.ORB)
  case object SIFT extends DoubleExtractorFromEnum(DescriptorExtractor.SIFT)
  case object SURF extends DoubleExtractorFromEnum(DescriptorExtractor.SURF)

  /**
   * An extractor backed by a call to OpenCV, which returns descriptors which
   * are vectors of Doubles.
   *
   * User must have called loadOpenCV before using this class.
   */
  class DoubleExtractorFromEnum(
    extractorType: Int) extends ExtractorSeveral[IndexedSeq[Double]] with Logging {
    override def extract = (image: Image, keyPoints: Seq[KeyPoint]) => {
      val extractor = DescriptorExtractor.create(extractorType)
      val imageMat = image.toMat
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
      // TODO: See if this is still an issue with newer versions of OpenCV.
      OpenCVExtractor.synchronized {
        extractor.compute(
          imageMat,
          markedKeyPointsMat,
          descriptor)
      }

      val descriptorsOption = descriptor.toMatrixDouble
      if (!descriptorsOption.isDefined) keyPoints.size times None
      else {
        val descriptors = descriptorsOption.get.toSeqSeq
        logger.debug(s"descriptors: $descriptors")

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

  /**
   * An extractor backed by a call to OpenCV, which returns descriptors which
   * are vectors of Booleans.
   *
   * User must have called loadOpenCV before using this class.
   */
  class BooleanExtractorFromEnum(
    extractorType: Int) extends ExtractorSeveral[IndexedSeq[Boolean]] with Logging {
    override def extract = (image, keyPoints) => {
      val doubles =
        new DoubleExtractorFromEnum(extractorType).extract(image, keyPoints)
      doubles.map(_.map(_.flatMap {
        x =>
          logger.trace(s"x: $x")
          
          // The bits are packed into unsigned bytes.
          assert(x >= 0 && x <= 255)
          assert(x.round == x)
          val bytesTail = Integer.toBinaryString(x.toInt).map(_ == '1')
          assert(bytesTail.size <= 8)
          val bytes = ((8 - bytesTail.size) times false) ++ bytesTail
          assert(bytes.size == 8)
          bytes
      }))
    }
  }
}
