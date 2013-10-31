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
class TestRichSeqSeq extends FunGeneratorSuite with st.sparse.billy.TestUtil {
  val randomSeqSeq = DenseMatrix.tabulate[Double](100, 200) {
    case (y, x) => random.nextDouble
  } toSeqSeq

  test("toDenseMatrix", FastTest) {
    val randomMatrix = randomSeqSeq.toDenseMatrix
    
    randomMatrix foreachPair {
      case ((y, x), value) => assert(randomSeqSeq(y)(x) == value)
    }
  }
}