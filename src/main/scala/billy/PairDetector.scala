package billy

import org.opencv.features2d.KeyPoint

import com.sksamuel.scrimage.Image

import nebula.util.Homography
import nebula.util.KeyPointUtil
import nebula.util.Util

///////////////////////////////////////////////////////////

/** Detects keypoints independently in each of a pair of images.
 *  
 *  Using a ground-truth homography providing the motion between the images,
 *  it discards points that are either detected in only one image or for which
 *  the correspondence is ambiguous.
 *  Correspondence ambiguity can arise when a point in one image
 *  could map to >= 2 points in the other image.
 */
trait PairDetector {
  type PairDetect = (Homography, Image, Image) => Seq[Tuple2[KeyPoint, KeyPoint]]  
  
  def detectPair: PairDetect
}

object PairDetector {
  def apply[D <% Detector](threshold: Double, detector: D) = new PairDetector {
    override def detectPair =
      (homography: Homography,
        leftImage: Image,
        rightImage: Image) => {
        val left = detector.detect(leftImage)
        val right = detector.detect(rightImage)

        // TODO: Move these util methods.
        Util.nearestUnderWarpRemoveDuplicates(
          threshold,
          homography,
          left,
          right).sortBy(KeyPointUtil.pairQuality).reverse
      }    
  }
}
