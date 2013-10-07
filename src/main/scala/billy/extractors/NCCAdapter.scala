package billy.extractors

import billy._
import breeze.linalg._
import grizzled.math.stats
import st.sparse.sundry._

////////////////////////////////////////////////

//case class VectorizeNormalizer[F, E <% Extractor[DenseMatrix[F]]](
//  extractor: E) extends ExtractorSeveral[IndexedSeq[F]] {
//  override def extract = (image, keyPoints) => {
//    extractor.extract(image, keyPoints) map {
//      _ map {
//        _.copy.data.toIndexedSeq
//      }
//    }
//  }
//}

/**
 * Normalizes vector-like descriptors so they have zero mean and unit norm.
 */
case class NCCAdapter[F <% Double, C[F], E <% Extractor[C[F]]](
  extractor: E)(
    implicit toSeq: C[F] => IndexedSeq[F]) extends ExtractorSeveral[IndexedSeq[Double]] {
  override def extract = (image, keyPoints) => {
    extractor.extract(image, keyPoints) map {
      _ map { container =>
        val unnormalized = toSeq(container).map(_.toDouble)

        val mean = stats.mean(unnormalized: _*)
        val centered = unnormalized.map(_ - mean)
        val norm = DenseVector(centered.toArray).norm(2)

        // If the standard deviation is low, merely center the data.  
        if (norm < 0.001) {
          assertNear(stats.mean(centered: _*), 0)
          centered
        } else {
          val normalized = centered.map(_ / norm)
          assertNear(stats.mean(normalized: _*), 0)
          assertNear(DenseVector(centered.toArray).norm(2), 1)
          normalized
        }
      }
    }
  }
}