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

import nebula._
import org.scalatest._
import javax.imageio.ImageIO
import java.io.File
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import nebula.util._
import billy.JsonProtocols._
import spray.json._
import com.sun.xml.internal.bind.v2.model.runtime.RuntimeClassInfo
import breeze.linalg._
import breeze.math._
import grizzled.math.stats
import org.scalacheck._
import org.scalatest.prop._
import org.scalatest._
import DenseMatrixUtil._
import org.opencv.features2d.{ DMatch, KeyPoint }

///////////////////////////////////////////////////////////

@RunWith(classOf[JUnitRunner])
@WrapWith(classOf[ConfigMapWrapperSuite])
class TestNCCLogPolarMatcher(
  val configMap: Map[String, Any]) extends ConfigMapFunSuite with GeneratorDrivenPropertyChecks with ShouldMatchers {
  import TestUtil._

  test("nccFromUnnormalized on no-op example", FastTest) {
    val unnormalizedInnerProduct = .7
    val leftData = NormalizationData(
      AffinePair(1, 0),
      3,
      5)

    val rightData = NormalizationData(
      AffinePair(1, 0),
      4,
      5)

    val ncc = NCCLogPolarMatcher.nccFromUnnormalized(
      leftData,
      rightData,
      unnormalizedInnerProduct)

    // Normalization shouldn't do anything in this case.
    assertNear(unnormalizedInnerProduct, ncc)
  }

  def correlationHelper(left: DenseMatrix[Int], right: DenseMatrix[Int]) {
    val unnormalizedCorrelation = FFT.correlateSameSize(
      left mapValues (_.toDouble) mapValues MathUtil.doubleToComplex,
      right mapValues (_.toDouble) mapValues MathUtil.doubleToComplex) mapValues MathUtil.complexToDouble

    val leftData = NCCLogPolarExtractor.getNormalizationData(left)
    val rightData = NCCLogPolarExtractor.getNormalizationData(right)
    val postNormalized = unnormalizedCorrelation mapValues { correlation =>
      NCCLogPolarMatcher.nccFromUnnormalized(
        leftData,
        rightData,
        correlation)
    }

    val ncc = {
      val leftNormalized = TestUtil.normalize(left)
      val rightNormalized = TestUtil.normalize(right)
      FFT.correlateSameSize(
        leftNormalized mapValues MathUtil.doubleToComplex,
        rightNormalized mapValues MathUtil.doubleToComplex) mapValues MathUtil.complexToDouble
    }

    assertNear(postNormalized, ncc)
  }

  test("nccFromUnnormalized on simple example", FastTest) {
    val left = new DenseMatrix(
      2,
      Array(1, 1, 2, 3))

    val right = new DenseMatrix(
      2,
      Array(-1, 5, 2, 1))

    correlationHelper(left, right)
  }

  test("nccFromUnnormalized", FastTest) {
    forAll(TestUtil.genPowerOfTwoMatrixPair[Double]) {
      case (left, right) => whenever(left.size > 1 && left.size <= 64) {
        correlationHelper(
          left mapValues (_.toInt),
          right mapValues (_.toInt))
      }
    }
  }

  def responseMapHelper(
    scaleSearchRadius: Int,
    leftSamples: DenseMatrix[Int],
    rightSamples: DenseMatrix[Int]) {
    val nccResponseMap = {
      val leftBlock = NCCLogPolarExtractor.getNCCBlock(leftSamples)
      val rightBlock = NCCLogPolarExtractor.getNCCBlock(rightSamples)
      NCCLogPolarMatcher.getResponseMap(
        scaleSearchRadius,
        leftBlock,
        rightBlock)
    }

    val nccDistanceMap =
      NCCLogPolarMatcher.responseMapToDistanceMap(nccResponseMap)

    val goldenResponseMap = {
      val matcher = LogPolarMatcher(
        PatchNormalizer.NCC,
        Matcher.L2,
        true,
        true,
        scaleSearchRadius)
      LogPolar.getResponseMapWrapper(matcher, leftSamples, rightSamples)
    }

    assertNear(nccDistanceMap, goldenResponseMap)
  }

  test("responseMapHelper on simple example", FastTest) {
    val left = new DenseMatrix(
      2,
      Array(1, 5, 3, 4, 5, -6, 7, -8))

    val right = new DenseMatrix(
      2,
      Array(-1, 2, -3, -4, 5, -2, -7, 1))

    //    val correlation = FFT.correlateSameSize(
    //        left mapValues (_.toDouble) mapValues MathUtil.doubleToComplex, 
    //        right mapValues (_.toDouble) mapValues MathUtil.doubleToComplex)
    //    println(correlation)

    responseMapHelper(1, left, right)
  }

  test("responseMapHelper", FastTest) {
    forAll(TestUtil.genPowerOfTwoMatrixPair[Double]) {
      case (left, right) => whenever(
        left.rows > 1 &&
          left.cols > 2 &&
          left.size <= 128) {
          val numScales = left.rows

          responseMapHelper(
            numScales - 1,
            left mapValues (_.toInt),
            right mapValues (_.toInt))
        }
    }
  }

  val image = ImageIO.read(new File(
    getClass.getResource("/iSpy.png").getFile).mustExist)

  val random = new scala.util.Random(0)

  def randomPoint(width: Int, height: Int, buffer: Int): KeyPoint = {
    val x = random.nextFloat * (width - 2 * buffer) + buffer
    val y = random.nextFloat * (height - 2 * buffer) + buffer
    //    KeyPointUtil(x, y)
    KeyPointUtil(x.floor + 0.5.toFloat, y.floor + 0.5.toFloat)
  }

  val genRandomPoint = Gen(_ => Some(randomPoint(
    image.getWidth,
    image.getHeight,
    100)))

  val genRandomPointPair = Gen(_ => Some((
    genRandomPoint.sample.get,
    genRandomPoint.sample.get)))

  test("NCCLogPolar must agree with LogPolar", FastTest) {
    implicit val generatorDrivenConfig =
      PropertyCheckConfig(minSuccessful = 5)
    forAll(genRandomPointPair) {
      case (left, right) =>
        val goldenExtractor = LogPolarExtractor(
          false,
          2,
          32,
          8,
          16,
          3,
          "Gray")

        val nccExtractor = NCCLogPolarExtractor(goldenExtractor)

        val goldenMatcher = LogPolarMatcher(
          PatchNormalizer.NCC,
          Matcher.L2,
          true,
          true,
          4)

        val nccMatcher = NCCLogPolarMatcher(
          true,
          4)

        def distance[E <% Extractor[F], M <% Matcher[F], F](
          extractor: E,
          matcher: M): Double = {
          val leftDescriptor = extractor.extractSingle(image, left).get
          val rightDescriptor = extractor.extractSingle(image, right).get

          matcher.distance(leftDescriptor, rightDescriptor)
        }

        val goldenDistance = distance(goldenExtractor, goldenMatcher)
        val nccDistance = distance(nccExtractor, nccMatcher)
        assertNear(goldenDistance, nccDistance)
    }
  }
}








