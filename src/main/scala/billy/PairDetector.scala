package billy

import org.opencv.core.KeyPoint

import com.sksamuel.scrimage.Image


///////////////////////////////////////////////////////////

/**
 * Detects keypoints independently in each of a pair of images.
 *
 *  Using a ground-truth homography providing the motion between the images,
 *  it discards points that are either detected in only one image or for which
 *  the correspondence is ambiguous.
 *  Correspondence ambiguity can arise when a point in one image
 *  could map to >= 2 points in the other image.
 */
trait PairDetector {
  def detectPair: (Homography, Image, Image) => Seq[Tuple2[KeyPoint, KeyPoint]]
}

object PairDetector extends Logging {
  def apply[D <% Detector](threshold: Double, detector: D) = new PairDetector {
    override def detectPair =
      (homography: Homography,
        leftImage: Image,
        rightImage: Image) => {
        val left = detector.detect(leftImage)
        logger.debug(s"left.size: ${left.size}")
        val right = detector.detect(rightImage)
        logger.debug(s"right.size: ${right.size}")

        createBijectionWithNearestUnderWarp(
          threshold,
          homography,
          left,
          right).sortBy(pairResponse).reverse
      }
  }

  /**
   * Warps the `leftKeyPoint` by the `homography` and returns its
   * nearest neighbor among the `rightKeyPoints`.
   *
   * If no neighbors lay within `threshold` pixels, returns None.
   */
  def nearestUnderWarp(
    threshold: Double,
    homography: Homography,
    rightKeyPoints: Seq[KeyPoint])(leftKeyPoint: KeyPoint): Option[KeyPoint] = {
    val leftWarped = homography.transformXYOnly(leftKeyPoint)
    logger.debug(s"leftKeyPoint: $leftKeyPoint")
    logger.debug(s"leftWarped: $leftWarped")
    val rightWithDistances = rightKeyPoints zip rightKeyPoints.map(
      leftWarped.l2Distance)
    val (nearest, distance) = rightWithDistances.minBy(_._2)
    logger.debug(s"distance: $distance")
    if (distance < threshold) Some(nearest)
    else None
  }

  /**
   * The quality of a pair of keypoints.
   */
  def pairResponse(left: KeyPoint, right: KeyPoint): Double = {
    require(left.response >= 0)
    require(right.response >= 0)
    left.response * right.response
  }

  /**
   * The quality of a pair of keypoints.
   */
  def pairResponse(pair: Tuple2[KeyPoint, KeyPoint]): Double = 
    pairResponse(pair._1, pair._2)

  /**
   * Ensures the homography is a bijection between the keypoints on the left
   * and the right.
   *
   * Left keypoints which map to either zero right keypoints
   * are removed, as are right keypoints to which nothing maps.
   * Left keypoints which map to several right keypoints are coupled with the
   * closest match.
   * This induces a set of tuples of unique elements, which is returned.
   * A left keypoint is said to map to a right keypoint if the image of the
   * left keypoint is within |threshold| pixels of the right keypoint.
   *
   * Note: The homography maps from left to right.
   */
  def createBijectionWithNearestUnderWarp(
    threshold: Double, 
    homography: Homography, 
    leftKeyPoints: Seq[KeyPoint], 
    rightKeyPoints: Seq[KeyPoint]): Seq[Tuple2[KeyPoint, KeyPoint]] = {
    require(leftKeyPoints.size == leftKeyPoints.toSet.size)
    require(rightKeyPoints.size == rightKeyPoints.toSet.size)

    // The nearest neighbors on the right side of each left keypoint.
    val rightMatches = leftKeyPoints.map(nearestUnderWarp(
      threshold,
      homography,
      rightKeyPoints))
    assert(leftKeyPoints.size == rightMatches.size)

    // Drop pairs where the right keypoint wasn't found.
    val culledOption = leftKeyPoints zip rightMatches filter (_._2.isDefined)
    val culled = culledOption map {
      case (left, rightOption) => (left, rightOption.get)
    }

    // Sort all the putative pairs by their quality.
    // The best pairs will be _at the end_.
    val sorted = culled.sortBy(pairResponse)

    // Now the tricky bit; we flip the map, making keys into values and
    // vice-versa.
    // We push the flipped relation through a Map, which removes duplicate
    // right key points.
    // When there are duplicate key-value pairs, toMap takes the last pair;
    // here this means the highest quality pairs are retained.
    // Finally, the relation is unflipped.
    val noDuplicates = sorted.map(_.swap).toMap.toSeq.map(_.swap)

    // The toMap step destroyed the sort, so we have to recreate it.
    noDuplicates.sortBy(pairResponse).reverse
  }
}
