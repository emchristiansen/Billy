package st.sparse.billy.extractors

import st.sparse.billy._
import breeze.linalg.DenseMatrix

/**
 * An extractor produced by combining two existing extractors.
 * 
 * Each extraction succeeds only if both succeed.
 */
case class AndExtractor[E1 <% Extractor[F1], E2 <% Extractor[F2], F1, F2](
  extractor1: E1,
  extractor2: E2) extends ExtractorSeveral[(F1, F2)] {
  override def extract = (image, keyPoints) => {
    val descriptors1 = extractor1.extract(image, keyPoints)
    val descriptors2 = extractor2.extract(image, keyPoints)
    (descriptors1, descriptors2).zipped map {
      case ((Some(descriptor1), Some(descriptor2))) =>
        Some((descriptor1, descriptor2))
      case _ => None
    }
  }
}