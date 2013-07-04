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
import billy.ExperimentRunner
import billy.Extractor
import billy.Matcher

import billy.RuntimeConfig
import billy.StorageInfo
import scala.Option.option2Iterable

///////////////////////////////////////////////////////////

/**
 * Information needed to run an experiment on the Brown dataset.
 */
case class BrownExperiment[E <% Extractor[F], M <% Matcher[F], F](
  dataset: String,
  numMatches: Int,
  extractor: E,
  matcher: M)

/**
 * Views to ExperimentRunner.
 */
trait BrownExperiment2ExperimentRunner {
  implicit class BrownExperiment2ExperimentRunner[E <% Extractor[F], M <% Matcher[F], F](
    self: BrownExperiment[E, M, F])(
      runtimeConfig: RuntimeConfig) extends ExperimentRunner[BrownExperimentResults[E, M, F]] {
    private implicit val iRC = runtimeConfig

    override def run = BrownExperimentResults(self)
  }

  implicit def WTFBrownExperiment2ExperimentRunner[E <% Extractor[F], M <% Matcher[F], F](
    self: BrownExperiment[E, M, F])(
      implicit runtimeConfig: RuntimeConfig) =
    new BrownExperiment2ExperimentRunner(self)(runtimeConfig)
}

/**
 * Views to StorageInfo.
 */
trait BrownExperiment2StorageInfo { 
  // TODO: Refactor when Scala type inference bug is fixed. 
  implicit def WTFBrownExperiment2StorageInfo[E <% Extractor[F], M <% Matcher[F], F](
    self: BrownExperiment[E, M, F])(
      runtimeConfig: RuntimeConfig) =
    new StorageInfo.Experiment2StorageInfo(self)(runtimeConfig)

  // TODO: Refactor when Scala type inference bug is fixed.      
//  implicit def WTFBrownExperiment2StorageInfoImplicit[E <% Extractor[F]: JsonFormat, M <% Matcher[F]: JsonFormat, F](
//    self: BrownExperiment[E, M, F])(
//      implicit runtimeConfig: RuntimeConfig) =
//    new StorageInfo.Experiment2StorageInfo(self)(runtimeConfig)
}

object BrownExperiment extends BrownExperiment2ExperimentRunner with BrownExperiment2StorageInfo