package billy

import org.opencv.core.KeyPoint
import breeze.linalg._
import java.io.File
import scalatestextra._
import org.apache.commons.io.FileUtils
import com.sksamuel.scrimage.Image
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
  
}

trait RichImageImplicits {
  implicit def image2RichImage(image: Image) = RichImage(image)
}
