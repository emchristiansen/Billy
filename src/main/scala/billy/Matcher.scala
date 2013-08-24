package billy

import org.opencv.core.DMatch
import breeze.linalg.DenseMatrix

///////////////////////////////////////////////////////////

/**
 * Matches descriptors to descriptors.
 */
trait Matcher[F] {
  /**
   * The distance between two descriptors.
   */
  def distance: (F, F) => Double
  
  /**
   * The distances between the corresponding descriptors in the two
   * sequences.
   * 
   * The two sequences must be the same length. 
   */
  def matchCorresponding: (Seq[F], Seq[F]) => Seq[Double]
  
  /**
   * The distances between all the descriptors.
   * 
   * The distance at (l, r) is the distance between the descriptor in the
   * left list at index l, and the descriptor in the right list at index r.
   * The sequences can be different lengths.
   */
  def matchAll: (Seq[F], Seq[F]) => DenseMatrix[Double]
}
