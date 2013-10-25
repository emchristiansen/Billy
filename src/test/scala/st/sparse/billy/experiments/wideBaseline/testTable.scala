package st.sparse.billy.experiments.wideBaseline

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
import breeze.linalg.DenseMatrix
import scala.reflect.ClassTag
import com.sksamuel.scrimage._
import org.opencv.core.KeyPoint
import java.io.File
import st.sparse.billy.internal._

////////////////////////////////////////////////////////////////////////////////

@RunWith(classOf[JUnitRunner])
class TestTable extends FunGeneratorSuite with st.sparse.billy.experiments.TestUtil with Logging {
  test("Oxford table", MediumTest) {
    val experimentsFAST = for (
      otherImage <- 1 to 4
    ) yield {
      Oxford(
        "boat",
        otherImage,
        10,
        DoublyBoundedPairDetector(2, 20, 100, OpenCVDetector.FAST),
        OpenCVExtractor.FREAK,
        VectorMatcher.L0): Experiment
    }

    val experimentsSIFT = for (
      otherImage <- 3 to 5
    ) yield {
      Oxford(
        "boat",
        otherImage,
        10,
        DoublyBoundedPairDetector(2, 20, 100, OpenCVDetector.SIFT),
        OpenCVExtractor.FREAK,
        VectorMatcher.L0): Experiment
    }

    val experiments = experimentsFAST ++ experimentsSIFT
    val results = experiments.map(_.run)

//    experiments.map(_.modelParametersString).foreach(println)
//    experiments.map(_.experimentParametersString)
//    results.map(_.recognitionRate)
    
    val table = Table(
      experiments zip results,
      (e: Experiment) => e.modelParametersString,
      (e: Experiment) => e.experimentParametersString,
      (r: Results) => r.recognitionRate.toString)

    logger.debug(s"table: $table")
    logger.debug(s"table.csv: ${table.csv}")
      
    //    val results = experiment.run
    //    for (index <- 0 until results.distances.rows) {
    //      assert(results.distances(index, index) == 0)
    //    }
    //
    //    val table = Table()
  }
}