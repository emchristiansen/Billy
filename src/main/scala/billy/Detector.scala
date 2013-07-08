package billy

import org.opencv.features2d.KeyPoint

import nebula.imageProcessing.Image

///////////////////////////////////////////////////////////

/**
 * Something which finds keypoints in a given image.
 */
trait Detector {
  def detect: Detector.DetectorAction
}

object Detector {
  type DetectorAction = Image => Seq[KeyPoint]
}