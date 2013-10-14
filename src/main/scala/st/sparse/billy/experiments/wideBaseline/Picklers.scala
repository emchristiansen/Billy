package st.sparse.billy.experiments.wideBaseline

import st.sparse.billy._

import scala.pickling._

/**
 * This contains workarounds for a few cases where scala-pickling fails.
 */
trait Picklers {
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

      Experiment(imageClass, otherImage, detector, extractor, matcher)
    }
  }

  implicit def customExperimentPickler[D <% Detector: SPickler: Unpickler: FastTypeTag, E <% Extractor[F]: SPickler: Unpickler: FastTypeTag, M <% Matcher[F]: SPickler: Unpickler: FastTypeTag, F](
    implicit format: PickleFormat) = new ExperimentPickler[D, E, M, F]()
}