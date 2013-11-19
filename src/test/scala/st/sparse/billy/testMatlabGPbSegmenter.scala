package st.sparse.billy

import st.sparse.billy._
import st.sparse.billy.experiments._
import st.sparse.billy.detectors._
import st.sparse.billy.extractors._
import st.sparse.billy.matchers._
import org.junit.runner.RunWith
import scala.pickling._
import scala.pickling.binary._
import st.sparse.sundry._
import breeze.linalg._
import org.scalatest.junit.JUnitRunner

////////////////////////////////////////////////////////////////////////////////

@RunWith(classOf[JUnitRunner])
class TestMatlabGPbSegmenter extends FunGeneratorSuite with st.sparse.billy.MatlabTestUtil {
  val smallGoldfishGirl = goldfishGirl.scale(0.5)
  logImage("smallGoldfishGirl", smallGoldfishGirl)

  test("boundaries", SlowTest, MatlabTest) {
    val boundaries = MatlabGPbSegmenter.boundariesImageScaling(palmTree)

    logImage("boundaries", boundaries)
  }

  test("connected components", FastTest, MatlabTest) {
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

  test("image segments", SlowTest, MatlabTest) {
    val boundaries = MatlabGPbSegmenter.boundaries(smallGoldfishGirl)

    val segments = boundaries.mapValues(_ < 0.1)
    val labels = MatlabGPbSegmenter.connectedComponentsLabels(segments)

    val intImage = labels mapValues {
      case None => 0
      case Some(n) => n + 1
    }
    val segmentsImage = intImage.affineToUnitInterval.toImage

    logImage("segmentsImage", segmentsImage)
  }
}