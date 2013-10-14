package st.sparse.billy.detectors

import st.sparse.billy._

///////////////////////////////////////////////////////////

/** A Detector which returns no more than a set number of keypoints.
 */
case class BoundedDetector[D <% Detector](
  detector: D,
  maxKeyPoints: Int) extends Detector {
  override def detect = image => detector.detect(image).take(maxKeyPoints)
}
