package billy

import org.opencv.features2d.KeyPoint

import com.sksamuel.scrimage.Image

///////////////////////////////////////////////////////////

/** Extracts descriptors from an image at the provided keypoints.
 */
trait Extractor[F] {
  type Extract[F] = (Image, Seq[KeyPoint]) => Seq[Option[F]]
  def extract: Extract[F]

  type ExtractSingle[F] = (Image, KeyPoint) => Option[F]
  def extractSingle: ExtractSingle[F]
}
