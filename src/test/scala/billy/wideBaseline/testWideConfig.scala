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
import billy.detectors._
import billy.extractors._
import billy.matchers._
import nebula._
import org.scalatest.FunSuite
import javax.imageio.ImageIO
import java.io.File
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import nebula.util._
import scalatestextra._
import billy.testing._

///////////////////////////////////////////////////////////

@RunWith(classOf[JUnitRunner])
class TestWideConfig extends FunGeneratorConfigSuite with RuntimeConfigTest {
  ignore("ensure implicits are found") { implicit configMap =>
    val experiment = WideBaselineExperiment(
      "wall",
      2,
      OpenCVDetector.FAST,
      OpenCVExtractor.SIFT,
      VectorMatcher.L2)
    
    val results = experiment.run
    val summary = results.to[ExperimentSummary]
  }
}