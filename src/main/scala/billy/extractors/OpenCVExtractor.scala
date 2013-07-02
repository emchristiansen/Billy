package billy.extractors
 

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
import imageProcessing.Pixel

import nebula.util.JSONUtil.AddClassName
import nebula.util.JSONUtil.singletonObject
import spray.json.DefaultJsonProtocol
import spray.json.JsonFormat
import util.OpenCVUtil
import util.Util
import nebula.util._

///////////////////////////////////////////////////////////

/**
 * Represents descriptor extraction algorithms available in OpenCV.
 */
trait OpenCVExtractor {
  object BRISK
  object FREAK
  object BRIEF
  object ORB
  object SIFT
  object SURF
}

/**
 * Views to Extractor.
 */
trait OpenCVExtractor2Extractor {
  implicit def openCVExtractorBrisk2Extractor(self: OpenCVExtractor.BRISK.type) =
    Extractor.fromAction(Extractor.booleanExtractorSeveralFromEnum(DescriptorExtractor.BRISK))
  implicit def openCVExtractorFreak2Extractor(self: OpenCVExtractor.FREAK.type) =
    Extractor.fromAction(Extractor.booleanExtractorSeveralFromEnum(DescriptorExtractor.FREAK))
  implicit def openCVExtractorBrief2Extractor(self: OpenCVExtractor.BRIEF.type) =
    Extractor.fromAction(Extractor.booleanExtractorSeveralFromEnum(DescriptorExtractor.BRIEF))
  implicit def openCVExtractorOrb2Extractor(self: OpenCVExtractor.ORB.type) =
    Extractor.fromAction(Extractor.booleanExtractorSeveralFromEnum(DescriptorExtractor.ORB))
  implicit def openCVExtractorSift2Extractor(self: OpenCVExtractor.SIFT.type) =
    Extractor.fromAction(Extractor.doubleExtractorSeveralFromEnum(DescriptorExtractor.SIFT))
  implicit def openCVExtractorSurf2Extractor(self: OpenCVExtractor.SURF.type) =
    Extractor.fromAction(Extractor.doubleExtractorSeveralFromEnum(DescriptorExtractor.SURF))
  //  implicit def openCVExtractorBrisk2Extractor(self: OpenCVExtractor.BRISK.type) =
  //    Extractor(Extractor.booleanExtractorFromEnum(DescriptorExtractor.BRISK))
  //  implicit def openCVExtractorFreak2Extractor(self: OpenCVExtractor.FREAK.type) =
  //    Extractor(Extractor.booleanExtractorFromEnum(DescriptorExtractor.FREAK))
  //  implicit def openCVExtractorBrief2Extractor(self: OpenCVExtractor.BRIEF.type) =
  //    Extractor(Extractor.booleanExtractorFromEnum(DescriptorExtractor.BRIEF))
  //  implicit def openCVExtractorOrb2Extractor(self: OpenCVExtractor.ORB.type) =
  //    Extractor(Extractor.booleanExtractorFromEnum(DescriptorExtractor.ORB))
  //  implicit def openCVExtractorSift2Extractor(self: OpenCVExtractor.SIFT.type) =
  //    Extractor(Extractor.doubleExtractorFromEnum(DescriptorExtractor.SIFT))
  //  implicit def openCVExtractorSurf2Extractor(self: OpenCVExtractor.SURF.type) =
  //    Extractor(Extractor.doubleExtractorFromEnum(DescriptorExtractor.SURF))
}

/**
 * Implementations of JsonFormat.
 */
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

object OpenCVExtractor extends OpenCVExtractor with OpenCVExtractor2Extractor with OpenCVExtractorJsonProtocol