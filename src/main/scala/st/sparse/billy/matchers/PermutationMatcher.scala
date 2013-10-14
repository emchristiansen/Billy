package st.sparse.billy.matchers

import breeze.linalg._
import st.sparse.billy._

///////////////////////////////////////////////////////////

/**
 * Represents distances on permutations.
 * 
 */
object PermutationMatcher {
  /**
   * The Cayley distance.
   * 
   * See "Locally Uniform Comparison Image Descriptors",
   * Zieger et al., NIPS 2012.
   * http://books.nips.cc/papers/files/nips25/NIPS2012_0012.pdf
   */
  object Cayley extends MatcherSingle[Permutation] {
    override def distance = (left, right) => {
      left.data.size - right.compose(left.inverse).numCycles
    }
  }
}