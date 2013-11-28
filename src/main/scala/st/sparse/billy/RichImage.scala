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

case class ImagePOD(
  width: Int,
  height: Int,
  `type`: Int,
  data: Array[Int]) {
  override def toString = 
    s"ImagePOD($width, $height, ${`type`}, ${data.hashCode})" 
}

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

  def toPOD: ImagePOD = ImagePOD(
    image.awt.getWidth,
    image.awt.getHeight,
    image.awt.getType,
    image.pixels.toArray)

  // Only takes grayscale images.
  def inpaintBlackPixels: Image = {
    val matrixInt = toGrayMatrix
    val matrixOptionDouble = matrixInt.mapValues {
      case x if PixelTools.gray(x) == 0 => None
      case x => Some(x.toDouble / 255)
    }
    val inpainted = RichDenseMatrix.inpaint(matrixOptionDouble)
    inpainted.toImage
  }

  def map(function: ((Int, Int), Int) => Int): Image = {
    val mapped = Image.empty(image.width, image.height).toMutable
    for (y <- 0 until image.height par; x <- 0 until image.width) {
      val newPixel = function((x, y), image.pixel(x, y))
      mapped.setPixel(x, y, newPixel)
    }
    mapped
  }

  def patchSafe(patchRadius: Int, xCenter: Int, yCenter: Int): Image = {
    val xStart = math.max(xCenter - patchRadius, 0)
    val xEnd = math.min(xCenter + patchRadius + 1, image.width)
    val yStart = math.max(yCenter - patchRadius, 0)
    val yEnd = math.min(yCenter + patchRadius + 1, image.height)

    val patchWidth = xEnd - xStart
    val patchHeight = yEnd - yStart
    new Image(image.awt.getSubimage(
      xStart,
      yStart,
      patchWidth,
      patchHeight)).copy
  }

  def mapStencil(
    patchRadius: Int)(
      function: ((Int, Int), Int, Image) => Int): Image = {
    map {
      case ((xCenter, yCenter), pixel) =>
        val patch = patchSafe(patchRadius, xCenter, yCenter)
        function((xCenter, yCenter), pixel, patch)
    }
  }

  // 
  def anisotropicDiffusion(
    similarityThreshold: Double,
    similarities: Image): Image = mapStencil(1) {
    case ((xCenter, yCenter), pixel, imagePatch) => {
      if (imagePatch.width != 3 || imagePatch.height != 3) pixel
      else {
        val similarityPatch =
          RichImage(similarities).patchSafe(1, xCenter, yCenter)
        assert(
          similarityPatch.width == imagePatch.width &&
            similarityPatch.height == imagePatch.height)

        val pixelsAndWeights = for (ySample <- 0 until 3; xSample <- 0 until 3) yield {
          val similarityCenter = PixelTools.gray(similarityPatch.pixel(1, 1))
          val similaritySample = PixelTools.gray(similarityPatch.pixel(
            xSample,
            ySample))

          // We'll use a sum of ratios to be scale invariant.
          val dissimilarity =
            similarityCenter.toDouble / similaritySample +
              similaritySample.toDouble / similarityCenter
          val weight = math.pow(1 / dissimilarity, 20)

          if (dissimilarity > similarityThreshold) None
          else {
            val pixel = imagePatch.pixel(xSample, ySample)
            val vector = DenseVector[Double](
              PixelTools.alpha(pixel),
              PixelTools.red(pixel),
              PixelTools.green(pixel),
              PixelTools.blue(pixel))
            Some(vector)
          }
          //          val pixel = imagePatch.pixel(xSample, ySample)
          //          val vector = DenseVector[Double](
          //            PixelTools.alpha(pixel),
          //            PixelTools.red(pixel),
          //            PixelTools.green(pixel),
          //            PixelTools.blue(pixel))
          //          (vector * weight, weight)
        }
        //
        //        val Seq(alpha, red, green, blue): Seq[Int] =
        //          (pixelsAndWeights.map(_._1).reduce(_ + _) /
        //            pixelsAndWeights.map(_._2).sum).data.map(_.round.toInt)
        //        PixelTools.argb(alpha, red, green, blue)

        val meanVector =
          pixelsAndWeights.flatten.reduce(_ + _) / pixelsAndWeights.flatten.size.toDouble
        val Array(alpha, red, green, blue) = meanVector.map(_.round.toInt).data
        PixelTools.argb(alpha, red, green, blue)
      }
    }
  }
}

// TODO
object RichImage {
  def fromPOD(pod: ImagePOD): Image = {
    val ImagePOD(width, height, imageType, data) = pod
    Image(width, height, data.toArray)
  }

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
