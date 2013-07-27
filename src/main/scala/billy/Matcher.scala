package billy

import org.opencv.features2d.DMatch

///////////////////////////////////////////////////////////

/** Matches descriptors.
 *  
 *  Can either return the distance between a pair of descriptors, of match
 *  sets of descriptors to each other.
 */
trait Matcher[F] { 
  def distance: (F, F) => Double
  
  /** Matches two collections of descriptors against each other, and returns
   *  the results as a collection of DMatch.
   *  
   *  If the first argument is true, all pairs of descriptors are matched.
   *  Otherwise, only descriptors with corresponding sequence indices are
   *  matched.
   *  In this case, the sequences must be the same length.
   */
  def doMatch: (Boolean, Seq[F], Seq[F]) => Seq[DMatch]
}
