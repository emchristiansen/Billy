package billy.brown

import nebula._
import nebula.imageProcessing._
import nebula.util._
import billy._
import billy.brown._
import billy.mpie._
import billy.smallBaseline._
import billy.wideBaseline._
import billy.summary._
import billy.detectors._
import billy.extractors._
import billy.matchers._
import nebula._
import javax.imageio.ImageIO
import java.io.File
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import nebula.util._
import com.sun.xml.internal.bind.v2.model.runtime.RuntimeClassInfo
import billy.testing.BillyTestUtil

import scala.pickling._
import scala.pickling.json._

import org.scalatest.fixture
import nebula.testing._
import scalatestextra._

///////////////////////////////////////////////////////////

class TestBrown extends FunGeneratorConfigSuite {
//  ignore(
//    "loadPatchPairs should return reasonable pairs",
//    SlowTest,
//    InteractiveTest,
//    DatasetTest) {
//      val datasetName = "liberty"
//      val numMatches = 1000
//
//      val patchPairs = PatchPair.loadPatchPairs(
//        datasetName,
//        numMatches,
//        datasetRoot)
//
//      for ((patchPair, index) <- patchPairs.zipWithIndex) {
//        val sideBySide = GraphicsUtil.drawSideBySide(
//            patchPair.left.image, 
//            patchPair.right.image)
//        sideBySide.getGraphics.drawString(patchPair.corresponds.toString, 5, 20)
//
//        //      TestUtil.dumpImage(s"${index}", sideBySide)
//      }
//    }
//
//  ignore("ensure implicits are found") {
//    val experiment = BrownExperiment(
//      "liberty",
//      1000,
//      OpenCVExtractor.SIFT,
//      VectorMatcher.L2)
//
//    implicit val runtimeConfig: RuntimeConfig = BillyTestUtil.runtimeConfig
//
//    val results = experiment.run
//    val summary = results.to[ExperimentSummary]
//
//    //    Distributed.unsafeCapstone(experiment)
//  }
//
//  test("brown pickle", InstantTest) {
//    val experiment = BrownExperiment(
//      "liberty",
//      1000,
//      OpenCVExtractor.SIFT,
//      VectorMatcher.L2)
//  }
}