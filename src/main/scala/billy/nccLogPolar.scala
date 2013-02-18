package billy

import nebula._
import nebula.imageProcessing._
import nebula.util._

import billy._
import billy.brown._
import billy.mpie._
import billy.smallBaseline._
import billy.wideBaseline._
import billy.summary._

import java.awt.image.BufferedImage
import breeze.math._
import breeze.linalg._
import org.opencv.core._
import org.opencv.features2d._
import nebula.util._
import MathUtil._
import DenseMatrixUtil._

///////////////////////////////////////////////////////////

/**
 * The two values that characterize a 1D affine function.
 */
case class AffinePair(scale: Double, offset: Double) {
  requirey(scale > 0)
}

/**
 * Data needed to determine normalized dot product from dot product
 * of unnormalized vectors.
 */
case class NormalizationData(
  affinePair: AffinePair,
  // This is the sum of the elements of the normalized vector.
  elementSum: Double,
  size: Int)

/**
 * A mapping from scale levels. Scale levels must be a sequential and 
 * symmetric about zero.
 */
case class ScaleMap[A](map: Map[Int, A]) {
  val minIndex = map.keys.min
  val maxIndex = map.keys.max
  requirey(minIndex == -maxIndex)
  requirey(map.keys.toList.sorted == (minIndex to maxIndex))
  requirey(map.size % 2 == 1)
}

object ScaleMap {
  implicit def scaleMap2Map[A](self: ScaleMap[A]): Map[Int, A] = self.map
}

/**
 * The descriptor. Contains a Fourier-space version of the log polar
 * data as well as normalization data for each scale.
 */
case class NCCBlock(
  fourierData: DenseMatrix[Complex],
  scaleMap: ScaleMap[NormalizationData]) {
  asserty(fourierData.rows - 1 == scaleMap.map.size)
}

/**
 * The extractor.
 */
case class NCCLogPolarExtractor(extractor: LogPolarExtractor) {
  require(FFT.isPowerOf2(extractor.numScales))
  require(FFT.isPowerOf2(extractor.numAngles))
}

object NCCLogPolarExtractor {
  /**
   * Find the affine pair that normalizes this matrix.
   */
  def getAffinePair(descriptor: DenseMatrix[Int]): AffinePair = {
    requirey(descriptor.size > 1)

    val data = descriptor.data
    val offset = MathUtil.mean(data)
    val scale = MathUtil.l2Norm(data map (_ - offset))
    asserty(scale > 0)
    AffinePair(scale, offset)
  }

  /**
   * Get the normalization data for a matrix.
   */
  def getNormalizationData(descriptor: DenseMatrix[Int]): NormalizationData =
    NormalizationData(
      getAffinePair(descriptor),
      MathUtil.normalizeL2(descriptor.data).sum,
      descriptor.size)

  /**
   * Get the scale map for an entire log-polar pattern.
   */
  def getScaleMap(
    descriptor: DenseMatrix[Int]): ScaleMap[NormalizationData] = {
    requirey(descriptor.rows > 0)
    requirey(descriptor.cols > 1)

    val numScales = descriptor.rows

    val pairs = for (scaleOffset <- (-numScales + 1) to (numScales - 1)) yield {
      val start = math.max(scaleOffset, 0)
      val stop = math.min(numScales, scaleOffset + numScales)

      val roi = descriptor(start until stop, ::)
      (scaleOffset, getNormalizationData(copy(roi)))
    }

    ScaleMap(pairs.toMap)
  }

  /**
   * Get a descriptor from an entire log-polar pattern.
   */
  def getNCCBlock(samples: DenseMatrix[Int]): NCCBlock = {
    // We require the descriptor width and height each be a power of two.
    require(FFT.isPowerOf2(samples.rows))
    require(FFT.isPowerOf2(samples.cols))
    require(samples.cols > 1)

    val scaleMap = getScaleMap(samples)

    val fourierData = {
      val zeroPadding = DenseMatrix.zeros[Int](
        samples.rows,
        samples.cols)

      val padded = DenseMatrix.vertcat(samples, zeroPadding)

      FFT.fft2(padded mapValues (r => Complex(r, 0)))
    }

    NCCBlock(fourierData, scaleMap)
  }

  implicit class NCCLogPolarExtractor2Extractor(
    self: NCCLogPolarExtractor) extends SingleExtractor[NCCBlock] {
    override def extractSingle = (image: BufferedImage, keyPoint: KeyPoint) => {
      val samplesOption = self.extractor.extractSingle(image, keyPoint)

      for (samples <- samplesOption) yield {
        asserty(samples.rows == self.extractor.numScales)
        asserty(samples.cols == self.extractor.numAngles)

        getNCCBlock(samples)
      }
    }
  }
}

case class NCCLogPolarMatcher(
  rotationInvariant: Boolean,
  scaleSearchRadius: Int)

object NCCLogPolarMatcher {
  /**
   * Determine what the dot product would have been had the vectors been
   * normalized first.
   */
  def nccFromUnnormalized(
    leftData: NormalizationData,
    rightData: NormalizationData,
    unnormalizedInnerProduct: Double): Double = {
    requirey(leftData.size == rightData.size)

    // Suppose we observe the inner product between two vectors
    // (a_x * x + b_x) and (a_y * y + b_y), where x and y are normalized.
    // Note (a_x * x + b_x)^T (a_y * y + b_y) is
    // (a_x * x)^T (a_y * y) + a_y * b_x^T y + a_x * b_y^T x + b_x^T b_y.
    // Thus we can solve for the normalized dot product:
    // x^T y = ((a_x * x)^T (a_y * y) - a_y * b_x^T y - a_x * b_y^T x - b_x^T b_y) / (a_x * a_y).
    val aybxy =
      rightData.affinePair.scale *
        leftData.affinePair.offset *
        rightData.elementSum

    val axbyx =
      leftData.affinePair.scale *
        rightData.affinePair.offset *
        leftData.elementSum

    val bxby = leftData.size *
      leftData.affinePair.offset *
      rightData.affinePair.offset

    val numerator = unnormalizedInnerProduct - aybxy - axbyx - bxby
    val denominator = leftData.affinePair.scale * rightData.affinePair.scale
    asserty(denominator != 0)

    val correlation = numerator / denominator
    asserty(correlation <= 1 + implicitly[Epsilon])
    asserty(correlation >= -1 - implicitly[Epsilon])
    correlation
  }

  /**
   * The map of normalized correlations.
   */
  def getResponseMap(
    scaleSearchRadius: Int,
    leftBlock: NCCBlock,
    rightBlock: NCCBlock): DenseMatrix[Double] = {
    requirey(leftBlock.fourierData.rows == rightBlock.fourierData.rows)
    requirey(leftBlock.fourierData.cols == rightBlock.fourierData.cols)
    requirey(scaleSearchRadius < leftBlock.fourierData.rows)

    // TODO: Some weirdness here regarding flipping left and right.
    val correlation = FFT.correlationFromPreprocessed(
      rightBlock.fourierData,
      leftBlock.fourierData) mapValues MathUtil.complexToDouble

    // The normalized rows corresponding to each scale offset.
    val normalizedRows = for (scaleOffset <- (-scaleSearchRadius to scaleSearchRadius)) yield {
      val rowIndex = scaleOffset mod leftBlock.fourierData.rows
      val row = copy(correlation(rowIndex, ::))
      val normalized = row mapValues { correlation =>
        nccFromUnnormalized(
          leftBlock.scaleMap(scaleOffset),
          rightBlock.scaleMap(-scaleOffset),
          correlation)
      }
      normalized.toSeqSeq.flatten
    }

    normalizedRows.toMatrix
  }

  /**
   * Assuming the dot product is between two unit length vectors, find
   * their l2 distance.
   * Divides by sqrt(2) to undo a previous normalization.
   */
  def dotProductToL2DistanceUnnormalized(dotProduct: Double): Double =
    math.sqrt(2 - 2 * dotProduct) / math.sqrt(2)

  /**
   * The map of distances.
   */
  def responseMapToDistanceMap: DenseMatrix[Double] => DenseMatrix[Double] =
    responseMap => responseMap mapValues dotProductToL2DistanceUnnormalized

  def distanceHelper = (scaleSearchRadius: Int) =>
    (left: NCCBlock, right: NCCBlock) => {
      val responseMap = getResponseMap(scaleSearchRadius, left, right)
      val distanceMap = responseMapToDistanceMap(responseMap)
      distanceMap.min
    }

  implicit class NCCLogPolarMatcher2Matcher(
    self: NCCLogPolarMatcher) extends SingleMatcher[NCCBlock] {
    override def distance = {
      // TODO
      require(self.rotationInvariant == true)
      distanceHelper(self.scaleSearchRadius)
    }
  }
}