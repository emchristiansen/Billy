package billy

import org.opencv.features2d.KeyPoint

import com.sksamuel.scrimage.Image

///////////////////////////////////////////////////////////

trait Extractor[F] {
  def extract: Extractor.ExtractorAction[F]

  def extractSingle: Extractor.ExtractorActionSingle[F]
}

///////////////////////////////////////////////////////////

object Extractor {
  type ExtractorAction[F] = (Image, Seq[KeyPoint]) => Seq[Option[F]]
  type ExtractorActionSingle[F] = (Image, KeyPoint) => Option[F]
}
