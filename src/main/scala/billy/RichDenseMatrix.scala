package billy

import org.opencv.core.Mat
import breeze.linalg.DenseMatrix

///////////////////////////////////////////////////

/**
 * Additional methods for DenseMatrix.
 */
case class RichDenseMatrix[A](matrix: DenseMatrix[A]) {
  def toSeqSeq: IndexedSeq[IndexedSeq[A]] =
    for (i <- 0 until matrix.rows) yield {
      for (j <- 0 until matrix.cols) yield matrix(i, j)
    }
}

trait RichDenseMatrixImplicits {
  implicit def denseMatrix2RichDenseMatrix[A](matrix: DenseMatrix[A]) = 
    RichDenseMatrix(matrix)
}