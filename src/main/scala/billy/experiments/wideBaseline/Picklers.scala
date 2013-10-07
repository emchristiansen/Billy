package billy.experiments.wideBaseline

import billy._

import org.joda.time._
import scala.pickling._

/**
 * This contains workarounds for a few cases where scala-pickling fails.
 *
 * You must import the contents of this object to have access to
 * the workarounds.
 * Obviously, this isn't a complete list.
 * Hopefully, though, scala-pickling will become stable soon and this whole
 * file can be deleted.
 */
trait Picklers {
  def pickleableToBytes[A: SPickler: Unpickler: FastTypeTag](
    a: A): Array[Byte] = {
    import binary._
    a.pickle.value
  }

  def putBytes[A: SPickler: Unpickler: FastTypeTag](
    builder: PBuilder,
    name: String,
    a: A) {
    builder.putField(
      name,
      b => b.hintTag(FastTypeTag.ArrayByte).beginEntry(
        pickleableToBytes(a)).endEntry())
  }

  def bytesToPickleable[A: SPickler: Unpickler: FastTypeTag](
    bytes: Array[Byte]): A = {
    import binary._
    BinaryPickle(bytes).unpickle[A]
  }

  def readBytes[A: SPickler: Unpickler: FastTypeTag](
    reader: PReader,
    name: String): A = {
    val bytes = reader.readField(name).readPrimitive().asInstanceOf[Array[Byte]]
    bytesToPickleable[A](bytes)
  }

  class ExperimentPickler[D <% Detector: SPickler: Unpickler: FastTypeTag, E <% Extractor[F]: SPickler: Unpickler: FastTypeTag, M <% Matcher[F]: SPickler: Unpickler: FastTypeTag, F](
    implicit val format: PickleFormat) extends SPickler[Experiment[D, E, M, F]] with Unpickler[Experiment[D, E, M, F]] {
    override def pickle(
      picklee: Experiment[D, E, M, F],
      builder: PBuilder) {
      builder.beginEntry(picklee)

      builder.putField(
        "imageClass",
        b => b.hintTag(FastTypeTag.ScalaString).beginEntry(
          picklee.imageClass).endEntry())
      builder.putField(
        "otherImage",
        b => b.hintTag(FastTypeTag.Int).beginEntry(
          picklee.otherImage).endEntry())
      putBytes(builder, "detector", picklee.detector)
      putBytes(builder, "extractor", picklee.extractor)
      putBytes(builder, "matcher", picklee.matcher)

      builder.endEntry()
    }

    override def unpickle(
      tag: => FastTypeTag[_],
      reader: PReader): Experiment[D, E, M, F] = {
      reader.beginEntry()
      
      val imageClass = reader.readField("imageClass").unpickle[String]
      val otherImage = reader.readField("otherImage").unpickle[Int]
      val detector = readBytes[D](reader, "detector")
      val extractor = readBytes[E](reader, "extractor")
      val matcher = readBytes[M](reader, "matcher")
      
      reader.endEntry()
      Experiment(imageClass, otherImage, detector, extractor, matcher)
    }
  }

  //      def pickle(picklee: DateTime, builder: PBuilder) {
  //      builder.beginEntry(picklee)
  //      builder.putField("iso8601", b =>
  //        b.hintTag(FastTypeTag.ScalaString).beginEntry(picklee.toString()).endEntry())
  //      builder.endEntry()
  //    }
  //
  //    def unpickle(tag: => FastTypeTag[_], reader: PReader): DateTime = {
  //      reader.beginEntry()
  //      val date = reader.readField("iso8601").unpickle[String]
  //      reader.endEntry()
  //      new DateTime(date)
  //    }

  implicit def customExperimentPickler[D <% Detector: SPickler: Unpickler: FastTypeTag, E <% Extractor[F]: SPickler: Unpickler: FastTypeTag, M <% Matcher[F]: SPickler: Unpickler: FastTypeTag, F](
    implicit format: PickleFormat) = new ExperimentPickler[D, E, M, F]()
}