package billy.wideBaseline

import nebula._
import nebula.imageProcessing._
import nebula.util._

import billy._
import billy.brown._
import billy.mpie._
import billy.smallBaseline._
import billy.wideBaseline._
import billy.summary._

import nebula._
import org.scalatest.FunSuite
import javax.imageio.ImageIO
import java.io.File
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import nebula.util._
import billy.JsonProtocols._

///////////////////////////////////////////////////////////

@RunWith(classOf[JUnitRunner])
class TestWideConfig extends FunSuite {
  ignore("ensure implicits are found") {
    val experiment = WideBaselineExperiment(
      "wall",
      2,
      OpenCVDetector.FAST,
      OpenCVExtractor.SIFT,
      Matcher.L2)
      
    implicit val runtimeConfig = BillyTestUtil.runtimeConfig
    
    val results = experiment.run
    val summary = results.to[ExperimentSummary]
    
    Distributed.unsafeCapstone(experiment)
  }
}