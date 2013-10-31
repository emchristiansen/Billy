package st.sparse.billy.matchers

import st.sparse.billy._
import st.sparse.sundry._
import st.sparse.sundry.ExpectyOverrides._
import breeze.linalg._
import grizzled.math.stats

case class PixelSMatcher(
  pixelsNCCWeight: Double,
  maskNCCWeight: Double,
  pixelsWeightedNCCWeight: Double,
  maskWeightedNCCWeight: Double) extends MatcherSingle[(DenseMatrix[IndexedSeq[Int]], DenseMatrix[Double])] with Logging {
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

    def normalizeL2Weighted(
      patch: DenseMatrix[Double],
      weightsMatrix: DenseMatrix[Double]): DenseMatrix[Double] = {
      require(patch.rows == weightsMatrix.rows)
      require(patch.cols == weightsMatrix.cols)

      val data = patch.toSeqSeq.flatten
      val weights = weightsMatrix.toSeqSeq.flatten
      val dataAndWeights = data.zip(weights)

      val mean = PixelSMatcher.weightedMean(dataAndWeights)
      val std = PixelSMatcher.weightedSTD(dataAndWeights)

      patch mapValues { element => (element - mean) / std }
    }

    def normalizeL2(patch: DenseMatrix[Double]): DenseMatrix[Double] =
      normalizeL2Weighted(
        patch,
        DenseMatrix.ones[Double](patch.rows, patch.cols))

    // Regular l2 normalized pixels.
    val leftPixelsNormalized = normalizeL2(leftPixels)
    val rightPixelsNormalized = normalizeL2(rightPixels)

    // L2 normalized mask probabilities.
    val leftMaskNormalized = normalizeL2(leftMask)
    val rightMaskNormalized = normalizeL2(rightMask)

    val l2Matcher: Matcher[IndexedSeq[Double]] = VectorMatcher.L2

    // Standard NCC on the pixels.
    val pixelsNCC = l2Matcher.distance(
      leftPixelsNormalized.toSeqSeq.flatten.toIndexedSeq,
      rightPixelsNormalized.toSeqSeq.flatten.toIndexedSeq)

    // Standard NCC on the foreground mask.
    val maskNCC = l2Matcher.distance(
      leftMaskNormalized.toSeqSeq.flatten.toIndexedSeq,
      rightMaskNormalized.toSeqSeq.flatten.toIndexedSeq)

    // NCC on the pixels, weighted by the probability the pixel is in the 
    // foreground of both patches.
    val pixelsWeightedNCC = {
      val weights = {
        val left = leftMask.toSeqSeq.flatten
        val right = rightMask.toSeqSeq.flatten
        (left, right).zipped.map(_ * _).grouped(leftMask.cols).
          toIndexedSeq.toDenseMatrix
      }

      val leftPixelsWeightNormalized =
        normalizeL2Weighted(leftPixels, weights)
      val rightPixelsWeightNormalized =
        normalizeL2Weighted(rightPixels, weights)

      val sqrtWeights = weights mapValues (math.sqrt)

      val unnormalizedDistance = l2Matcher.distance(
        (leftPixelsWeightNormalized.toSeqSeq.flatten,
          sqrtWeights.toSeqSeq.flatten).zipped.map(_ * _),
        (rightPixelsWeightNormalized.toSeqSeq.flatten,
          sqrtWeights.toSeqSeq.flatten).zipped.map(_ * _))

      unnormalizedDistance / weights.sum
    }

    // NCC on the masks, weighted by the probability the point is in the
    // foreground of at least one of the patches.
    val maskWeightedNCC = {
      val weights = {
        val left = leftMask.toSeqSeq.flatten
        val right = rightMask.toSeqSeq.flatten
        val orProbabilities = (left, right).zipped.map {
          case (l, r) =>
            // The chance the point is in at least one foreground.
            1 - (1 - l) * (1 - r)
        }
        orProbabilities.grouped(leftMask.cols).toIndexedSeq.toDenseMatrix
      }

      val leftMaskWeightNormalized =
        normalizeL2Weighted(leftMask, weights)
      val rightMaskWeightNormalized =
        normalizeL2Weighted(rightMask, weights)

      val sqrtWeights = weights mapValues (math.sqrt)

      val unnormalizedDistance = l2Matcher.distance(
        (leftMaskWeightNormalized.toSeqSeq.flatten,
          sqrtWeights.toSeqSeq.flatten).zipped.map(_ * _),
        (rightMaskWeightNormalized.toSeqSeq.flatten,
          sqrtWeights.toSeqSeq.flatten).zipped.map(_ * _))

      unnormalizedDistance / weights.sum
    }

    pixelsNCCWeight * pixelsNCC +
      maskNCCWeight * maskNCC +
      pixelsWeightedNCCWeight * pixelsWeightedNCC +
      maskWeightedNCCWeight * maskWeightedNCC
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