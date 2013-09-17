package thirdParty

import org.scalatest.fixture
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalacheck.Gen
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.pickling._
import scala.pickling.binary._

import scalatestextra._

////////////////////////////////////////////////////////////////////////////////

object bar {
  val foo = 42
}

@RunWith(classOf[JUnitRunner])
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
