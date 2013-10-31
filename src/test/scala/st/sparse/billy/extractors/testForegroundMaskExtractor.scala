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
import st.sparse.sundry.ExpectyOverrides._
import breeze.linalg._
import scala.reflect.ClassTag
import st.sparse.billy._
import java.io.File

////////////////////////////////////////////////////////////////////////////////

@RunWith(classOf[JUnitRunner])
class TestForegroundMaskExtractor extends FunGeneratorSuite with st.sparse.billy.TestUtil {
  val image = palmTree

  test("inpaint", FastTest) {
    val matrix = image.toGrayMatrix mapValues { _ / 255.0 }

    val withMissing = matrix mapValues { element =>
      if (random.nextDouble < 0.2) None
      else Some(element)
    }
    
    val missingAsZero = withMissing mapValues {
      case Some(element) => element
      case None => 0
    }
    
    val inpainted = ForegroundMaskExtractor.inpaint(withMissing)
    
    logImage("missingAsZero", missingAsZero.toImage)
    logImage("inpainted", inpainted.toImage)
  }
}