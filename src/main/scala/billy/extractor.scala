package billy

import org.opencv.features2d.KeyPoint

import com.sksamuel.scrimage.Image

///////////////////////////////////////////////////////////

/** Extracts descriptors from an image at the provided keypoints.
 */
trait Extractor[F] {
  def extract: (Image, Seq[KeyPoint]) => Seq[Option[F]]

  def extractSingle: (Image, KeyPoint) => Option[F]
}
