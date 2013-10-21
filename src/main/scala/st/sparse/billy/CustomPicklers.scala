package st.sparse.billy

import breeze.linalg._
import scala.pickling._
import scala.reflect._

object ImplicitGuider {
  implicitly[SPickler[Double]]
  //  def denseMatrixPickler[A: SPickler: FastTypeTag](
  //    implicit format: PickleFormat) =
  //    implicitly[SPickler[Array[Double]]]
}

trait CustomPicklers {
  def putField[A: SPickler: FastTypeTag](
    builder: PBuilder,
    name: String,
    value: A) {
    builder.putField(
      name,
      b => {
        b.hintTag(implicitly[FastTypeTag[A]])
        implicitly[SPickler[A]].pickle(value, b)
      })
  }

  def readField[A: Unpickler: FastTypeTag](reader: PReader): A = {
    reader.hintTag(implicitly[FastTypeTag[A]])
    val tag = reader.beginEntry()
    val value = implicitly[Unpickler[A]].unpickle(
      tag,
      reader).asInstanceOf[A]
    reader.endEntry()
    value
  }

  // TODO: This should be generalized to DenseMatrix[A], but currently that
  // causes pickling to barf.
  implicit def denseMatrixDoublePickler(implicit implicitFormat: PickleFormat) =
    new SPickler[DenseMatrix[Double]] with Unpickler[DenseMatrix[Double]] {
      override val format = implicitFormat

      override def pickle(
        picklee: DenseMatrix[Double],
        builder: PBuilder) {
        builder.beginEntry(picklee)

        putField(builder, "rows", picklee.rows)
        putField(builder, "data", picklee.data)

        builder.endEntry()
      }

      override def unpickle(
        tag: => FastTypeTag[_],
        reader: PReader): DenseMatrix[Double] = {
        val rows = readField[Int](reader)
        val data = readField[Array[Double]](reader)

        new DenseMatrix(rows, data)
      }
    }

  //  implicit def denseMatrixPickler

  //  class DenseMatrixPickler[A: SPickler: Unpickler: FastTypeTag: ClassTag](
  //    implicit val format: PickleFormat,
  //    listSP: SPickler[List[A]],
  //    listU: Unpickler[List[A]],
  //    listFTT: FastTypeTag[List[A]]) extends SPickler[DenseMatrix[A]] with Unpickler[DenseMatrix[A]] {
  //    override def pickle(
  //      picklee: DenseMatrix[A],
  //      builder: PBuilder) {
  //      builder.beginEntry(picklee)
  //
  //      val rows = picklee.rows
  //      val data = picklee.data.toList
  //
  //      putField(builder, "rows", rows)
  //      putField(builder, "data", data)
  //
  //      builder.endEntry()
  //    }
  //
  //    override def unpickle(
  //      tag: => FastTypeTag[_],
  //      reader: PReader): DenseMatrix[A] = {
  //
  //      val rows = readField[Int](reader)
  //      val data = readField[List[A]](reader)
  //
  //      new DenseMatrix(rows, data.toArray)
  //    }
  //  }
  //
  //  implicit def denseMatrixPickler[A: SPickler: Unpickler: FastTypeTag: ClassTag](
  //    implicit format: PickleFormat,
  //    listSP: SPickler[List[A]],
  //    listU: Unpickler[List[A]],
  //    listFTT: FastTypeTag[List[A]]) = new DenseMatrixPickler[A]
}