package st.sparse.billy.extractors

import st.sparse.billy._
import st.sparse.billy.internal._
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

  ignore("boundaries", SlowTest) {
    val boundaries = MatlabGPbSegmenter.boundaries(image)
    assert(boundaries.min >= 0)
    assert(boundaries.max <= 1)

    // TODO
    image.write("/home/eric//Downloads/resized.png")
    boundaries.toImage.write("/home/eric//Downloads/boundaries.png")
  }

  test("connected components", FastTest) {
    val denseMatrix = Seq(
      Seq(true, true, false, true),
      Seq(true, false, false, true),
      Seq(false, true, false, false)).toDenseMatrix

    val labels = Seq(
      Seq(Some(0), Some(0), None, Some(1)),
      Seq(Some(0), None, None, Some(1)),
      Seq(None, Some(2), None, None)).toDenseMatrix
      
    assert(MatlabGPbSegmenter.connectedComponentsLabels(denseMatrix) == labels)
  }
  
  test("image segments", SlowTest) {
    val boundaries = MatlabGPbSegmenter.boundaries(goldfishGirl.scale(0.3))
    
    val segments = boundaries.mapValues(_ < 0.1)
    val labels = MatlabGPbSegmenter.connectedComponentsLabels(segments)
    
    val intImage = labels mapValues {
      case None => 0
      case Some(n) => n + 1
    }
    val image = intImage.affineToUnitInterval.toImage
    
    boundaries.toImage.write("/home/eric//Downloads/boundaries1.png")
    image.write("/home/eric//Downloads/segments1.png")
  }
}