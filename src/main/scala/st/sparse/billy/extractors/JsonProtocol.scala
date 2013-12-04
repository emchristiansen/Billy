package st.sparse.billy.extractors

import spray.json._
import st.sparse.billy._
import st.sparse.billy.PairDetector
import st.sparse.billy.Matcher
import st.sparse.sundry._
import scala.pickling.FastTypeTag

trait JsonProtocol extends DefaultJsonProtocol {
  implicit def andExtractorFormat[E1 <% Extractor[F1]: JsonFormat, E2 <% Extractor[F2]: JsonFormat, F1, F2] =
    jsonFormat2(AndExtractor.apply[E1, E2, F1, F2])
  
  implicit def openCVExtractorBRISKFormat = jsonFormat0(OpenCVExtractor.BRISK)
  implicit def openCVExtractorFREAKFormat = jsonFormat0(OpenCVExtractor.FREAK)
  implicit def openCVExtractorBRIEFFormat = jsonFormat0(OpenCVExtractor.BRIEF)
  implicit def openCVExtractorORBFormat = jsonFormat0(OpenCVExtractor.ORB)
  implicit def openCVExtractorSIFTFormat = jsonFormat0(OpenCVExtractor.SIFT)
  implicit def openCVExtractorSURFFormat = jsonFormat0(OpenCVExtractor.SURF)
  
  implicit def patchExtractorFormat = jsonFormat3(PatchExtractor.apply)
  
//  implicit def foregroundMaskExtractorFormat = 
//    jsonFormat1(ForegroundMaskExtractor.apply)
  
  implicit def briefExtractorFormat = jsonFormat3(BRIEFExtractor)
  
  // TODO: Add other extractors and adapters.
}
