package billy.detectors

import billy.Detector

///////////////////////////////////////////////////////////

/**
 * A detector which returns no more than a set number of keypoints.
 */
case class BoundedDetector[D <% Detector](
  detector: D,
  maxKeyPoints: Int)

object BoundedDetector {
  implicit class ToDetector[D <% Detector](
    self: BoundedDetector[D]) extends Detector {
    override def detect =
      image => self.detector.detect(image).take(self.maxKeyPoints)
  }
}
