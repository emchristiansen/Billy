package billy

import org.opencv.features2d.KeyPoint

import com.sksamuel.scrimage.Image

///////////////////////////////////////////////////////////

/** Finds keypoints in a given image.
 */
trait Detector {
  type Detect = Image => Seq[KeyPoint]
  
  def detect: Detect
}
