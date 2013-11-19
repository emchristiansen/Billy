package st.sparse.billy.extractors

import st.sparse.sundry._
import st.sparse.billy._
import org.opencv.core.Point
import com.sksamuel.scrimage.Image
import st.sparse.sundry.ExistingDirectory
import java.io.File
import st.sparse.billy.MatlabUtil
import breeze.linalg.DenseMatrix
import com.sksamuel.scrimage.PixelTools
import scala.collection.mutable.Queue
import java.net.JarURLConnection

//trait Segmenter {
//  def segmentation: Image => Segmentation
//
//  def boundaries: Image => DenseMatrix[Double]
//}
//
//trait SegmenterBoundaries extends Segmenter {
//  override def segmentation = (image: Image) => {
//    val boundaries = this.boundaries(image)
//
//    val step = 0.04
//    val layers = (0.0 until 1.0 by step) map { probability =>
//      val segments = boundaries mapValues (_ < probability)
//      (MatlabGPbSegmenter.connectedComponentsLabels(segments), probability)
//    }
//
//    new Segmentation {
//      override def probabilityInSameSegment = (left: Point, right: Point) => {
//        val leftX = left.x.round.toInt
//        val leftY = left.y.round.toInt
//        val rightX = right.x.round.toInt
//        val rightY = right.y.round.toInt
//
//        require(leftX >= 0 && leftX < image.width)
//        require(leftY >= 0 && leftY < image.height)
//        require(rightX >= 0 && rightX < image.width)
//        require(rightY >= 0 && rightY < image.height)
//
//        val probabilityDifferent = layers.find {
//          case (layer, _) =>
//            val left = layer(leftY, leftX)
//            val right = layer(rightY, rightX)
//            left.isDefined && right.isDefined && left.get == right.get
//        } map (_._2)
//
//        probabilityDifferent map (1 - _)
//      }
//    }
//  }
//}

