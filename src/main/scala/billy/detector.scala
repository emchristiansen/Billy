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

import scala.reflect.runtime.universe

import org.opencv.core.MatOfKeyPoint
import org.opencv.features2d.FeatureDetector
import org.opencv.features2d.KeyPoint

import nebula._
import util.Homography
import util.KeyPointUtil
import util.OpenCVUtil
import util.Util

///////////////////////////////////////////////////////////

trait Detector {
  def detect: Detector.DetectorAction
}

object Detector {
  type DetectorAction = Image => Seq[KeyPoint]
}

///////////////////////////////////////////////////////////

case class BoundedDetector[D <% Detector](detector: D, maxKeyPoints: Int)

object BoundedDetector {
  implicit class ToDetector[D <% Detector](self: BoundedDetector[D]) extends Detector {
    override def detect = image => self.detector.detect(image).take(self.maxKeyPoints)
  }
}

///////////////////////////////////////////////////////////

trait PairDetector extends Detector {
  def detectPair: PairDetector.PairDetectorAction
}

object PairDetector {
  type PairDetectorAction = (Homography, Image, Image) => Seq[Tuple2[KeyPoint, KeyPoint]]

  implicit class ToPairDetector[D <% Detector](self: D) extends PairDetector {
    override def detect = self.detect

    override def detectPair =
      (homography: Homography,
        leftImage: Image,
        rightImage: Image) => {
        val left = detect(leftImage)
        val right = detect(rightImage)

        // Euclidean distance in pixels.
        // TODO: Make parameter
        val threshold = 2

        Util.nearestUnderWarpRemoveDuplicates(
          threshold,
          homography,
          left,
          right).sortBy(KeyPointUtil.pairQuality).reverse
      }
  }
}

///////////////////////////////////////////////////////////

case class BoundedPairDetector[D <% PairDetector](
  pairDetector: D,
  maxKeyPoints: Int)

object BoundedPairDetector {
  implicit class ToPairDetector[D <% PairDetector](
    self: BoundedPairDetector[D]) extends PairDetector {
    override def detect =
      image => self.pairDetector.detect(image).take(self.maxKeyPoints)

    override def detectPair = (homography, leftImage, rightImage) =>
      self.pairDetector.detectPair(
        homography,
        leftImage,
        rightImage).take(self.maxKeyPoints)
  }
}