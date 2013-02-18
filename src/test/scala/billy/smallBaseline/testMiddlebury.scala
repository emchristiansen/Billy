package billy.smallBaseline

import nebula._
import nebula.imageProcessing._
import nebula.util._

import billy._
import billy.brown._
import billy.mpie._
import billy.smallBaseline._
import billy.wideBaseline._
import billy.summary._

import org.apache.commons.math3.linear.{ Array2DRowRealMatrix, ArrayRealVector }
import org.scalatest._
import nebula._
import org.scalacheck.Properties
import org.scalacheck.Prop._
import org.scalacheck._
import math._
import java.io.File
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

///////////////////////////////////////////////////////////

@RunWith(classOf[JUnitRunner])
@WrapWith(classOf[ConfigMapWrapperSuite])
class TestMiddlebury(val configMap: Map[String, Any]) extends ConfigMapFunSuite {
  test("construct FlowField from file", SlowTest, DatasetTest) {
    val file = datasetRoot + "middleburyImages/other-gt-flow/Dimetrodon/flow10.flo.txt" 
    FlowField(file)
  }

  test("construct SmallBaselinePair from file", FastTest, DatasetTest) {
    val rootDirectory = datasetRoot + "middleburyImages"
    SmallBaselinePair(rootDirectory, "Dimetrodon")
  }

  test("the distance from a FlowField to itself should be zero", FastTest, DatasetTest) {
    val flow = {
      val file = datasetRoot + "middleburyImages/other-gt-flow/Dimetrodon/flow10.flo.txt"
      FlowField(file)
    }
    
    asserty(flow.mse(flow).abs == 0)
  }
}