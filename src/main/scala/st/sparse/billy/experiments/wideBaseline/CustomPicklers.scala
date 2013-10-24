package st.sparse.billy.experiments.wideBaseline

import st.sparse.billy
import st.sparse.billy._
import breeze.linalg._

import scala.pickling._

trait CustomPicklers extends billy.CustomPicklers {
  implicit def oxfordPickler[D <% PairDetector: SPickler: Unpickler: FastTypeTag, E <% Extractor[F]: SPickler: Unpickler: FastTypeTag, M <% Matcher[F]: SPickler: Unpickler: FastTypeTag, F](
    implicit implicitFormat: PickleFormat) =
    new SPickler[Oxford[D, E, M, F]] with Unpickler[Oxford[D, E, M, F]] {
      override val format = implicitFormat

      override def pickle(
        picklee: Oxford[D, E, M, F],
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
        reader: PReader): Oxford[D, E, M, F] = {
        val imageClass = readField[String](reader)
        val otherImage = readField[Int](reader)
        val detector = readField[D](reader)
        val extractor = readField[E](reader)
        val matcher = readField[M](reader)

        Oxford(imageClass, otherImage, detector, extractor, matcher)
      }
    }

  implicit def middleburyPickler[D <% PairDetector: SPickler: Unpickler: FastTypeTag, E <% Extractor[F]: SPickler: Unpickler: FastTypeTag, M <% Matcher[F]: SPickler: Unpickler: FastTypeTag, F](
    implicit implicitFormat: PickleFormat) =
    new SPickler[Middlebury[D, E, M, F]] with Unpickler[Middlebury[D, E, M, F]] {
      override val format = implicitFormat

      override def pickle(
        picklee: Middlebury[D, E, M, F],
        builder: PBuilder) {
        builder.beginEntry(picklee)

        putField(builder, "databaseYear", picklee.databaseYear)
        putField(builder, "imageClass", picklee.imageClass)
        putField(builder, "detector", picklee.detector)
        putField(builder, "extractor", picklee.extractor)
        putField(builder, "matcher", picklee.matcher)

        builder.endEntry()
      }

      override def unpickle(
        tag: => FastTypeTag[_],
        reader: PReader): Middlebury[D, E, M, F] = {
        val databaseYear = readField[Int](reader)
        val imageClass = readField[String](reader)
        val detector = readField[D](reader)
        val extractor = readField[E](reader)
        val matcher = readField[M](reader)

        Middlebury(databaseYear, imageClass, detector, extractor, matcher)
      }
    }
}