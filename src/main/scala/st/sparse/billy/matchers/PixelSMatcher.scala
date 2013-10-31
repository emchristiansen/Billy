package st.sparse.billy.matchers

import st.sparse.billy._
import st.sparse.sundry._
import st.sparse.sundry.ExpectyOverrides._
import breeze.linalg._
import grizzled.math.stats

object PixelSMatcher extends MatcherSingle[(DenseMatrix[IndexedSeq[Int]], DenseMatrix[Double])] with Logging {
  override def distance = (left, right) => {
    val ((leftPixels, leftMask), (rightPixels, rightMask)) = (left, right)

    def normalizeL2Pixels(patch: DenseMatrix[IndexedSeq[Int]]): DenseMatrix[Double] = {
      val data = patch.toSeqSeq.flatten.flatten
      val mean = stats.mean(data: _*)
      val std = stats.sampleStdDev(data: _*)

      patch mapValues { element =>
        assert(element.size == 1)
        (element.head - mean) / std
      }
    }

    def normalizeL2Mask(
      patch: DenseMatrix[Double]): DenseMatrix[Double] = {
      val data = patch.toSeqSeq.flatten
      val mean = stats.mean(data: _*)
      val std = stats.sampleStdDev(data: _*)

      patch mapValues { element => (element - mean) / std }
    }

    val leftPixelsNormalized = normalizeL2Pixels(leftPixels)
    val rightPixelsNormalized = normalizeL2Pixels(rightPixels)

    val leftMaskNormalized = normalizeL2Mask(leftMask)
    val rightMaskNormalized = normalizeL2Mask(rightMask)

    val l2Matcher: Matcher[IndexedSeq[Double]] = VectorMatcher.L2

    val pixelsNCC = l2Matcher.distance(
      leftPixelsNormalized.toSeqSeq.flatten.toIndexedSeq,
      rightPixelsNormalized.toSeqSeq.flatten.toIndexedSeq)

    //    val maskNCC = l2Matcher.distance(
    //        leftMaskNormalized.toSeqSeq.flatten.toIndexedSeq,
    //        rightMaskNormalized.toSeqSeq)

    ???
  }
}