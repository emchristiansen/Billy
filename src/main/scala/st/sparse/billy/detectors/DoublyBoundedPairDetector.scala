package st.sparse.billy.detectors

import st.sparse.billy._

///////////////////////////////////////////////////////////

/**
 * A PairDetector which returns no more than a set number of keypoints.
 * 
 * For efficiency, it requires you to set a bound on the number of keypoints
 * that can be detected individually in each image.
 * For a decent detector, you might set `individualMaxKeyPoints` to 
 * `4 * pairMaxKeyPoints`, but this depends on the detectors repeatability.
 */
case class DoublyBoundedPairDetector[D <% Detector](
  threshold: Double,
  pairMaxKeyPoints: Int,
  individualMaxKeyPoints: Int,
  detector: D) extends PairDetector {
  override def detectPair = {
    val boundedDetector = BoundedDetector(individualMaxKeyPoints, detector)
    BoundedPairDetector(threshold, pairMaxKeyPoints, boundedDetector).detectPair
  }
}