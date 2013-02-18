package billy.brown

import nebula._
import nebula.imageProcessing._
import nebula.util._

import billy._
import billy.brown._
import billy.mpie._
import billy.smallBaseline._
import billy.wideBaseline._
import billy.summary._

import org.opencv.features2d.DMatch
import nebula.util._
import billy.summary._
import billy.JsonProtocols._
import spray.json._
import billy.ExperimentRunner
import billy.Extractor
import billy.Matcher

import billy.RuntimeConfig
import billy.StorageInfo
import scala.Option.option2Iterable


///////////////////////////////////////////////////////////

case class BrownExperiment[E <% Extractor[F], M <% Matcher[F], F](
  dataset: String,
  numMatches: Int,
  extractor: E,
  matcher: M)

object BrownExperiment {
  implicit class BrownExperiment2ExperimentRunner[E <% Extractor[F], M <% Matcher[F], F](
    self: BrownExperiment[E, M, F])(
      runtimeConfig: RuntimeConfig) extends ExperimentRunner[BrownExperimentResults[E, M, F]] {
    private implicit val iRC = runtimeConfig

    override def run = BrownExperimentResults(self)
  }
      
  implicit def WTFBrownExperiment2ExperimentRunner[E <% Extractor[F], M <% Matcher[F], F](
    self: BrownExperiment[E, M, F])(
      implicit runtimeConfig: RuntimeConfig) = new BrownExperiment2ExperimentRunner(self)(runtimeConfig)

  // TODO: Refactor when Scala type inference bug is fixed. 
  implicit def WTFBrownExperiment2StorageInfo[E <% Extractor[F]: JsonFormat, M <% Matcher[F]: JsonFormat, F](
    self: BrownExperiment[E, M, F])(
      runtimeConfig: RuntimeConfig) = new StorageInfo.Experiment2StorageInfo(self)(runtimeConfig)      

  // TODO: Refactor when Scala type inference bug is fixed.      
  implicit def WTFBrownExperiment2StorageInfoImplicit[E <% Extractor[F]: JsonFormat, M <% Matcher[F]: JsonFormat, F](
    self: BrownExperiment[E, M, F])(
      implicit runtimeConfig: RuntimeConfig) = new StorageInfo.Experiment2StorageInfo(self)(runtimeConfig)
}

///////////////////////////////////////////////////////////

case class BrownExperimentResults[E, M, F](
  experiment: BrownExperiment[E, M, F],
  dmatches: Seq[DMatch])

object BrownExperimentResults {
  def apply[E <% Extractor[F], M <% Matcher[F], F](
    experiment: BrownExperiment[E, M, F])(
      implicit runtimeConfig: RuntimeConfig): BrownExperimentResults[E, M, F] = {
    val patchPairs = PatchPair.loadPatchPairs(
      experiment.dataset,
      experiment.numMatches,
      runtimeConfig.dataRoot)

    val dmatchOptions = patchPairs map {
      patchPair =>
        {
          val distance = patchPair.getDistance(experiment.extractor, experiment.matcher)
          for (d <- distance) yield new DMatch(patchPair.left.id, patchPair.right.id, d.toFloat)
        }
    }

    BrownExperimentResults(experiment, dmatchOptions.flatten)
  }

  implicit def brownExperimentResults2ExperimentSummary[E, M, F](
    self: BrownExperimentResults[E, M, F])(runtimeConfig: RuntimeConfig) = {
    implicit val iRC = runtimeConfig
    ExperimentSummary(
      Map(
        "errorRateAtRecall95" -> (() => SummaryUtil.errorRateAtRecall(0.95, self.dmatches))),
      Map())
  }
  
    // TODO: Remove when Scala inference bug is fixed.
  implicit def WTFBrownExperimentResults2ExperimentSummary[E, M, F](
    self: BrownExperimentResults[E, M, F])(implicit runtimeConfig: RuntimeConfig) = 
      brownExperimentResults2ExperimentSummary(self)(runtimeConfig)
}

