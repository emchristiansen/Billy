package st.sparse.billy.extractors

import st.sparse.billy._
import st.sparse.billy.experiments._
import st.sparse.billy.detectors._
import st.sparse.billy.extractors._
import st.sparse.billy.matchers._

import org.scalatest.fixture
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalacheck.Gen
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.pickling._
import scala.pickling.binary._
import st.sparse.sundry._
import breeze.linalg._
import scala.reflect.ClassTag

////////////////////////////////////////////////////////////////////////////////

@RunWith(classOf[JUnitRunner])
class TestMatlabGPbSegmenter extends FunGeneratorSuite with st.sparse.billy.TestUtil {
  val image = goldfishGirl.scale(0.5)
  
  test("boundaries", SlowTest) {
    val boundaries = MatlabGPbSegmenter.boundaries(image)
    assert(boundaries.min >= 0)
    assert(boundaries.max <= 1)
    
    println(boundaries.max)
    
    // TODO
    image.write("/home/eric//Downloads/resized.png")
    boundaries.toImage.write("/home/eric//Downloads/boundaries.png")
  }
}