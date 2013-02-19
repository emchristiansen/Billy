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

import javax.imageio.ImageIO

import nebula.util.Homography
import nebula.util.JSONUtil
import nebula.util.Logging
import nebula.util.Memoize
import spray.json.JsonFormat
import spray.json.pimpAny
import spray.json.pimpString
import spray.json._
import nebula.util.JSONUtil._
import nebula.util.DMatchJsonProtocol._

///////////////////////////////////////////////////////////

/**
 * Represents experiments on the Oxford image dataset.
 */
case class WideBaselineExperiment[D <% PairDetector, E <% Extractor[F], M <% Matcher[F], F](
  imageClass: String,
  otherImage: Int,
  detector: D,
  extractor: E,
  matcher: M)

trait WideBaselineExperiment2HasGroundTruth {
  implicit def wideBaselineExperiment2HasGroundTruth(
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
}

trait WideBaselineExperiment2ExperimentRunner {
  implicit class WideBaselineExperiment2ExperimentRunner[D <% PairDetector, E <% Extractor[F], M <% Matcher[F], F](
    self: WideBaselineExperiment[D, E, M, F])(
      runtimeConfig: RuntimeConfig) extends ExperimentRunner[WideBaselineExperimentResults[D, E, M, F]] {
    private implicit val iRC = runtimeConfig

    override def run = WideBaselineExperimentResults(self)
  }

  // TODO: Refactor when Scala inference bug is fixed.
  implicit def WTFWideBaselineExperiment2ExperimentRunner[D <% PairDetector, E <% Extractor[F], M <% Matcher[F], F](
    self: WideBaselineExperiment[D, E, M, F])(
      implicit runtimeConfig: RuntimeConfig) =
    new WideBaselineExperiment2ExperimentRunner(self)(runtimeConfig)
}

trait WideBaselineExperiment2StorageInfo {
  // TODO: Refactor when Scala type inference bug is fixed. 
  implicit def WTFWideBaselineExperiment2StorageInfo[D <% PairDetector: JsonFormat, E <% Extractor[F]: JsonFormat, M <% Matcher[F]: JsonFormat, F](
    self: WideBaselineExperiment[D, E, M, F])(
      runtimeConfig: RuntimeConfig) =
    new StorageInfo.Experiment2StorageInfo(self)(runtimeConfig)

  // TODO: Refactor when Scala type inference bug is fixed.      
  implicit def WTFWideBaselineExperiment2StorageInfoImplicit[D <% PairDetector: JsonFormat, E <% Extractor[F]: JsonFormat, M <% Matcher[F]: JsonFormat, F](
    self: WideBaselineExperiment[D, E, M, F])(
      implicit runtimeConfig: RuntimeConfig) =
    new StorageInfo.Experiment2StorageInfo(self)(runtimeConfig)
}

trait WideBaselineExperiment2ImagePairLike {
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

trait WideBaselineExperimentJsonProtocol extends DefaultJsonProtocol {
  implicit def wideBaselineExperiment[D <% PairDetector: JsonFormat, E <% Extractor[F]: JsonFormat, M <% Matcher[F]: JsonFormat, F] =
    jsonFormat5(
      WideBaselineExperiment.apply[D, E, M, F]).addClassInfo(
        "WideBaselineExperiment")
}

object WideBaselineExperiment extends WideBaselineExperiment2HasGroundTruth with WideBaselineExperiment2ExperimentRunner with WideBaselineExperiment2StorageInfo with WideBaselineExperiment2ImagePairLike with WideBaselineExperimentJsonProtocol