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
  test("random toImage", FastTest) {
    val matrix = DenseMatrix.tabulate[Double](100, 200) {
      case (y, x) => (new util.Random).nextDouble.abs % 1.0
    }
    
    val image = matrix.toImage
    
    image.write("/home/eric/Downloads/random.png")
  }
}