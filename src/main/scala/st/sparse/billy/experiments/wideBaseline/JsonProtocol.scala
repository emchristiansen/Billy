package st.sparse.billy.experiments.wideBaseline

import spray.json._
import spray.json.DefaultJsonProtocol
import st.sparse.billy.Extractor
import st.sparse.billy.PairDetector
import st.sparse.billy.Matcher
import breeze.linalg._
import scala.reflect.ClassTag
import st.sparse.billy._

trait JsonProtocol extends DefaultJsonProtocol {
  implicit def middleburyFormat[D <% PairDetector: JsonFormat, E <% Extractor[F]: JsonFormat, M <% Matcher[F]: JsonFormat, F] =
    jsonFormat(
      Middlebury.apply[D, E, M, F],
      "databaseYear",
      "imageClass",
      "maxPairedDescriptors",
      "detector",
      "extractor",
      "matcher")

  implicit def blurredMiddleburyFormat[D <% PairDetector: JsonFormat, E <% Extractor[F]: JsonFormat, M <% Matcher[F]: JsonFormat, F] =
    jsonFormat(
      BlurredMiddlebury.apply[D, E, M, F],
      "similarityThreshold",
      "numSmoothingIterations",
      "scaleFactor",
      "middlebury")

  implicit def resultsFormat = jsonFormat1(Results.apply)
}
