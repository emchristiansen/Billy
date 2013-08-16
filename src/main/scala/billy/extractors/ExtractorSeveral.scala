package billy.extractors

import billy._

///////////////////////////////////////////////////////////

trait ExtractSeveral[F] extends Extractor[F] {
  override def extractSingle = (image, keyPoint) =>
    extract(image, Seq(keyPoint)).head
}