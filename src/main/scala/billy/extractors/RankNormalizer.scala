package billy.extractors

import billy._
import breeze.linalg._
import grizzled.math.stats
import scalatestextra._

////////////////////////////////////////////////

/**
 * Returns the ranking PermutationDescriptor.
 */
case class RankNormalizer[F: Ordering, C[F], E <% Extractor[C[F]]](
  extractor: E)(
    implicit toSeq: C[F] => IndexedSeq[F]) extends ExtractorSeveral[PermutationDescriptor] {
  override def extract = (image, keyPoints) => {
    extractor.extract(image, keyPoints) map {
      _ map { container =>
        val unsorted = toSeq(container)
        val ordering = unsorted.zipWithIndex.sortBy(_._1).map(_._2)
        
        PermutationDescriptor(ordering.zipWithIndex.sortBy(_._1).map(_._2))
      }
    }
  }
}