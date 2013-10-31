package st.sparse.billy.matchers

import st.sparse.billy._
import st.sparse.sundry._
import breeze.linalg.DenseMatrix

///////////////////////////////////////////////

/**
 * A Matcher which only requires that distance be defined by the user.
 */
trait MatcherSingle[F] extends Matcher[F] {
  final override def matchCorresponding = (leftDescriptors, rightDescriptors) => {
    (leftDescriptors, rightDescriptors).zipped map distance
  }

  final override def matchAll = (leftDescriptors, rightDescriptors) => {
    // A stream in which each element is repeated before moving to the
    // next element.
    // For example [a, b, c] might become [a, a, b, b, c, c].
    val leftEachElementRepeats =
      leftDescriptors.toStream.flatMap(rightDescriptors.size times _)

    // A stream which is just |rightDescriptors| repeated.
    // For example [a, b, c] might become [a, b, c, a, b, c].
    val rightDescriptorsStreamRepeats = {
      val repeated = Stream.continually(rightDescriptors.toStream)
      repeated.take(leftDescriptors.size).flatten
    }

    val distances = (
      leftEachElementRepeats,
      rightDescriptorsStreamRepeats).zipped map distance

    // DenseMatrix stores data column-major.
    new DenseMatrix(rightDescriptors.size, distances.toArray).t
  }
}
