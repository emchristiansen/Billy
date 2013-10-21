package st.sparse.billy

import scala.pickling._

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
}