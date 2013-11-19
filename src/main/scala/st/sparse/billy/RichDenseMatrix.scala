package st.sparse.billy

import org.opencv.core.Mat
import breeze.linalg.DenseMatrix
import com.sksamuel.scrimage.Image
import com.sksamuel.scrimage.PixelTools
import st.sparse.sundry.ExpectyOverrides._
import grizzled.math.stats

///////////////////////////////////////////////////

/**
 * Additional methods for DenseMatrix.
 */
case class RichDenseMatrix[A](matrix: DenseMatrix[A]) {
  def toSeqSeq: IndexedSeq[IndexedSeq[A]] =
    for (i <- 0 until matrix.rows) yield {
      for (j <- 0 until matrix.cols) yield matrix(i, j)
    }

  /**
   * Creates a grayscale image from this matrix.
   *
   * Assumes the matrix values are in the range [0.0, 1.0].
   */
  def toImage(implicit numericA: Numeric[A]): Image = {
    val image = Image.empty(matrix.cols, matrix.rows).toMutable

    (0 until matrix.rows) foreach { row =>
      (0 until matrix.cols) foreach { column =>
        val element = implicitly[Numeric[A]].toDouble(matrix(row, column))
        require(element >= 0)
        require(element <= 1)
        val scaled = (element * 255).round.toInt

        image.setPixel(column, row, PixelTools.argb(
          255,
          scaled,
          scaled,
          scaled))
      }
    }

    image
  }

  /**
   * Affine transform the contents of the matrix so that the minimum value
   * is zero and the maximum is one.
   */
  def affineToUnitInterval(
    implicit numericA: Numeric[A]): DenseMatrix[Double] = {
    val doubles = matrix.mapValues(implicitly[Numeric[A]].toDouble)
    val zeroMin = doubles - doubles.min

    zeroMin / zeroMin.max
  }
}

// TODO: Move to case class.
object RichDenseMatrix {
  def inpaint: DenseMatrix[Option[Double]] => DenseMatrix[Double] = (image) =>
    image.mapPairs {
      case (_, Some(element)) => element
      case ((y, x), None) => {
        def at(y: Int, x: Int): Option[Option[Double]] =
          if (y >= 0 && y < image.rows && x >= 0 && x < image.cols)
            Some(image(y, x))
          else None

        val window: Seq[Double] = {
          val indices = (y - 2 to y + 2).flatMap { y =>
            (x - 2 to x + 2).map { x => (y, x) }
          }
          indices.map((at _).tupled).flatten.flatten
        }

        stats.mean(window: _*)
      }
    }
}

trait RichDenseMatrixImplicits {
  implicit def denseMatrix2RichDenseMatrix[A](matrix: DenseMatrix[A]) =
    RichDenseMatrix(matrix)
}
