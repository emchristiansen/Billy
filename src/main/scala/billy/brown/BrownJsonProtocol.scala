package billy.brown

import nebula._
import nebula.imageProcessing._
import nebula.util._

import billy._
import billy.brown._
import billy.mpie._
import billy.smallBaseline._
import billy.wideBaseline._
import billy.summary._

import nebula.util.DMatchJsonProtocol.dmatchJsonProtocol
import nebula.util.JSONUtil.AddClassName
import spray.json.DefaultJsonProtocol
import spray.json.JsonFormat

///////////////////////////////////////////////////////////

trait BrownJsonProtocol extends DefaultJsonProtocol {
  implicit def brownExperimentJsonProtocol[E <% Extractor[F]: JsonFormat, M <% Matcher[F] : JsonFormat, F] =
    jsonFormat4(BrownExperiment.apply[E, M, F]).addClassInfo(
      "BrownExperiment")
    
  implicit def brownExperimentResultsJsonProtocol[E <% Extractor[F]: JsonFormat, M <% Matcher[F] : JsonFormat, F] =
    jsonFormat2(BrownExperimentResults.apply[E, M, F]).addClassInfo(
      "BrownExperimentResults")    
}

object BrownJsonProtocol extends BrownJsonProtocol