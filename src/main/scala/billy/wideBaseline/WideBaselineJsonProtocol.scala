package nebula.wideBaseline

import spray.json.DefaultJsonProtocol
import spray.json.RootJsonFormat
import spray.json.JsonFormat
import nebula.PairDetector
import nebula.Extractor
import nebula.Matcher
import nebula.util.JSONUtil._
import nebula.util.DMatchJsonProtocol._

///////////////////////////////////////////////////////////

trait WideBaselineJsonProtocol extends DefaultJsonProtocol {
  implicit def wideBaselineExperiment[D, E, M, F](
    implicit evPairDetector: D => PairDetector,
    evExtractor: E => Extractor[F],
    evMatcher: M => Matcher[F],
    evDJson: JsonFormat[D],
    evEJson: JsonFormat[E],
    evMJson: JsonFormat[M]): RootJsonFormat[WideBaselineExperiment[D, E, M, F]] =
    jsonFormat5(WideBaselineExperiment.apply[D, E, M, F]).addClassInfo("WideBaselineExperiment")

  implicit def wideBaselineExperimentResults[D, E, M, F](
    implicit evPairDetector: D => PairDetector,
    evExtractor: E => Extractor[F],
    evMatcher: M => Matcher[F],
    evDJson: JsonFormat[D],
    evEJson: JsonFormat[E],
    evMJson: JsonFormat[M]): RootJsonFormat[WideBaselineExperimentResults[D, E, M, F]] =
    jsonFormat2(WideBaselineExperimentResults.apply[D, E, M, F]).addClassInfo(
      "WideBaselineExperimentResults")
}

object WideBaselineJsonProtocol extends WideBaselineJsonProtocol