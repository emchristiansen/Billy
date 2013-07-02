package billy

import nebula._
import billy.summary._
import billy.detectors._
import billy.extractors._
import billy.matchers._
import billy.wideBaseline._
import spray.json._
import nebula.util.JSONUtil._

////////////////////////////////

case class IncompleteWideBaselineExperiment[D <% PairDetector, E <% Extractor[F], M <% Matcher[F], F](
  detector: D,
  extractor: E,
  matcher: M)

trait IncompleteWideBaselineExperimentJsonProtocol extends DefaultJsonProtocol {
  implicit def jsonIncompleteWideBaselineExperiment[D <% PairDetector: JsonFormat, E <% Extractor[F]: JsonFormat, M <% Matcher[F]: JsonFormat, F] =
    jsonFormat3(IncompleteWideBaselineExperiment.apply[D, E, M, F]).addClassInfo(
      "IncompleteWideBaselineExperiment")
}

object IncompleteWideBaselineExperiment extends IncompleteWideBaselineExperimentJsonProtocol

object CompareMethods {
  def relativeBenchmarkFromMessage(
    incompleteExperiment: JSONAndTypeName)(
      implicit imports: Imports,
      runtimeConfig: RuntimeConfig): Double = {
    val source = s"""
    loadOpenCV  

    val incompleteExperiment = ${incompleteExperiment.toSource}
    
    def addRuntime(runtimeConfig: RuntimeConfig): Double = {
      implicit val rC = runtimeConfig
      def runExperiment(imageClass: String, otherImage: Int) =
        WideBaselineExperiment(
          imageClass,
          otherImage,
          incompleteExperiment.detector,
          incompleteExperiment.extractor,
          incompleteExperiment.matcher)
        
      CompareMethods.relativeBenchmark(runExperiment _)
    }
    
    addRuntime _
    """

    val needsRuntime =
      //      GlobalLock.synchronized {
      typeCheck[RuntimeConfig => Double](source.addImports)
    //    }

    (needsRuntime.apply).apply(runtimeConfig)
  }

  def relativeBenchmarkSources[E <% RuntimeConfig => ExperimentRunner[R]: JsonFormat: TypeName, R <% RuntimeConfig => ExperimentSummary](
    experiment: (String, Int) => E)(
      implicit runtimeConfig: RuntimeConfig,
      imports: Imports): Seq[ScalaSource[Double]] = {
    val imageClasses = Seq(
      "graffiti",
      "trees",
      "jpeg",
      "boat",
      "bark",
      "bikes",
      "light",
      "wall")

    // We use only a subset of the image pairs to separate train and test.
    val otherImages = Seq(2, 4, 6)

    for (
      imageClass <- imageClasses;
      otherImage <- otherImages
    ) yield relativeBenchmarkSingleSource(
      experiment(imageClass, otherImage),
      imageClass,
      otherImage)
  }

  def relativeBenchmarkSingleSource[E <% RuntimeConfig => ExperimentRunner[R]: JsonFormat: TypeName, R <% RuntimeConfig => ExperimentSummary](
    experiment: E,
    imageClass: String,
    otherImage: Int)(
      implicit runtimeConfig: RuntimeConfig,
      imports: Imports): ScalaSource[Double] = {
    val source = s"""
    loadOpenCV  

    implicit val runtimeConfig = ${getSource(runtimeConfig)} 
    
    val experiment = ${getSource(experiment)}
    
    CompareMethods.relativeBenchmarkSingle(
      experiment,
      "${imageClass}",
      ${otherImage})
    """.addImports

    ScalaSource[Double](source)
  }

  def relativeBenchmarkSingle[E <% RuntimeConfig => ExperimentRunner[R], R <% RuntimeConfig => ExperimentSummary](
    experiment: E,
    imageClass: String,
    otherImage: Int)(
      implicit runtimeConfig: RuntimeConfig): Double = {
    val fastDetector = BoundedPairDetector(
      BoundedDetector(OpenCVDetector.FAST, 2000),
      100)

    val siftDetector = BoundedPairDetector(
      BoundedDetector(OpenCVDetector.SIFT, 2000),
      100)

    val briskDetector = BoundedPairDetector(
      BoundedDetector(OpenCVDetector.BRISK, 2000),
      100)

    ///////////////////////////////////////

    def briefExperiment =
      WideBaselineExperiment(
        imageClass,
        otherImage,
        fastDetector,
        OpenCVExtractor.BRIEF,
        VectorMatcher.L0)

    def siftSIFTExperiment =
      WideBaselineExperiment(
        imageClass,
        otherImage,
        siftDetector,
        OpenCVExtractor.SIFT,
        VectorMatcher.L1)

    def briskExperiment =
      WideBaselineExperiment(
        imageClass,
        otherImage,
        briskDetector,
        OpenCVExtractor.BRISK,
        VectorMatcher.L0)

    /////////////////////////////////////////

    def lazyValue[A](a: => A): () => A = () => {
      val forcedA = a
      forcedA
    }

    val experimentSummary: () => ExperimentSummary =
      lazyValue((experiment(runtimeConfig).run)(runtimeConfig))
    val otherSummaries = Seq(
      lazyValue(briefExperiment.run: ExperimentSummary),
      lazyValue(siftSIFTExperiment.run: ExperimentSummary),
      lazyValue(briskExperiment.run: ExperimentSummary))

    def getNumericScore(summary: ExperimentSummary): Double = {
      assert(summary.summaryNumbers.size == 1)
      summary.summaryNumbers.values.head
    }

    val summaries = experimentSummary +: otherSummaries
    val experimentScore :: otherScores = (summaries.par.map { summary =>
      getNumericScore(summary())
    }).toList

    //    val experimentScore = getNumericScore(experimentSummary)
    //    val otherScores = otherSummaries map getNumericScore
    val meanOtherScore = (otherScores sum) / (otherScores size)

    experimentScore / meanOtherScore

  }

  //  def relativeBenchmark[E <% RuntimeConfig => ExperimentRunner[R], R <% RuntimeConfig => ExperimentSummary](
  //    experiment: (String, Int) => E)(implicit runtimeConfig: RuntimeConfig): Double = {
  //    val fastDetector = BoundedPairDetector(
  //      BoundedDetector(OpenCVDetector.FAST, 2000),
  //      100)
  //
  //    val siftDetector = BoundedPairDetector(
  //      BoundedDetector(OpenCVDetector.SIFT, 2000),
  //      100)
  //    val briskDetector = BoundedPairDetector(
  //      BoundedDetector(OpenCVDetector.BRISK, 2000),
  //      100)
  //
  //    ///////////////////////////////////////
  //
  //    def briefExperiment(imageClass: String, otherImage: Int) =
  //      WideBaselineExperiment(
  //        imageClass,
  //        otherImage,
  //        fastDetector,
  //        OpenCVExtractor.BRIEF,
  //        VectorMatcher.L0)
  //
  //    def siftSIFTExperiment(imageClass: String, otherImage: Int) =
  //      WideBaselineExperiment(
  //        imageClass,
  //        otherImage,
  //        siftDetector,
  //        OpenCVExtractor.SIFT,
  //        VectorMatcher.L1)
  //
  //    def briskExperiment(imageClass: String, otherImage: Int) =
  //      WideBaselineExperiment(
  //        imageClass,
  //        otherImage,
  //        briskDetector,
  //        OpenCVExtractor.BRISK,
  //        VectorMatcher.L0)
  //
  //    /////////////////////////////////////////
  //
  //    def relativeScore(imageClass: String, otherImage: Int): Double = {
  //      val experimentSummary: ExperimentSummary =
  //        (experiment(imageClass, otherImage)(runtimeConfig).run)(runtimeConfig)
  //      val otherSummaries = Seq(
  //        briefExperiment(imageClass, otherImage).run: ExperimentSummary,
  //        siftSIFTExperiment(imageClass, otherImage).run: ExperimentSummary,
  //        briskExperiment(imageClass, otherImage).run: ExperimentSummary)
  //
  //      def getNumericScore(summary: ExperimentSummary): Double = {
  //        assert(summary.summaryNumbers.size == 1)
  //        summary.summaryNumbers.values.head
  //      }
  //
  //      val experimentScore = getNumericScore(experimentSummary)
  //      val otherScores = otherSummaries map getNumericScore
  //      val meanOtherScore = (otherScores sum) / (otherScores size)
  //
  //      experimentScore / meanOtherScore
  //    }
  //
  //    ///////////////////////////////////////////
  //
  //    val imageClasses = Seq(
  //      "graffiti",
  //      "trees",
  //      "jpeg",
  //      "boat",
  //      "bark",
  //      "bikes",
  //      "light",
  //      "wall")
  //
  //    val otherImages = Seq(2, 3, 4, 5, 6)
  //
  //    val relativeScores = (for (
  //      imageClass <- imageClasses.par;
  //      otherImage <- otherImages.par
  //    ) yield relativeScore(imageClass, otherImage)).toList
  //
  //    (relativeScores sum) / (relativeScores size)
  //  }
}