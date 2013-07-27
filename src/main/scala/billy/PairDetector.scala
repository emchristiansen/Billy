package billy

import org.opencv.features2d.KeyPoint

import com.sksamuel.scrimage.Image

import nebula.util.Homography
import nebula.util.KeyPointUtil
import nebula.util.Util

///////////////////////////////////////////////////////////

/**
 * A detector which detects keypoints independently in each of a pair of images.
 * Using a ground-truth homography providing the motion between the images,
 * it discards points that are either detected in only one image or for which
 * the correspondence is ambiguous.
 * Correspondence ambiguity can arise when a point in one image
 * could map to 2+ points in the other image.
 */
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
