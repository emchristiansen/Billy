package billy.extractors

import billy._
import breeze.linalg._
import grizzled.math.stats
import scalatestextra._

////////////////////////////////////////////////

/**
 * Returns the ordering PermutationDescriptor.
 */
case class OrderNormalizer[F: Ordering, C[F], E <% Extractor[C[F]]](
  extractor: E)(
    implicit toSeq: C[F] => IndexedSeq[F]) extends ExtractorSeveral[PermutationDescriptor] {
  override def extract = (image, keyPoints) => {
    extractor.extract(image, keyPoints) map {
      _ map { container =>
        val unsorted = toSeq(container)
        
        PermutationDescriptor(unsorted.zipWithIndex.sortBy(_._1).map(_._2))
      }
    }
  }
}