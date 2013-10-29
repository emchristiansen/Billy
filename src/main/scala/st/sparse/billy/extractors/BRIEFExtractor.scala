package st.sparse.billy.extractors

import com.sksamuel.scrimage.filter.GaussianBlurFilter
import org.opencv.core.Point
import st.sparse.sundry._
import scala.util.Try
import com.sksamuel.scrimage.Image
import st.sparse.billy.Gray
import st.sparse.billy._
import com.sksamuel.scrimage.PixelTools

case class BRIEFExtractor(
  numPairs: Int,
  patchWidth: Int,
  blurWidth: Int) extends ExtractorSingle[IndexedSeq[Boolean]] {
  require(numPairs > 0)
  require(patchWidth > 0)
  require(blurWidth > 0)

  val random = new util.Random(0)
  val pointPairs = numPairs times {
    def nextCoordinate() = random.nextDouble.abs % patchWidth

    val left = new Point(nextCoordinate(), nextCoordinate())
    val right = new Point(nextCoordinate(), nextCoordinate())
    (left, right)
  }

  override def extractSingle = (image, keyPoint) => {
    val blurred = image.filter(GaussianBlurFilter(blurWidth))
    val patchOption: Option[Image] =
      Try(blurred.subpixelSubimageCenteredAtPoint(
        keyPoint.pt.x,
        keyPoint.pt.y,
        patchWidth.toDouble / 2,
        patchWidth.toDouble / 2)).toOption

    for (patch <- patchOption) yield {
      pointPairs map {
        case (leftPoint, rightPoint) =>
          val leftPixel = patch.subpixel(leftPoint.x, leftPoint.y)
          val rightPixel = patch.subpixel(rightPoint.x, rightPoint.y)
          
          PixelTools.gray(leftPixel) < PixelTools.gray(rightPixel)
      }
    }
  }
}