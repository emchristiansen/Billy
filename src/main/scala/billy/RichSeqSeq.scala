package billy

import scala.reflect.ClassTag

import breeze.linalg.DenseMatrix

//////////////////////////////////////////////////////

case class RichSeqSeq[A: ClassTag](seqSeq: Seq[Seq[A]]) {
  def toDenseMatrix: DenseMatrix[A] = {
    require(seqSeq.size > 0)
    require(seqSeq.head.size > 0)
    // Ensures all the rows are the same length.
    require(seqSeq forall { _.size == seqSeq.head.size })

    val matrix = new DenseMatrix[A](seqSeq.size, seqSeq.flatten.toArray)
    
    assert(matrix.rows == seqSeq.size)
    assert(matrix.cols == seqSeq.head.size)
    
    matrix
  }
}

trait RichSeqSeqImplicits {
  implicit def seqSeq2RichSeqSeq[A: ClassTag](seqSeq: Seq[Seq[A]]) =
    RichSeqSeq(seqSeq)
}