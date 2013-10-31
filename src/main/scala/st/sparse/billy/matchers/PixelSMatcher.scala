package st.sparse.billy.matchers

import st.sparse.billy._
import st.sparse.sundry._
import st.sparse.sundry.ExpectyOverrides._
import breeze.linalg._
import grizzled.math.stats

case class PixelSMatcher(
  pixelsNCCWeight: Double,
  maskNCCWeight: Double) extends MatcherSingle[(DenseMatrix[IndexedSeq[Int]], DenseMatrix[Double])] with Logging {
  override def distance = (left, right) => {
    val ((leftPixelsSeq, leftMask), (rightPixelsSeq, rightMask)) = (left, right)
    val leftPixels = leftPixelsSeq mapValues { elements =>
      // We only support Gray at the moment.
      require(elements.size == 1)
      elements.head.toDouble
    }

    val rightPixels = rightPixelsSeq mapValues { elements =>
      require(elements.size == 1)
      elements.head.toDouble
    }

    def normalizeL2(
      patch: DenseMatrix[Double]): DenseMatrix[Double] = {
      val data = patch.toSeqSeq.flatten
      val mean = stats.mean(data: _*)
      val std = stats.sampleStdDev(data: _*)

      patch mapValues { element => (element - mean) / std }
    }

    val leftPixelsNormalized = normalizeL2(leftPixels)
    val rightPixelsNormalized = normalizeL2(rightPixels)

    val leftMaskNormalized = normalizeL2(leftMask)
    val rightMaskNormalized = normalizeL2(rightMask)

    val l2Matcher: Matcher[IndexedSeq[Double]] = VectorMatcher.L2

    val pixelsNCC = l2Matcher.distance(
      leftPixelsNormalized.toSeqSeq.flatten.toIndexedSeq,
      rightPixelsNormalized.toSeqSeq.flatten.toIndexedSeq)

    val maskNCC = l2Matcher.distance(
      leftMaskNormalized.toSeqSeq.flatten.toIndexedSeq,
      rightMaskNormalized.toSeqSeq.flatten.toIndexedSeq)

    ???
  }
}

object PixelSMatcher {
  def weightedMean(dataAndWeights: Seq[(Double, Double)]): Double = {
    val (_, weights) = dataAndWeights.unzip
    require(weights.min >= 0)

    dataAndWeights.map(pair => pair._1 * pair._2).sum / weights.sum
  }

  def weightedSTD(dataAndWeights: Seq[(Double, Double)]): Double = {
    val mean = weightedMean(dataAndWeights)

    val numerator = dataAndWeights map {
      case (datum, weight) => weight * math.pow(datum - mean, 2)
    } sum

    math.sqrt(numerator / dataAndWeights.map(_._2).sum)
  }
}