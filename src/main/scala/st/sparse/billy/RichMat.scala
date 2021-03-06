package st.sparse.billy

import org.opencv.core.Mat
import org.opencv.core.CvType
import breeze.linalg.DenseMatrix

///////////////////////////////////////////////////

/**
 * Additional methods for Mat.
 */
case class RichMat(mat: Mat) {
  def toMatrixSeqDouble: Option[DenseMatrix[IndexedSeq[Double]]] = {
    // There may be errors converting byte types to Double.
    if (mat.rows == 0 || mat.cols == 0 || mat.channels == 0) None
    else {
      val matrix = DenseMatrix.zeros[IndexedSeq[Double]](mat.rows, mat.cols)
      for (row <- 0 until mat.rows; column <- 0 until mat.cols) {
        val doubles = mat.get(row, column)
        matrix(row, column) = doubles.toIndexedSeq
      }

      Some(matrix)
    }
  }

  def toMatrixDouble: Option[DenseMatrix[Double]] = {
    if (mat.channels == 1) {
      for (matrixSeqDouble <- toMatrixSeqDouble) yield {
        matrixSeqDouble mapValues {
          case IndexedSeq(x) => x
        }
      }
    } else None
  }
}

trait RichMatImplicits {
  implicit def mat2RichMat(mat: Mat) = RichMat(mat)
}
