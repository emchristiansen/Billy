package st.sparse.billy.extractors

import st.sparse.billy._

///////////////////////////////////////////////////////////

trait ExtractorSeveral[F] extends Extractor[F] {
  final override def extractSingle = (image, keyPoint) =>
    extract(image, Seq(keyPoint)).head
}
