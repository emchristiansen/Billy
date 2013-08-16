package billy.extractors

import billy._

///////////////////////////////////////////////////////////

trait ExtractorSingle[F] extends Extractor[F] {
  override def extract = (image, keyPoints) =>
    keyPoints.map(keyPoint => extractSingle(image, keyPoint))
}