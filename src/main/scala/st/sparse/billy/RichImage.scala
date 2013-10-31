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
}

trait RichImageImplicits {
  implicit def image2RichImage(image: Image) = RichImage(image)
}
