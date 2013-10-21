package st.sparse.billy

import org.expecty.Expecty

package object internal {
  type Logging = com.typesafe.scalalogging.slf4j.Logging
  
  val require = new Expecty()
  val assert = new Expecty()
} 