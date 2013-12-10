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
        putField(builder, "maxPairedDescriptors", picklee.maxPairedDescriptors)
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
        val maxPairedDescriptors = readField[Int](reader)
        val detector = readField[D](reader)
        val extractor = readField[E](reader)
        val matcher = readField[M](reader)

        Oxford(
          imageClass,
          otherImage,
          maxPairedDescriptors,
          detector,
          extractor,
          matcher)
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
        putField(builder, "maxPairedDescriptors", picklee.maxPairedDescriptors)
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
        val maxPairedDescriptors = readField[Int](reader)
        val detector = readField[D](reader)
        val extractor = readField[E](reader)
        val matcher = readField[M](reader)

        Middlebury(
          databaseYear,
          imageClass,
          maxPairedDescriptors,
          detector,
          extractor,
          matcher)
      }
    }

  implicit def blurredMiddleburyPickler[D <% PairDetector: SPickler: Unpickler: FastTypeTag, E <% Extractor[F]: SPickler: Unpickler: FastTypeTag, M <% Matcher[F]: SPickler: Unpickler: FastTypeTag, F](
    implicit implicitFormat: PickleFormat,
    middleburyFFT: FastTypeTag[Middlebury[D, E, M, F]]) =
    new SPickler[BlurredMiddlebury[D, E, M, F]] with Unpickler[BlurredMiddlebury[D, E, M, F]] {
      override val format = implicitFormat

      override def pickle(
        picklee: BlurredMiddlebury[D, E, M, F],
        builder: PBuilder) {
        builder.beginEntry(picklee)

        putField(builder, "similarityThreshold", picklee.similarityThreshold)
        putField(builder, "numSmoothingIterations", picklee.numSmoothingIterations)
        putField(builder, "scaleFactor", picklee.scaleFactor)
        putField(builder, "middlebury", picklee.middlebury)

        builder.endEntry()
      }

      override def unpickle(
        tag: => FastTypeTag[_],
        reader: PReader): BlurredMiddlebury[D, E, M, F] = {
        val similarityThreshold = readField[Double](reader)
        val numSmoothingIterations = readField[Int](reader)
        val scaleFactor = readField[Double](reader)
        val middlebury = readField[Middlebury[D, E, M, F]](reader)

        BlurredMiddlebury(
          similarityThreshold,
          numSmoothingIterations,
          scaleFactor,
          middlebury)
      }
    }
}