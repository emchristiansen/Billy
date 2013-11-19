package st.sparse.billy

import st.sparse.sundry._
import st.sparse.billy._
import org.opencv.core.Point
import com.sksamuel.scrimage.Image
import st.sparse.sundry.ExistingDirectory
import java.io.File
import breeze.linalg.DenseMatrix
import com.sksamuel.scrimage.PixelTools
import scala.collection.mutable.Queue
import java.net.JarURLConnection

trait Segmentation {
  /**
   * The probability two points belong to the same segment.
   *
   * Sometimes this cannot be computed, such as when a point is on a
   * boundary.
   */
  def probabilityInSameSegment: (Point, Point) => Option[Double]
}

object Segmentation {
  def fromBoundariesImage(boundariesImage: Image): Segmentation = {
    val boundaries = boundariesImage.toGrayMatrix mapValues { _ / 255.0 }

    val step = 0.04
    val layers = (0.0 until 1.0 by step) map { probability =>
      val segments = boundaries mapValues (_ < probability)
      (MatlabGPbSegmenter.connectedComponentsLabels(segments), probability)
    }

    new Segmentation {
      override def probabilityInSameSegment = (left: Point, right: Point) => {
        // Floor is here because the segmentation logic doesn't work with
        // subpixels.
        val leftX = left.x.floor.toInt
        val leftY = left.y.floor.toInt
        val rightX = right.x.floor.toInt
        val rightY = right.y.floor.toInt

        require(leftX >= 0 && leftX < boundariesImage.width)
        require(leftY >= 0 && leftY < boundariesImage.height)
        require(rightX >= 0 && rightX < boundariesImage.width)
        require(rightY >= 0 && rightY < boundariesImage.height)

        val probabilityDifferent = layers.find {
          case (layer, _) =>
            val left = layer(leftY, leftX)
            val right = layer(rightY, rightX)
            left.isDefined && right.isDefined && left.get == right.get
        } map (_._2)

        probabilityDifferent map (1 - _)
      }
    }
  }
}