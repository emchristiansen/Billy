package billy.summary

import nebula._
import nebula.imageProcessing._
import nebula.util._

import billy._
import billy.brown._

import billy.smallBaseline._
import billy.wideBaseline._
import billy.summary._

import org.scalatest.FunSuite

import org.scalatest.FunSuite
import org.opencv.features2d._
import javax.imageio.ImageIO
import java.io.File
import org.opencv.core.MatOfKeyPoint
import org.opencv.features2d.{ FeatureDetector, KeyPoint }
import nebula._
import org.opencv.core.Mat
import org.opencv.core.MatOfKeyPoint



import nebula.util.Memoize._
import nebula.util._

import breeze.linalg._



import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import org.scalatest.FunSuite
import org.opencv.features2d._
import javax.imageio.ImageIO
import java.io.File
import org.opencv.core.MatOfKeyPoint
import org.opencv.features2d.{ FeatureDetector, KeyPoint }
import nebula._
import org.opencv.core.Mat
import java.awt.Color
import org.apache.commons.math3.linear.Array2DRowRealMatrix
import nebula.util.Homography
import nebula.util.OpenCVUtil
import nebula.util.KeyPointUtil

import javax.imageio.ImageIO

import java.awt.{ Color, Rectangle }
import java.awt.color.ColorSpace
import java.awt.geom.AffineTransform
import java.awt.image.{ AffineTransformOp, BufferedImage, ColorConvertOp, ConvolveOp, DataBufferInt, Kernel }

import scala.Array.{ canBuildFrom, fallbackCanBuildFrom }

import org.opencv.features2d.KeyPoint

import java.awt.image.AffineTransformOp.TYPE_BILINEAR

import breeze.linalg.DenseMatrix

import org.opencv.features2d.{ DMatch, KeyPoint }

import DenseMatrixUtil._
import scalatestextra._

///////////////////////////////////////////////////////////

@RunWith(classOf[JUnitRunner])
class TestSummary extends FunSuite { 
  test("mapUnion", InstantTest) {
    val map1 = Map(1 -> 12, 2 -> 13)
    val map2 = Map(1 -> 10, 3 -> 10, 2 -> 13)
    val maps = Set(map1, map2)

    val union = SummaryUtil.mapUnion(maps)
    val golden = Map(1 -> Set(12, 10), 2 -> Set(13), 3 -> Set(10))

    assert(union == golden)
  }

  test("changingFields", InstantTest) {
    val map1 = Map(1 -> 10, 2 -> 20)
    val map2 = Map(1 -> 10, 2 -> 30)
    val maps = Seq(map1, map2)

    val changing = SummaryUtil.changingFields(maps)
    val golden = Seq(Map(2 -> 20), Map(2 -> 30))
    assert(changing == golden)
  }

  test("summarizeStructure", InstantTest) {
    val map1 = Map("a" -> "aa", "b" -> "bb")
    val map2 = Map("a" -> "aa", "b" -> "cc")
    val summary = SummaryUtil.summarizeStructure(Set(map1, map2))
    val golden = "a-aa_b-*"
    assert(summary == golden)
  }
  
  test("errorRateAtRecall", InstantTest) {
    val dmatch0 = new DMatch(0, 0, 10)
    val dmatch1 = new DMatch(1, 2, 11)
    val dmatch2 = new DMatch(3, 3, 12)
    val dmatch3 = new DMatch(4, 5, 13)
    val dmatch4 = new DMatch(2, 1, 14)
    val dmatch5 = new DMatch(0, 0, 15)
    
    val dmatches = Seq(
      dmatch1,
      dmatch4,
      dmatch3,
      dmatch2,
      dmatch5,
      dmatch0)
      
    assert(SummaryUtil.errorRateAtRecall(0, dmatches) == 3 / 6.0)
    assert(SummaryUtil.errorRateAtRecall(0.1, dmatches) == 2 / 6.0)
    assert(SummaryUtil.errorRateAtRecall(0.4, dmatches) == 2 / 6.0)
    assert(SummaryUtil.errorRateAtRecall(0.5, dmatches) == 2 / 6.0)
    assert(SummaryUtil.errorRateAtRecall(0.7, dmatches) == 3 / 6.0)
    assert(SummaryUtil.errorRateAtRecall(0.9, dmatches) == 3 / 6.0)
    assert(SummaryUtil.errorRateAtRecall(1, dmatches) == 3 / 6.0)
  }
}
