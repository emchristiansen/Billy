package billy.experiments.wideBaseline

import billy._

import breeze.linalg._

import java.io.File

import scalatestextra._

/////////////////////////////////////////////////////////////

case class Results(distances: DenseMatrix[Double])

//
//case class WideBaselineExperimentResults[D, E, M, F](
//  experiment: WideBaselineExperiment[D, E, M, F],
//  distances: DenseMatrix[Double])
//
//object WideBaselineExperimentResults {
////  private def connectToPersistentMap[D: SPickler: Unpickler: FastTypeTag, E: SPickler: Unpickler: FastTypeTag, M: SPickler: Unpickler: FastTypeTag, F: SPickler: Unpickler: FastTypeTag](
////    implicit runtimeConfig: RuntimeConfig) =
////    PersistentMap.connectElseCreate[WideBaselineExperiment[D, E, M, F], (Date, WideBaselineExperimentResults[D, E, M, F])](
////      "billyWideBaseline",
////      runtimeConfig.database)
//
//  private def run[D <% PairDetector, E <% Extractor[F], M <% Matcher[F], F](
//    experiment: WideBaselineExperiment[D, E, M, F])(
//      implicit runtimeConfig: RuntimeConfig): WideBaselineExperimentResults[D, E, M, F] = {
//    // TODO: Use a logger.
//    println(s"Running ${experiment}")
//
//    val leftImage = experiment.leftImage
//    val rightImage = experiment.rightImage
//
//    val (leftKeyPoints, rightKeyPoints) = experiment.detector.detectPair(
//      experiment.groundTruthHomography,
//      leftImage,
//      rightImage) unzip
//
//    println(s"Number of KeyPoints: ${leftKeyPoints.size}")
//
//    val (leftDescriptors, rightDescriptors) = {
//      val leftDescriptors = experiment.extractor.extract(leftImage, leftKeyPoints)
//      val rightDescriptors = experiment.extractor.extract(rightImage, rightKeyPoints)
//
//      for ((Some(left), Some(right)) <- leftDescriptors.zip(rightDescriptors)) yield (left, right)
//    } unzip
//
//    println(s"Number of surviving KeyPoints: ${leftDescriptors.size}")
//
//    val distances = experiment.matcher.matchAll(
//      leftDescriptors,
//      rightDescriptors)
//
//    WideBaselineExperimentResults(experiment, distances)
//  }
//
//  def load[D <% PairDetector, E <% Extractor[F], M <% Matcher[F], F](
//    experiment: WideBaselineExperiment[D, E, M, F])(
//      implicit runtimeConfig: RuntimeConfig): Option[WideBaselineExperimentResults[D, E, M, F]] = {
//    ???
//  }
//
//  def loadOrRunAndSave[D <% PairDetector, E <% Extractor[F], M <% Matcher[F], F](
//    experiment: WideBaselineExperiment[D, E, M, F])(
//      implicit runtimeConfig: RuntimeConfig): WideBaselineExperimentResults[D, E, M, F] = {
//    ???
//  }
//}
//
////trait WideBaselineExperimentResults2ExperimentSummary {
////  implicit def implicitExperimentSummary[D, E, M, F](
////    self: WideBaselineExperimentResults[D, E, M, F])(
////      runtimeConfig: RuntimeConfig) = {
////    implicit val iRC = runtimeConfig
////    ExperimentSummary(
////      Map(
////        "recognitionRate" -> (SummaryUtil.recognitionRate(self.dmatches))),
////      // TODO
////      //      Map(
////      //        "histogram" -> (Histogram(self, "").render)),
////      Map("precisionRecall" -> SummaryUtil.precisionRecall(self.dmatches)),
////      Map())
////  }
////
////  // TODO: Remove when Scala inference bug is fixed.
////  implicit def WTFImplicitWideBaselineExperimentSummary[D, E, M, F](
////    self: WideBaselineExperimentResults[D, E, M, F])(
////      implicit runtimeConfig: RuntimeConfig) = implicitExperimentSummary(self)(runtimeConfig)
////}
////
////object WideBaselineExperimentResults extends WideBaselineExperimentResults2ExperimentSummary {
////  def apply[D <% PairDetector, E <% Extractor[F], M <% Matcher[F], F](
////    experiment: WideBaselineExperiment[D, E, M, F])(
////      implicit runtimeConfig: RuntimeConfig): WideBaselineExperimentResults[D, E, M, F] = {
////    if (runtimeConfig.skipCompletedExperiments &&
////      experiment.mostRecentPath.isDefined) {
////      experiment.load.get
////    } else {
////      // TODO: Don't hardcode saving here.
////      val results = run(experiment)
////      experiment.save(results)
////      results
////    }
////  }
////}
