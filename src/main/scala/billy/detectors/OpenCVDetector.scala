package billy.detectors

import org.opencv.core.MatOfKeyPoint
import org.opencv.features2d.FeatureDetector

import billy.Detector
import nebula.util.OpenCVUtil

///////////////////////////////////////////////////////////

/** Represents keypoint detection algorithms available in OpenCV.
 */
object OpenCVDetector {
  object DENSE extends DetectorFromEnum(FeatureDetector.DENSE)
  object FAST extends DetectorFromEnum(FeatureDetector.FAST)
  object BRISK extends DetectorFromEnum(FeatureDetector.BRISK)
  object SIFT extends DetectorFromEnum(FeatureDetector.SIFT)
  object SURF extends DetectorFromEnum(FeatureDetector.SURF)
  object ORB extends DetectorFromEnum(FeatureDetector.ORB)
  
  class DetectorFromEnum(detectorType: Int) extends Detector {
    override def detect = image => {
      // TODO: Move util method.
      val matImage = OpenCVUtil.bufferedImageToMat(image.awt)
      val keyPoints = new MatOfKeyPoint
      FeatureDetector.create(detectorType).detect(matImage, keyPoints)
      val reversed = keyPoints.toArray.sortBy { e =>
        (e.response, e.pt.x, e.pt.y, e.size, e.angle, e.octave, e.class_id)
      }
      reversed.reverse
    }
  }
}
