package st.sparse.billy.detectors

import st.sparse.billy._

///////////////////////////////////////////////////////////

/**
 * A PairDetector which returns no more than a set number of keypoints.
 */
case class BoundedPairDetector[D <% Detector](
  threshold: Double,
  maxKeyPoints: Int,
  detector: D) extends PairDetector {
  override def detectPair = (homography, leftImage, rightImage) => {
    val pairDetector = PairDetector(threshold, detector)
    
    pairDetector.detectPair(
      homography,
      leftImage,
      rightImage).take(maxKeyPoints)
  }
}
