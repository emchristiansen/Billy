package billy.detectors

import billy._

///////////////////////////////////////////////////////////

/** A PairDetector which returns no more than a set number of keypoints.
 */
case class BoundedPairDetector[D <% PairDetector](
  pairDetector: D,
  maxKeyPoints: Int) extends PairDetector {
  override def detectPair = (homography, leftImage, rightImage) =>
    pairDetector.detectPair(
      homography,
      leftImage,
      rightImage).take(maxKeyPoints)
}
