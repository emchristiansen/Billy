package thirdParty

import scala.pickling._
import scala.pickling.json._
import org.junit.runner.RunWith
import org.scalatest.WrapWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.scalatest.ConfigMap

import nebula.testing._

import scalatestextra._
import org.scalatest._

///////////////////////////////////////////////////////////

object bar {
  val foo = 42
}

class TestPickling extends FunGeneratorSuite {
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
