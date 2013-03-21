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

trait Detector {
  def detect: Detector.DetectorAction
}

object Detector {
  type DetectorAction = BufferedImage => Seq[KeyPoint]
}

///////////////////////////////////////////////////////////

case class BoundedDetector[D <% Detector](detector: D, maxKeyPoints: Int)

trait BoundedDetectorJsonProtocol extends DefaultJsonProtocol {
    implicit def boundedDetectorJsonProtocol[D <% Detector: JsonFormat] = 
    jsonFormat2(BoundedDetector.apply[D])
}

object BoundedDetector extends BoundedDetectorJsonProtocol {
  implicit class ToDetector[D <% Detector](self: BoundedDetector[D]) extends Detector {
    override def detect = image => self.detector.detect(image).take(self.maxKeyPoints)
  }
}

///////////////////////////////////////////////////////////

trait PairDetector extends Detector {
  def detectPair: PairDetector.PairDetectorAction
}

object PairDetector {
  type PairDetectorAction = (Homography, BufferedImage, BufferedImage) => Seq[Tuple2[KeyPoint, KeyPoint]]

  implicit class ToPairDetector[D <% Detector](self: D) extends PairDetector {
    override def detect = self.detect

    override def detectPair =
      (homography: Homography,
        leftImage: BufferedImage,
        rightImage: BufferedImage) => {
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

trait BoundedPairDetectorJsonProtocol extends DefaultJsonProtocol {
  implicit def boundedPairDetectorJsonProtocol[D <% PairDetector: JsonFormat] = 
    jsonFormat2(BoundedPairDetector.apply[D])  
}
  
object BoundedPairDetector extends BoundedPairDetectorJsonProtocol {
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

// TODO: Delete
object DetectorJsonProtocol