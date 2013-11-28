package st.sparse.billy

import scala.pickling._
import com.sksamuel.scrimage.Image

/**
 * This contains workarounds for a few cases where scala-pickling fails.
 */
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

  implicit def imagePickler(implicit implicitFormat: PickleFormat) =
    new SPickler[Image] with Unpickler[Image] {
      override val format = implicitFormat

      override def pickle(
        pickleeReference: Image,
        builder: PBuilder) {
        val picklee = pickleeReference.copy
        builder.beginEntry(picklee)

        putField(builder, "width", picklee.awt.getWidth)
        putField(builder, "height", picklee.awt.getHeight)
        putField(builder, "type", picklee.awt.getType)
        val pixels: Array[Int] = picklee.pixels.toList.toArray
        putField(builder, "pixels", pixels)

        builder.endEntry()
      }

      override def unpickle(
        tag: => FastTypeTag[_],
        reader: PReader): Image = {
        val width = readField[Int](reader)
        val height = readField[Int](reader)
        val `type` = readField[Int](reader)
        val pixels = readField[Array[Int]](reader)
        
        // TODO: Don't assume type can be ignored.
        Image(width, height, pixels)
      }
    }
}