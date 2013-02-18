package billy.extractors

import java.awt.image.BufferedImage

import nebula._
import nebula.imageProcessing._
import nebula.util._

import billy._
import billy.brown._
import billy.mpie._
import billy.smallBaseline._
import billy.wideBaseline._
import billy.summary._

import nebula._
import org.opencv.core.Mat
import org.opencv.core.MatOfKeyPoint
import org.opencv.features2d.DescriptorExtractor
import org.opencv.features2d.KeyPoint

import breeze.linalg.DenseMatrix
import breeze.linalg.DenseVector
import imageProcessing.ImageUtil
import imageProcessing.Pixel

import nebula.imageProcessing.RichImage.bufferedImage
import nebula.util.JSONUtil.AddClassName
import nebula.util.JSONUtil.singletonObject
import spray.json.DefaultJsonProtocol
import spray.json.JsonFormat
import util.OpenCVUtil
import util.Util
import nebula.util._

///////////////////////////////////////////////////////////

object OpenCVExtractor {
  object BRISK
  object FREAK
  object BRIEF
  object ORB
  object SIFT
  object SURF

  implicit def openCVExtractorBrisk2Extractor(self: BRISK.type) =
    Extractor(Extractor.booleanExtractorFromEnum(DescriptorExtractor.BRISK))
  implicit def openCVExtractorFreak2Extractor(self: FREAK.type) =
    Extractor(Extractor.booleanExtractorFromEnum(DescriptorExtractor.FREAK))
  implicit def openCVExtractorBrief2Extractor(self: BRIEF.type) =
    Extractor(Extractor.booleanExtractorFromEnum(DescriptorExtractor.BRIEF))
  implicit def openCVExtractorOrb2Extractor(self: ORB.type) =
    Extractor(Extractor.booleanExtractorFromEnum(DescriptorExtractor.ORB))
  implicit def openCVExtractorSift2Extractor(self: SIFT.type) =
    Extractor(Extractor.doubleExtractorFromEnum(DescriptorExtractor.SIFT))
  implicit def openCVExtractorSurf2Extractor(self: SURF.type) =
    Extractor(Extractor.doubleExtractorFromEnum(DescriptorExtractor.SURF))
}

trait OpenCVExtractorJsonProtocol extends DefaultJsonProtocol {
  implicit val openCVExtractorBriskJsonProtocol =
    singletonObject(OpenCVExtractor.BRISK)
  implicit val openCVExtractorFreakJsonProtocol =
    singletonObject(OpenCVExtractor.FREAK)
  implicit val openCVExtractorBriefJsonProtocol =
    singletonObject(OpenCVExtractor.BRIEF)
  implicit val openCVExtractorOrbJsonProtocol =
    singletonObject(OpenCVExtractor.ORB)
  implicit val openCVExtractorSiftJsonProtocol =
    singletonObject(OpenCVExtractor.SIFT)
  implicit val openCVExtractorSurfJsonProtocol =
    singletonObject(OpenCVExtractor.SURF)
}

object OpenCVExtractorJsonProtocol extends OpenCVExtractorJsonProtocol