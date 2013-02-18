package billy.wideBaseline

import java.io.File

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

import WideBaselineJsonProtocol.wideBaselineExperiment
import WideBaselineJsonProtocol.wideBaselineExperimentResults
import javax.imageio.ImageIO

import nebula.util.Homography
import nebula.util.JSONUtil
import nebula.util.Logging
import nebula.util.Memoize
import spray.json.JsonFormat
import spray.json.pimpAny
import spray.json.pimpString

///////////////////////////////////////////////////////////

case class WideBaselineExperiment[D <% PairDetector, E <% Extractor[F], M <% Matcher[F], F](
  imageClass: String,
  otherImage: Int,
  detector: D,
  extractor: E,
  matcher: M)

object WideBaselineExperiment {
  implicit def implicitHasGroundTruth(
    self: WideBaselineExperiment[_, _, _, _])(
      implicit runtime: RuntimeConfig): HasGroundTruth[Homography] =
    new HasGroundTruth[Homography] {
      override def groundTruth = {
        val homographyFile = new File(
          runtime.dataRoot,
          s"oxfordImages/${self.imageClass}/homographies/H1to${self.otherImage}p").mustExist
        Homography.fromFile(homographyFile)
      }
    }

  implicit class WideBaselineExperiment2ExperimentRunner[D <% PairDetector, E <% Extractor[F], M <% Matcher[F], F](
    self: WideBaselineExperiment[D, E, M, F])(
      runtimeConfig: RuntimeConfig) extends ExperimentRunner[WideBaselineExperimentResults[D, E, M, F]] {
    private implicit val iRC = runtimeConfig

    override def run = WideBaselineExperimentResults(self)
  }

  // TODO: Refactor when Scala inference bug is fixed.
  implicit def WTFWideBaselineExperiment2ExperimentRunner[D <% PairDetector, E <% Extractor[F], M <% Matcher[F], F](
    self: WideBaselineExperiment[D, E, M, F])(
      implicit runtimeConfig: RuntimeConfig) = new WideBaselineExperiment2ExperimentRunner(self)(runtimeConfig)

  // TODO: Refactor when Scala type inference bug is fixed. 
  implicit def WTFWideBaselineExperiment2StorageInfo[D <% PairDetector: JsonFormat, E <% Extractor[F]: JsonFormat, M <% Matcher[F]: JsonFormat, F](
    self: WideBaselineExperiment[D, E, M, F])(
      runtimeConfig: RuntimeConfig) = new StorageInfo.Experiment2StorageInfo(self)(runtimeConfig)      

  // TODO: Refactor when Scala type inference bug is fixed.      
  implicit def WTFWideBaselineExperiment2StorageInfoImplicit[D <% PairDetector: JsonFormat, E <% Extractor[F]: JsonFormat, M <% Matcher[F]: JsonFormat, F](
    self: WideBaselineExperiment[D, E, M, F])(
      implicit runtimeConfig: RuntimeConfig) = new StorageInfo.Experiment2StorageInfo(self)(runtimeConfig)            
      
  implicit class ImplicitImagePairLike(
    self: WideBaselineExperiment[_, _, _, _])(
      implicit runtime: RuntimeConfig) extends HasImagePair {
    override def leftImage = {
      val file = new File(
        runtime.dataRoot,
        s"oxfordImages/${self.imageClass}/images/img1.bmp").mustExist
      ImageIO.read(file)
    }
    override def rightImage = {
      val file = new File(
        runtime.dataRoot,
        s"oxfordImages/${self.imageClass}/images/img${self.otherImage}.bmp").mustExist
      ImageIO.read(file)
    }
  }
}

///////////////////////////////////////////////////////////

case class WideBaselineExperimentResults[D, E, M, F](
  experiment: WideBaselineExperiment[D, E, M, F],
  dmatches: Seq[DMatch])

object WideBaselineExperimentResults extends Logging {
  def apply[D, E, M, F](
    experiment: WideBaselineExperiment[D, E, M, F])(
      implicit runtimeConfig: RuntimeConfig,
      evPairDetector: D => PairDetector,
      evExtractor: E => Extractor[F],
      evMatcher: M => Matcher[F]): WideBaselineExperimentResults[D, E, M, F] = {
    run(experiment)
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

  implicit def implicitExperimentSummary[D, E, M, F](
    self: WideBaselineExperimentResults[D, E, M, F])(
      runtimeConfig: RuntimeConfig) = {
    implicit val iRC = runtimeConfig
    ExperimentSummary(
      Map(
        "recognitionRate" -> (() => SummaryUtil.recognitionRate(self.dmatches))),
      Map(
        "histogram" -> (() => Histogram(self, "").render)))
  }

  // TODO: Remove when Scala inference bug is fixed.
  implicit def WTFImplicitExperimentSummary[D, E, M, F](
    self: WideBaselineExperimentResults[D, E, M, F])(
      implicit runtimeConfig: RuntimeConfig) = implicitExperimentSummary(self)(runtimeConfig)
}





