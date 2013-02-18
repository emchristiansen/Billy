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

///////////////////////////////////////////////////////////

@RunWith(classOf[JUnitRunner])
@WrapWith(classOf[ConfigMapWrapperSuite])
class TestNCCLogPolarExtractor(
  val configMap: Map[String, Any]) extends ConfigMapFunSuite with GeneratorDrivenPropertyChecks with ShouldMatchers {
  import TestUtil._

  def affinePairHelper(descriptor: DenseMatrix[Int]) {
    val affinePair = NCCLogPolarExtractor.getAffinePair(descriptor)

    val normalized = normalize(descriptor)

    assertNear(
      descriptor mapValues (_.toDouble),
      normalized mapValues (_ * affinePair.scale + affinePair.offset))
  }

  test("getAffinePair on simple example", FastTest) {
    val descriptor = new DenseMatrix(
      2,
      Array(1, 2, 3, 4))

    affinePairHelper(descriptor)
  }

  test("getAffinePair", FastTest) {
    forAll(TestUtil.genPowerOfTwoMatrix[Double]) { matrixDouble =>
      whenever(matrixDouble.size > 1) {
        val descriptor = matrixDouble mapValues (_.toInt)

        affinePairHelper(descriptor)
      }
    }
  }

  test("getNCCBlock should be sane", FastTest) {
    forAll(TestUtil.genPowerOfTwoMatrix[Double]) { matrixDouble =>
      whenever(matrixDouble.cols > 1 && 
          matrixDouble.rows > 1 && 
          matrixDouble.size <= 64) {
        val descriptor = matrixDouble mapValues (_.toInt)

        val numScales = descriptor.rows
        val numAngles = descriptor.cols
        
        val block = NCCLogPolarExtractor.getNCCBlock(descriptor)
        
        asserty(block.fourierData.rows == 2 * numScales)
        asserty(block.fourierData.cols == numAngles)
        asserty(block.scaleMap.map.size == 2 * numScales - 1)
        
        val dotProduct = descriptor.toSeqSeq.flatten.map(x => x * x).sum
        val correlation = FFT.correlationFromPreprocessed(
           block.fourierData,
           block.fourierData) mapValues MathUtil.complexToDouble
           
        assertNear(dotProduct, correlation.max)
        asserty(correlation.argmax == (0, 0))
      }
    }
  }
}