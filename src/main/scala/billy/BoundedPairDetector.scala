package billy

///////////////////////////////////////////////////////////

case class BoundedPairDetector[D <% PairDetector](
  pairDetector: D,
  maxKeyPoints: Int)

object BoundedPairDetector {
  implicit class ToPairDetector[D <% PairDetector](
    self: BoundedPairDetector[D]) extends PairDetector {
    override def detect =
      image => self.pairDetector.detect(image).take(self.maxKeyPoints)

    override def detectPair = (homography, leftImage, rightImage) =>
      self.pairDetector.detectPair(
        homography,
        leftImage,
        rightImage).take(self.maxKeyPoints)
  }
}