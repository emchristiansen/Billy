package billy.experiments.wideBaseline

import java.io.File

import nebula._
import nebula.imageProcessing._
import nebula.util._

import billy._
import billy.brown._

import billy.smallBaseline._
import billy.wideBaseline._
import billy.summary._

import org.opencv.features2d.DMatch

import javax.imageio.ImageIO

import nebula.util.Homography
import nebula.util.Logging
import nebula.util.Memoize

///////////////////////////////////////////////////////////

case class WideBaselineExperimentResults[D, E, M, F](
  experiment: WideBaselineExperiment[D, E, M, F],
  dmatches: Seq[DMatch])

trait WideBaselineExperimentResults2ExperimentSummary {
  implicit def implicitExperimentSummary[D, E, M, F](
    self: WideBaselineExperimentResults[D, E, M, F])(
      runtimeConfig: RuntimeConfig) = {
    implicit val iRC = runtimeConfig
    ExperimentSummary(
      Map(
        "recognitionRate" -> (SummaryUtil.recognitionRate(self.dmatches))),
      // TODO
//      Map(
//        "histogram" -> (Histogram(self, "").render)),
      Map("precisionRecall" -> SummaryUtil.precisionRecall(self.dmatches)),
      Map())
  }

  // TODO: Remove when Scala inference bug is fixed.
  implicit def WTFImplicitWideBaselineExperimentSummary[D, E, M, F](
    self: WideBaselineExperimentResults[D, E, M, F])(
      implicit runtimeConfig: RuntimeConfig) = implicitExperimentSummary(self)(runtimeConfig)
}

object WideBaselineExperimentResults extends WideBaselineExperimentResults2ExperimentSummary {
  def apply[D <% PairDetector, E <% Extractor[F], M <% Matcher[F], F](
    experiment: WideBaselineExperiment[D, E, M, F])(
      implicit runtimeConfig: RuntimeConfig): WideBaselineExperimentResults[D, E, M, F] = {
    if (runtimeConfig.skipCompletedExperiments && 
        experiment.mostRecentPath.isDefined) {
      experiment.load.get 
    } else {
      // TODO: Don't hardcode saving here.
      val results = run(experiment)
      experiment.save(results)
      results
    }
  }

  private def run[D, E, M, F](
    self: WideBaselineExperiment[D, E, M, F])(
      implicit runtimeConfig: RuntimeConfig,
      evPairDetector: D => PairDetector,
      evExtractor: E => Extractor[F],
      evMatcher: M => Matcher[F]): WideBaselineExperimentResults[D, E, M, F] = {
    println(s"Running ${self}")

    val leftImage = self.leftImage
    val rightImage = self.rightImage

    val (leftKeyPoints, rightKeyPoints) = self.detector.detectPair(
      self.groundTruth,
      leftImage,
      rightImage) unzip

    println(s"Number of KeyPoints: ${leftKeyPoints.size}")

    val (leftDescriptors, rightDescriptors) = {
      val leftDescriptors = self.extractor.extract(leftImage, leftKeyPoints)
      val rightDescriptors = self.extractor.extract(rightImage, rightKeyPoints)

      for ((Some(left), Some(right)) <- leftDescriptors.zip(rightDescriptors)) yield (left, right)
    } unzip

    println(s"Number of surviving KeyPoints: ${leftDescriptors.size}")

    val dmatches = self.matcher.doMatch(true, leftDescriptors, rightDescriptors)

    WideBaselineExperimentResults(self, dmatches)
  }
}
