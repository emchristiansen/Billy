package st.sparse.billy.extractors

import st.sparse.billy._

///////////////////////////////////////////////////////////

trait ExtractorSingle[F] extends Extractor[F] {
  final override def extract = (image, keyPoints) =>
    keyPoints.map(keyPoint => extractSingle(image, keyPoint))
}
