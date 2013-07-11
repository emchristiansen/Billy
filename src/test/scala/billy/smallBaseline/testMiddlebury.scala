package billy.smallBaseline

import nebula._
import nebula.imageProcessing._
import nebula.util._

import billy._
import billy.brown._

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
import scalatestextra._

///////////////////////////////////////////////////////////

@RunWith(classOf[JUnitRunner])
class TestMiddlebury extends FunGeneratorConfigSuite with DataTest {
  test("construct FlowField from file", SlowTest, DatasetTest) {
    implicit configMap =>
      val path = ExistingFile(
        dataRoot + "middleburyImages/other-gt-flow/Dimetrodon/flow10.flo.txt")
      FlowField(path)
  }

  test("construct SmallBaselinePair from file", FastTest, DatasetTest) {
    implicit configMap =>
      val directory = ExistingDirectory(dataRoot + "middleburyImages")
      SmallBaselinePair(directory, "Dimetrodon")
  }

  test("the distance from a FlowField to itself should be zero", FastTest, DatasetTest) {
    implicit configMap =>
      val flow = {
        val file = ExistingFile(
          dataRoot + "middleburyImages/other-gt-flow/Dimetrodon/flow10.flo.txt")
        FlowField(file)
      }

      assert(flow.mse(flow).abs == 0)
  }
}
