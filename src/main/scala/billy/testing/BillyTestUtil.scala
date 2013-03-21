package billy.testing

import nebula._
import java.io.File
import billy._

///////////////////////////////////////////////////////////

object BillyTestUtil {
  val runtimeConfig = RuntimeConfig(
    homeDirectory + "Bitcasa/data",
    new File("/tmp"),
    None,
    false,
    false)
}