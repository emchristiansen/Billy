package st.sparse.billy.detectors

import org.opencv.core.MatOfKeyPoint
import org.opencv.features2d.FeatureDetector

import st.sparse.billy.Detector
import st.sparse.billy._

///////////////////////////////////////////////////////////

/** Represents keypoint detection algorithms available in OpenCV.
 */
object OpenCVDetector {
  case object DENSE extends DetectorFromEnum(FeatureDetector.DENSE) 
  case object FAST extends DetectorFromEnum(FeatureDetector.FAST)
  case object BRISK extends DetectorFromEnum(FeatureDetector.BRISK)
  case object SIFT extends DetectorFromEnum(FeatureDetector.SIFT)
  case object SURF extends DetectorFromEnum(FeatureDetector.SURF)
  case object ORB extends DetectorFromEnum(FeatureDetector.ORB)
  
  class DetectorFromEnum(detectorType: Int) extends Detector {
    override def detect = image => {
      val keyPoints = new MatOfKeyPoint
      FeatureDetector.create(detectorType).detect(image.toMat, keyPoints)
      val reversed = keyPoints.toArray.sortBy { e =>
        (e.response, e.pt.x, e.pt.y, e.size, e.angle, e.octave, e.class_id)
      }
      reversed.reverse
    }
  }
}
