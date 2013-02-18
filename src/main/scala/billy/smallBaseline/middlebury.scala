package nebula.smallBaseline

import nebula._
import java.awt.image.BufferedImage
import java.io.File

import scala.annotation.elidable
import scala.annotation.elidable.ASSERTION

import javax.imageio.ImageIO

///////////////////////////////////////////////////////////

case class SmallBaselinePair(left: BufferedImage, right: BufferedImage, flow: FlowField) {
  requirey(left.getWidth == right.getWidth)
  requirey(left.getWidth == flow.data.cols)
  requirey(left.getHeight == right.getHeight)
  requirey(left.getHeight == flow.data.rows)
}

object SmallBaselinePair {
  def apply(directoryRoot: File, name: String): SmallBaselinePair = {
    requirey(directoryRoot.isDirectory)

    def getFile(format: String): File = {
      val filename = format.format(name)
      val file = new File(directoryRoot, filename)
//      asserty(file.isFile, "not a file: %s".format(file.toString))
      asserty(file.isFile)
      file
    }

    val flow = {
      val file = getFile("/other-gt-flow/%s/flow10.flo.txt")
      FlowField.apply(file)
    }

    def getImage(format: String): BufferedImage = {
      val file = getFile(format)
      val image = ImageIO.read(file)
      asserty(image != null)
      image
    }

    val left = getImage("/other-data/%s/frame10.png")
    val right = getImage("/other-data/%s/frame11.png")

    SmallBaselinePair(left, right, flow)
  }
}