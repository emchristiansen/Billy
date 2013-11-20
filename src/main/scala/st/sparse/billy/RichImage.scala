package st.sparse.billy

import org.opencv.core.KeyPoint
import breeze.linalg._
import java.io.File
import st.sparse.sundry._
import org.apache.commons.io.FileUtils
import com.sksamuel.scrimage._
import org.opencv.core.Mat
import javax.imageio.ImageIO
import org.opencv.highgui.Highgui
import scalaz._
import Scalaz._
import org.opencv.core.Point

///////////////////////////////////////

/**
 * Additional methods for Image.
 */
case class RichImage(image: Image) {
  def toMat: Mat = {
    // TODO: Figure out how to do this without IO.
    val file = File.createTempFile("imageToMat", ".png")
    ImageIO.write(image.awt, "png", file)
    val mat = Highgui.imread(file.toString)
    file.delete
    assert(mat != null)
    mat
  }

  def toSeqSeq: Seq[Seq[(Int, Int, Int, Int)]] =
    for (y <- 0 until image.height) yield {
      for (x <- 0 until image.width) yield {
        val pixel = image.pixel(x, y)

        (PixelTools.alpha(pixel),
          PixelTools.red(pixel),
          PixelTools.green(pixel),
          PixelTools.blue(pixel))
      }
    }

  def toGrayMatrix: DenseMatrix[Int] = DenseMatrix.tabulate[Int](
    image.height,
    image.width) {
      case (y, x) => {
        PixelTools.gray(image.pixel(x, y))
      }
    }
  
  // Only takes grayscale images.
  def inpaintBlackPixels: Image = {
    val matrixInt = toGrayMatrix
    val matrixOptionDouble = matrixInt.mapValues {
      case 0 => None
      case x => Some(x.toDouble / 255)
    }
    val inpainted = RichDenseMatrix.inpaint(matrixOptionDouble)
    inpainted.toImage
  }
}

// TODO
object RichImage {
  def edgePreservingSmoothing(
    radius: Int,
    segmentation: Segmentation)(
      image: Image): Image = {
    val smoothed = Image.empty(image.width, image.height).toMutable
    for (
      yCenter <- 0 until image.height par;
      xCenter <- 0 until image.width
    ) {
      val yStart = math.max(yCenter - radius, 0)
      val yEnd = math.min(yCenter + radius, image.height - 1)
      val xStart = math.max(xCenter - radius, 0)
      val xEnd = math.min(xCenter + radius, image.width - 1)

      val pixelsAndWeights = for (
        ySample <- yStart to yEnd;
        xSample <- xStart to xEnd
      ) yield {
        val pixel = image.pixel(xSample, ySample)
        val vector = DenseVector[Double](
          PixelTools.alpha(pixel),
          PixelTools.red(pixel),
          PixelTools.green(pixel),
          PixelTools.blue(pixel))
        val weightOption = segmentation.probabilityInSameSegment(
          new Point(xCenter, yCenter),
          new Point(xSample, ySample))
        for (weight <- weightOption) yield {
          val weightPower = math.pow(weight, 5)
          (vector * weightPower, weightPower)
        }
      }

      val Seq(alpha, red, green, blue): Seq[Int] =
        (pixelsAndWeights.flatten.map(_._1).reduce(_ + _) /
          pixelsAndWeights.flatten.map(_._2).sum).data.map(_.round.toInt)
      smoothed.setPixel(
        xCenter,
        yCenter,
        PixelTools.argb(alpha, red, green, blue))
    }

    smoothed
  }
}

trait RichImageImplicits {
  implicit def image2RichImage(image: Image) = RichImage(image)
}
