package st.sparse.billy.matchers

import st.sparse.sundry._

object BRIEFSMatcher extends MatcherSingle[IndexedSeq[(Boolean, Option[Double], Option[Double])]] with Logging {
  override def distance = (left, right) => {
    require(left.size == right.size)

    def booleanToInt(boolean: Boolean): Int = boolean match {
      case false => 0
      case true => 1
    }

    // We ignore all comparisons where segmentation information is unknown.    
    val flattened = for (
      ((
        leftComparison,
        Some(leftLeftProbability),
        Some(leftRightProbability)),
        (
          rightComparison,
          Some(rightLeftProbability),
          Some(rightRightProbability))) <- left.zip(right)
    ) yield {
      // Directly compare pixels, ignoring foreground mask.
      val briefError = booleanToInt(leftComparison != rightComparison)

      // Directly compare the probabilities pixels are in the foreground.
      val shapeError = {
        val leftShapeComparison = leftLeftProbability < leftRightProbability
        val rightShapeComparison = rightLeftProbability < rightRightProbability
        booleanToInt(leftShapeComparison != rightShapeComparison)
      }

      // Compare pixels in the intersection of the foreground masks.
      val weightedBRIEFError = {
        val probabilityAllPointsInForeground =
          leftLeftProbability *
            leftRightProbability *
            rightLeftProbability *
            rightRightProbability

        probabilityAllPointsInForeground * briefError
      }

      // TODO: Finish
      
      ((leftComparison, leftLeftProbability, leftRightProbability),
        (rightComparison, rightLeftProbability, rightRightProbability))
    }

    logger.debug(s"Original size: ${left.size}, flat size: ${flattened.size}")

    ???
  }
}