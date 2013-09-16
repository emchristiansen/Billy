package billy.extractors

import billy._
import breeze.linalg._
import grizzled.math.stats
import scalatestextra._

////////////////////////////////////////////////

/**
 * An `Extractor` which returns a rank permutation.
 * 
 * This wraps around an existing `Extractor`, which must extract something
 * which can be sorted.
 */
case class RankAdapter[F: Ordering, C[F], E <% Extractor[C[F]]](
  extractor: E)(
    implicit toSeq: C[F] => IndexedSeq[F]) extends ExtractorSeveral[Permutation] {
  override def extract = (image, keyPoints) => {
    extractor.extract(image, keyPoints) map {
      _ map { container =>
        val unsorted = toSeq(container)
        val ordering = unsorted.zipWithIndex.sortBy(_._1).map(_._2)
        
        Permutation(ordering.zipWithIndex.sortBy(_._1).map(_._2))
      }
    }
  }
}