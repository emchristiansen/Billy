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

import java.awt.image.BufferedImage

import scala.reflect.runtime.universe

import org.opencv.core.MatOfKeyPoint
import org.opencv.features2d.FeatureDetector
import org.opencv.features2d.KeyPoint

import nebula._
import nebula.util.JSONUtil.singletonObject
import spray.json.DefaultJsonProtocol
import spray.json.JsonFormat
import util.Homography
import util.KeyPointUtil
import util.OpenCVUtil
import util.Util

///////////////////////////////////////////////////////////

object OpenCVDetector {
  object DENSE
  object FAST
  object BRISK
  object SIFT
  object SURF

  def detectorFromEnum(detectorType: Int): Detector = new Detector {
    override def detect = image => {
      val matImage = OpenCVUtil.bufferedImageToMat(image)
      val keyPoints = new MatOfKeyPoint
      FeatureDetector.create(detectorType).detect(matImage, keyPoints)
      keyPoints.toArray.sortBy(_.response).reverse
    }
  }

  implicit def openCVDetectorDense2Detector(self: DENSE.type) =
    detectorFromEnum(FeatureDetector.DENSE)
  implicit def openCVDetectorFast2Detector(self: FAST.type) =
    detectorFromEnum(FeatureDetector.FAST)
  implicit def openCVDetectorBrisk2Detector(self: BRISK.type) =
    detectorFromEnum(FeatureDetector.BRISK)
  implicit def openCVDetectorSift2Detector(self: SIFT.type) =
    detectorFromEnum(FeatureDetector.SIFT)
  implicit def openCVDetectorSurf2Detector(self: SURF.type) =
    detectorFromEnum(FeatureDetector.SURF)
    
  // This enumeration is necessary because Scala doesn't do deep searches for implicits.
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
}

trait OpenCVDetectorJsonProtocol extends DefaultJsonProtocol {
  implicit val openCVDetectorDenseJsonProtocol = singletonObject(OpenCVDetector.DENSE)
  implicit val openCVDetectorFastJsonProtocol = singletonObject(OpenCVDetector.FAST)
  implicit val openCVDetectorBriskJsonProtocol = singletonObject(OpenCVDetector.BRISK)
  implicit val openCVDetectorSiftJsonProtocol = singletonObject(OpenCVDetector.SIFT)
  implicit val openCVDetectorSurfJsonProtocol = singletonObject(OpenCVDetector.SURF)
}

object OpenCVDetectorJsonProtocol extends OpenCVDetectorJsonProtocol