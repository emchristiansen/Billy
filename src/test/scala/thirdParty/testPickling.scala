package thirdParty

import scala.pickling._
import scala.pickling.json._
import org.junit.runner.RunWith
import nebula.ConfigMapFunSuite
import org.scalatest.WrapWith
import org.scalatest.ConfigMapWrapperSuite
import org.scalatest.junit.JUnitRunner
import nebula.InstantTest
import org.scalatest.FunSuite

///////////////////////////////////////////////////////////

object bar {
  val foo = 42
}

class TestPickling extends FunSuite {
  test("pickling on a primitive", InstantTest) {
    val obj = 2.0
    val pickle = obj.pickle
    assert(obj == pickle.unpickle[Double])
  }
  
  test("pickling an object", InstantTest) {    
    val pickle = bar.pickle
    assert(pickle.unpickle[bar.type] == bar)
  }
}