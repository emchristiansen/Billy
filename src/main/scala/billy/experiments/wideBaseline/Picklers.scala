package billy.experiments.wideBaseline

import billy._

import org.joda.time._
import scala.pickling._

//case class MyClass[A: SPickler: Unpickler: FastTypeTag](myString: String, a: A)

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
  //  {
  //    class MyClassPickler[A: SPickler: Unpickler: FastTypeTag](
  //      implicit val format: PickleFormat) extends SPickler[MyClass[A]] with Unpickler[MyClass[A]] {
  //      override def pickle(
  //        picklee: MyClass[A],
  //        builder: PBuilder) {
  //        builder.beginEntry(picklee)
  //
  //        // Here we save `myString` in some custom way.
  //        builder.putField(
  //          "mySpecialPickler",
  //          b => b.hintTag(FastTypeTag.ScalaString).beginEntry(
  //            picklee.myString).endEntry())
  //
  //        // Now we need to save `a`, which has an implicit SPickler.
  //        // But how do we use it?
  //
  //        builder.endEntry()
  //      }
  //
  //      override def unpickle(
  //        tag: => FastTypeTag[_],
  //        reader: PReader): MyClass[A] = {
  //        reader.beginEntry()
  //        val myString = reader.readField("mySpecialPickler").unpickle[String]
  //
  //        // Now we need to read `a`, which has an implicit Unpickler.
  //        // But how do we use it?
  //        val a: A = ???
  //
  //        reader.endEntry()
  //
  //        MyClass(myString, a)
  //      }
  //    }
  //  }

  //  def pickleableToBytes[A: SPickler: Unpickler: FastTypeTag](
  //    a: A): Array[Byte] = {
  //    import binary._
  //    println(a.pickle)
  //    a.pickle.value
  //  }
  //
  //  def putBytes[A: SPickler: Unpickler: FastTypeTag](
  //    builder: PBuilder,
  //    name: String,
  //    a: A) {
  //    builder.putField(
  //      name,
  //      b => b.hintTag(FastTypeTag.ArrayByte).beginEntry(
  //        pickleableToBytes(a)).endEntry())
  //  }
  //
  //  def bytesToPickleable[A: SPickler: Unpickler: FastTypeTag](
  //    bytes: Array[Byte]): A = {
  //    import binary._
  //    BinaryPickle(bytes).unpickle[A]
  //  }
  //
  //  def readBytes[A: SPickler: Unpickler: FastTypeTag](
  //    reader: PReader,
  //    name: String): A = {
  //    import binary._
  //    val bytes = reader.readField(name)
  //    println(bytes.readPrimitive)
  //    //    bytes.unpickle[Array[_]]
  //    ???
  //    //    bytesToPickleable[A](bytes)
  //  }

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

  class ExperimentPickler[D <% Detector: SPickler: Unpickler: FastTypeTag, E <% Extractor[F]: SPickler: Unpickler: FastTypeTag, M <% Matcher[F]: SPickler: Unpickler: FastTypeTag, F](
    implicit val format: PickleFormat) extends SPickler[Experiment[D, E, M, F]] with Unpickler[Experiment[D, E, M, F]] {
    override def pickle(
      picklee: Experiment[D, E, M, F],
      builder: PBuilder) {
      builder.beginEntry(picklee)

      putField(builder, "imageClass", picklee.imageClass)
      putField(builder, "otherImage", picklee.otherImage)
      putField(builder, "detector", picklee.detector)
      putField(builder, "extractor", picklee.extractor)
      putField(builder, "matcher", picklee.matcher)

      //      builder.putField(
      //        "imageClass",
      //        b => b.hintTag(FastTypeTag.ScalaString).beginEntry(
      //          picklee.imageClass).endEntry())
      //      builder.putField(
      //        "otherImage",
      //        b => b.hintTag(FastTypeTag.Int).beginEntry(
      //          picklee.otherImage).endEntry())
      //      builder.putField("detector",
      //        b => {
      //          b.hintTag(implicitly[FastTypeTag[D]])
      //          implicitly[SPickler[D]].pickle(picklee.detector, b)
      //        })
      //      builder.putField("extractor",
      //        b => {
      //          b.hintTag(implicitly[FastTypeTag[E]])
      //          implicitly[SPickler[E]].pickle(picklee.extractor, b)
      //        })
      //      builder.putField("matcher",
      //        b => {
      //          b.hintTag(implicitly[FastTypeTag[M]])
      //          implicitly[SPickler[M]].pickle(picklee.matcher, b)
      //        })

      builder.endEntry()
    }

    override def unpickle(
      tag: => FastTypeTag[_],
      reader: PReader): Experiment[D, E, M, F] = {
      val imageClass = readField[String](reader)
      val otherImage = readField[Int](reader)
      val detector = readField[D](reader)
      val extractor = readField[E](reader)
      val matcher = readField[M](reader)
      
//      val imageClass = {
//        reader.hintTag(FastTypeTag.ScalaString)
//        val tag = reader.beginEntry()
//        val value = implicitly[Unpickler[String]].unpickle(
//          tag,
//          reader).asInstanceOf[String]
//        reader.endEntry()
//        value
//      }
//
//      val otherImage = {
//        reader.hintTag(FastTypeTag.Int)
//        val tag = reader.beginEntry()
//        val value = implicitly[Unpickler[Int]].unpickle(
//          tag,
//          reader).asInstanceOf[Int]
//        reader.endEntry()
//        value
//      }
//
//      val detector = {
//        reader.hintTag(implicitly[FastTypeTag[D]])
//        val tag = reader.beginEntry()
//        val value = implicitly[Unpickler[D]].unpickle(
//          tag,
//          reader).asInstanceOf[D]
//        reader.endEntry()
//        value
//      }
//
//      val extractor = {
//        reader.hintTag(implicitly[FastTypeTag[E]])
//        val tag = reader.beginEntry()
//        val value = implicitly[Unpickler[E]].unpickle(
//          tag,
//          reader).asInstanceOf[E]
//        reader.endEntry()
//        value
//      }
//
//      val matcher = {
//        reader.hintTag(implicitly[FastTypeTag[M]])
//        val tag = reader.beginEntry()
//        val value = implicitly[Unpickler[M]].unpickle(
//          tag,
//          reader).asInstanceOf[M]
//        reader.endEntry()
//        value
//      }

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