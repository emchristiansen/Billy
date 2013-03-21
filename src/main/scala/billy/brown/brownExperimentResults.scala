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
import spray.json._
import billy.ExperimentRunner
import billy.Extractor
import billy.Matcher

import billy.RuntimeConfig
import billy.StorageInfo
import scala.Option.option2Iterable
import spray.json._
import nebula.util.JSONUtil.AddClassName
import DMatchJsonProtocol._

///////////////////////////////////////////////////////////

/**
 * Holds the results of a Brown experiment.
 */
case class BrownExperimentResults[E, M, F](
  experiment: BrownExperiment[E, M, F],
  dmatches: Seq[DMatch])

/**
 * Views to ExperimentSummary.
 */
trait BrownExperimentResults2ExperimentSummary {
  implicit def brownExperimentResults2ExperimentSummary[E, M, F](
    self: BrownExperimentResults[E, M, F])(runtimeConfig: RuntimeConfig) = {
    implicit val iRC = runtimeConfig
    ExperimentSummary(
      Map(
        "errorRateAtRecall95" -> (SummaryUtil.errorRateAtRecall(
          0.95,
          self.dmatches))),
      Map())
  }

  // TODO: Remove when Scala inference bug is fixed.
  implicit def WTFBrownExperimentResults2ExperimentSummary[E, M, F](
    self: BrownExperimentResults[E, M, F])(implicit runtimeConfig: RuntimeConfig) =
    brownExperimentResults2ExperimentSummary(self)(runtimeConfig)
}

/**
 * Implementations of JsonProtocol.
 */
trait BrownExperimentResultsJsonProtocol extends DefaultJsonProtocol {
  implicit def brownExperimentResultsJsonProtocol[E <% Extractor[F]: JsonFormat, M <% Matcher[F]: JsonFormat, F] =
    jsonFormat2(BrownExperimentResults.apply[E, M, F]).addClassInfo(
      "BrownExperimentResults")
}

object BrownExperimentResults extends BrownExperimentResults2ExperimentSummary with BrownExperimentResultsJsonProtocol {
  /**
   * Creates a BrownExperimentResults from a BrownExperiment by running the
   * experiment.
   */
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
}