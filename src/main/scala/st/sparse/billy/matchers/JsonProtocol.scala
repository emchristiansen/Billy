package st.sparse.billy.matchers

import spray.json._
import st.sparse.billy._
import st.sparse.billy.PairDetector
import st.sparse.billy.Matcher
import st.sparse.sundry._
import scala.pickling.FastTypeTag

trait JsonProtocol extends DefaultJsonProtocol {  
  implicit def vectorMatcherL0Format = jsonFormat0(VectorMatcher.L0)
  implicit def vectorMatcherL1Format = jsonFormat0(VectorMatcher.L1)
  implicit def vectorMatcherL2Format = jsonFormat0(VectorMatcher.L2)
  
  // TODO: Add other matchers.
}
