package st.sparse.billy.detectors

import spray.json._
import st.sparse.billy._
import st.sparse.billy.PairDetector
import st.sparse.billy.Matcher
import st.sparse.sundry._
import scala.pickling.FastTypeTag

trait JsonProtocol extends DefaultJsonProtocol {
  implicit def openCVDetectorDENSEFormat = jsonFormat0(OpenCVDetector.DENSE)
  implicit def openCVDetectorFASTFormat = jsonFormat0(OpenCVDetector.FAST)
  implicit def openCVDetectorBRISKFormat = jsonFormat0(OpenCVDetector.BRISK)
  implicit def openCVDetectorSIFTFormat = jsonFormat0(OpenCVDetector.SIFT)
  implicit def openCVDetectorSURFFormat = jsonFormat0(OpenCVDetector.SURF)
  implicit def openCVDetectorORBFormat = jsonFormat0(OpenCVDetector.ORB)

  //  implicit def boundedDetectorFormat[D <% Detector: JsonFormat] =
  //    jsonFormat2(BoundedDetector.apply[D])

  implicit def boundedDetectorFormat[D <% Detector: JsonFormat] =
    jsonFormat(BoundedDetector.apply[D], "maxKeyPoints", "detector")

  implicit def boundedPairDetectorFormat[D <% Detector: JsonFormat] =
    jsonFormat3(BoundedPairDetector.apply[D])

  implicit def doublyBoundedPairDetectorFormat[D <% Detector: JsonFormat] =
    jsonFormat(
      DoublyBoundedPairDetector.apply[D],
      "threshold",
      "pairMaxKeyPoints",
      "individualMaxKeyPoints",
      "detector")
}
