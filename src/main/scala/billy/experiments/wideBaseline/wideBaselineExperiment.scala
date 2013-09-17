package billy.experiments.wideBaseline

import billy._
import billy.experiments._

import java.io.File

import scalatestextra._

import com.sksamuel.scrimage.Image

import scala.pickling._
import binary._

///////////////////////////////////////////////////////////

/**
 * Represents experiments on the Oxford image dataset.
 */
case class WideBaselineExperiment[F](
  imageClass: String,
  otherImage: Int,
  detector: Detector,
  extractor: Extractor[F],
  matcher: Matcher[F]) {
//  (
//    implicit runtimeConfig: RuntimeConfig)
//  def groundTruthHomography = Homography.fromFile(ExistingFile(new File(
//    runtimeConfig.dataRoot,
//    s"oxfordImages/${imageClass}/homographies/H1to${otherImage}p")))
//
//  def leftImage = Image(ExistingFile(new File(
//    runtimeConfig.dataRoot,
//    s"oxfordImages/${imageClass}/images/img1.bmp")))
//
//  def rightImage = Image(ExistingFile(new File(
//    runtimeConfig.dataRoot,
//    s"oxfordImages/${imageClass}/images/img${otherImage}.bmp")))
}

object Bar {
//  def foo[D: SPickler: Unpickler: FastTypeTag, E: SPickler: Unpickler: FastTypeTag, M: SPickler: Unpickler: FastTypeTag, F: SPickler: Unpickler: FastTypeTag](experiment: WideBaselineExperiment[D, E, M, F]) {
//    experiment.pickle
//  }
}

//trait WideBaselineExperiment2HasGroundTruth {
//  implicit def wideBaselineExperiment2HasGroundTruth(
//    self: WideBaselineExperiment[_, _, _, _])(
//      implicit runtime: RuntimeConfig): HasGroundTruth[Homography] =
//    new HasGroundTruth[Homography] {
//      override def groundTruth = {
//        val homographyFile = new File(
//          runtime.dataRoot,
//          s"oxfordImages/${self.imageClass}/homographies/H1to${self.otherImage}p").mustExist
//        Homography.fromFile(homographyFile)
//      }
//    }
//}

//trait WideBaselineExperiment2ExperimentRunner {
  // TODO: JsonFormat should not be required to run experiments.
  // TODO: Remote workers should get their data over the wire, and thus not
  // take a RuntimeConfig.
//  implicit class WideBaselineExperiment2ExperimentRunner[D <% PairDetector, E <% Extractor[F], M <% Matcher[F], F](
//    self: WideBaselineExperiment[D, E, M, F])(
//      runtimeConfig: RuntimeConfig) extends ExperimentRunner[WideBaselineExperimentResults[D, E, M, F]] {
//    private implicit val iRC = runtimeConfig
//
//    override def run = WideBaselineExperimentResults(self)
//  }

  // TODO: Refactor when Scala inference bug is fixed.
//  implicit def WTFWideBaselineExperiment2ExperimentRunner[D <% PairDetector, E <% Extractor[F], M <% Matcher[F], F](
//    self: WideBaselineExperiment[D, E, M, F])(
//      implicit runtimeConfig: RuntimeConfig) =
//    new WideBaselineExperiment2ExperimentRunner(self)(runtimeConfig)
//}

//trait WideBaselineExperiment2StorageInfo {
  // TODO: Refactor when Scala type inference bug is fixed. 
//  implicit def WTFWideBaselineExperiment2StorageInfo[D <% PairDetector, E <% Extractor[F], M <% Matcher[F], F](
//    self: WideBaselineExperiment[D, E, M, F])(
//      runtimeConfig: RuntimeConfig) =
//    new StorageInfo.Experiment2StorageInfo(self)(runtimeConfig)
//
//  // TODO: Refactor when Scala type inference bug is fixed.      
//  implicit def WTFWideBaselineExperiment2StorageInfoImplicit[D <% PairDetector, E <% Extractor[F], M <% Matcher[F], F](
//    self: WideBaselineExperiment[D, E, M, F])(
//      implicit runtimeConfig: RuntimeConfig) =
//    new StorageInfo.Experiment2StorageInfo(self)(runtimeConfig)
//}

//trait WideBaselineExperiment2ImagePairLike {
//  implicit class ImplicitImagePairLike(
//    self: WideBaselineExperiment[_, _, _, _])(
//      implicit runtime: RuntimeConfig) extends HasImagePair {
//    override def leftImage = {
//      val file = new File(
//        runtime.dataRoot,
//        s"oxfordImages/${self.imageClass}/images/img1.bmp").mustExist
//      Image.read(file)
//    }
//    override def rightImage = {
//      val file = new File(
//        runtime.dataRoot,
//        s"oxfordImages/${self.imageClass}/images/img${self.otherImage}.bmp").mustExist
//      Image.read(file)
//    }
//  }
//}
//
//object WideBaselineExperiment extends WideBaselineExperiment2HasGroundTruth with WideBaselineExperiment2ExperimentRunner with WideBaselineExperiment2StorageInfo with WideBaselineExperiment2ImagePairLike
