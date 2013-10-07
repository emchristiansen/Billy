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
case class Experiment[D <% Detector, E <% Extractor[F], M <% Matcher[F], F](
  imageClass: String,
  otherImage: Int,
  detector: D,
  extractor: E,
  matcher: M) {
  def run(implicit runtimeConfig: RuntimeConfig): Results = {
    ???
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
  }
}
  
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
