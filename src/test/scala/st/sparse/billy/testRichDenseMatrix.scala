package st.sparse.billy

import org.scalatest.fixture
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalacheck.Gen
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import breeze.linalg.DenseMatrix
import st.sparse.sundry.FunGeneratorSuite
import st.sparse.sundry._

////////////////////////////////////////////////////////////////////////////////

@RunWith(classOf[JUnitRunner])
class TestRichDenseMatrix extends FunGeneratorSuite with st.sparse.billy.TestUtil {
  val image = palmTree

  val randomMatrix = DenseMatrix.tabulate[Double](100, 200) {
    case (y, x) => random.nextDouble
  }

  test("toSeqSeq", FastTest) {
    val seqSeq = randomMatrix.toSeqSeq

    randomMatrix foreachPair {
      case ((y, x), value) => assert(seqSeq(y)(x) == value)
    }
  }

  test("random toImage", FastTest) {
    val image = randomMatrix.toImage

    logImage("random", image)
  }

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

    val inpainted = RichDenseMatrix.inpaint(withMissing)

    logImage("missingAsZero", missingAsZero.toImage)
    logImage("inpainted", inpainted.toImage)
  }
}