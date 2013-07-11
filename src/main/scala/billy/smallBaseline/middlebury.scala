package billy.smallBaseline

import nebula._
import nebula.imageProcessing._
import nebula.util._

import billy._
import billy.brown._

import billy.smallBaseline._
import billy.wideBaseline._
import billy.summary._
 
import java.io.File

import scala.annotation.elidable
import scala.annotation.elidable.ASSERTION

import javax.imageio.ImageIO

///////////////////////////////////////////////////////////

case class SmallBaselinePair(left: Image, right: Image, flow: FlowField) {
  require(left.getWidth == right.getWidth)
  require(left.getWidth == flow.data.cols)
  require(left.getHeight == right.getHeight)
  require(left.getHeight == flow.data.rows)
}

object SmallBaselinePair {
  def apply(directoryRoot: File, name: String): SmallBaselinePair = {
    require(directoryRoot.isDirectory)

    def getFile(format: String): File = {
      val filename = format.format(name)
      val file = new File(directoryRoot, filename)
//      assert(file.isFile, "not a file: %s".format(file.toString))
      assert(file.isFile)
      file
    }

    val flow = {
      val file = getFile("/other-gt-flow/%s/flow10.flo.txt")
      FlowField.apply(file)
    }

    def getImage(format: String): Image = {
      val file = getFile(format)
      Image.read(file)
    }

    val left = getImage("/other-data/%s/frame10.png")
    val right = getImage("/other-data/%s/frame11.png")

    SmallBaselinePair(left, right, flow)
  }
}
