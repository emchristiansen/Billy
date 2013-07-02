package billy.detectors

import nebula._
import nebula.imageProcessing._
import nebula.util._

import billy._
import billy.brown._
import billy.mpie._
import billy.smallBaseline._
import billy.wideBaseline._
import billy.summary._

import scala.reflect.runtime.universe

import org.opencv.core.MatOfKeyPoint
import org.opencv.features2d.FeatureDetector
import org.opencv.features2d.KeyPoint

import nebula._
import nebula.util.JSONUtil.singletonObject
import spray.json._
import util.Homography
import util.KeyPointUtil
import util.OpenCVUtil
import util.Util

///////////////////////////////////////////////////////////

/**
 * Represents keypoint detection algorithms available in OpenCV.
 */
trait OpenCVDetector {
  object DENSE
  object FAST
  object BRISK
  object SIFT
  object SURF
  object ORB
}

/**
 * Views to Detector and PairDetector. The views to Detector are mixed
 * in here to ensure the Detector views have higher priority than the
 * PairDetector views.
 */
trait OpenCVDetector2Detector extends OpenCVDetector2PairDetector {
  /**
   * Turns an enum exposed by the OpenCV Java interface into a proper
   * extractor.
   * Returns the keypoints sorted by response value, with the best
   * keypoints first.
   */
  def detectorFromEnum(detectorType: Int): Detector = new Detector {
    override def detect = image => {
      val matImage = OpenCVUtil.bufferedImageToMat(image)
      val keyPoints = new MatOfKeyPoint
      FeatureDetector.create(detectorType).detect(matImage, keyPoints)
      val reversed = keyPoints.toArray.sortBy { e =>
        (e.response, e.pt.x, e.pt.y, e.size, e.angle, e.octave, e.class_id)
      }
      reversed.reverse
    }
  }

  implicit def openCVDetectorDense2Detector(self: OpenCVDetector.DENSE.type) =
    detectorFromEnum(FeatureDetector.DENSE)
  implicit def openCVDetectorFast2Detector(self: OpenCVDetector.FAST.type) =
    detectorFromEnum(FeatureDetector.FAST)
  implicit def openCVDetectorBrisk2Detector(self: OpenCVDetector.BRISK.type) =
    detectorFromEnum(FeatureDetector.BRISK)
  implicit def openCVDetectorSift2Detector(self: OpenCVDetector.SIFT.type) =
    detectorFromEnum(FeatureDetector.SIFT)
  implicit def openCVDetectorSurf2Detector(self: OpenCVDetector.SURF.type) =
    detectorFromEnum(FeatureDetector.SURF)
  implicit def openCVDetectorOrb2Detector(self: OpenCVDetector.ORB.type) =
    detectorFromEnum(FeatureDetector.ORB)
}

/**
 * Views to PairDetector.
 */
trait OpenCVDetector2PairDetector {
  // This enumeration is necessary because Scala doesn't do deep 
  // searches for implicits.
  implicit def openCVDetectorDense2PairDetector(
    self: OpenCVDetector.DENSE.type) = self.to[Detector].to[PairDetector]
  implicit def openCVDetectorFast2PairDetector(
    self: OpenCVDetector.FAST.type) = self.to[Detector].to[PairDetector]
  implicit def openCVDetectorBrisk2PairDetector(
    self: OpenCVDetector.BRISK.type) = self.to[Detector].to[PairDetector]
  implicit def openCVDetectorSift2PairDetector(
    self: OpenCVDetector.SIFT.type) = self.to[Detector].to[PairDetector]
  implicit def openCVDetectorSurf2PairDetector(
    self: OpenCVDetector.SURF.type) = self.to[Detector].to[PairDetector]
  implicit def openCVDetectorOrb2PairDetector(
    self: OpenCVDetector.ORB.type) = self.to[Detector].to[PairDetector]
}

/**
 * Implementations of JsonFormat.
 */
trait OpenCVDetectorJsonProtocol extends DefaultJsonProtocol {
  implicit val openCVDetectorDenseJsonProtocol =
    singletonObject(OpenCVDetector.DENSE)
  implicit val openCVDetectorFastJsonProtocol =
    singletonObject(OpenCVDetector.FAST)
  implicit val openCVDetectorBriskJsonProtocol =
    singletonObject(OpenCVDetector.BRISK)
  implicit val openCVDetectorSiftJsonProtocol =
    singletonObject(OpenCVDetector.SIFT)
  implicit val openCVDetectorSurfJsonProtocol =
    singletonObject(OpenCVDetector.SURF)
  implicit val openCVDetectorOrbJsonProtocol =
    singletonObject(OpenCVDetector.ORB)
}

object OpenCVDetector extends OpenCVDetector with OpenCVDetector2Detector with OpenCVDetectorJsonProtocol