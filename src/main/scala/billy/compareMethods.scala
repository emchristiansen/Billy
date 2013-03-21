package billy

import nebula._
import billy.summary._
import billy.detectors._
import billy.extractors._
import billy.matchers._
import billy.wideBaseline._
import spray.json._

////////////////////////////////

object CompareMethods {
  def relativeBenchmark[E <% RuntimeConfig => ExperimentRunner[R], R <% RuntimeConfig => ExperimentSummary](
    experiment: (String, Int) => E)(implicit runtimeConfig: RuntimeConfig): Double = {
    val fastDetector = BoundedPairDetector(
      BoundedDetector(OpenCVDetector.FAST, 5000),
      200)

    val siftDetector = BoundedPairDetector(
      BoundedDetector(OpenCVDetector.SIFT, 5000),
      200)
    val briskDetector = BoundedPairDetector(
      BoundedDetector(OpenCVDetector.BRISK, 5000),
      200)

    ///////////////////////////////////////

    def briefExperiment(imageClass: String, otherImage: Int) =
      WideBaselineExperiment(
        imageClass,
        otherImage,
        fastDetector,
        OpenCVExtractor.BRIEF,
        VectorMatcher.L0)

    def siftSIFTExperiment(imageClass: String, otherImage: Int) =
      WideBaselineExperiment(
        imageClass,
        otherImage,
        siftDetector,
        OpenCVExtractor.SIFT,
        VectorMatcher.L1)

    def briskExperiment(imageClass: String, otherImage: Int) =
      WideBaselineExperiment(
        imageClass,
        otherImage,
        briskDetector,
        OpenCVExtractor.BRISK,
        VectorMatcher.L0)

    /////////////////////////////////////////

    def relativeScore(imageClass: String, otherImage: Int): Double = {
      val experimentSummary: ExperimentSummary =
        (experiment(imageClass, otherImage)(runtimeConfig).run)(runtimeConfig)
      val otherSummaries = Seq(
        briefExperiment(imageClass, otherImage).run: ExperimentSummary,
        siftSIFTExperiment(imageClass, otherImage).run: ExperimentSummary,
        briskExperiment(imageClass, otherImage).run: ExperimentSummary)

      def getNumericScore(summary: ExperimentSummary): Double = {
        asserty(summary.summaryNumbers.size == 1)
        summary.summaryNumbers.values.head
      }

      val experimentScore = getNumericScore(experimentSummary)
      val otherScores = otherSummaries map getNumericScore
      val meanOtherScore = (otherScores sum) / (otherScores size)

      experimentScore / meanOtherScore
    }

    ///////////////////////////////////////////

    val imageClasses = Seq(
      "graffiti",
      "trees",
      "jpeg",
      "boat",
      "bark",
      "bikes",
      "light",
      "wall")

    val otherImages = Seq(2, 3, 4, 5, 6)

    val relativeScores = (for (
      imageClass <- imageClasses.par;
      otherImage <- otherImages.par
    ) yield relativeScore(imageClass, otherImage)).toList

    (relativeScores sum) / (relativeScores size)
  }
}