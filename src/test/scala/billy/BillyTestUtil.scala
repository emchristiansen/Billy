package billy

import nebula._
import java.io.File

///////////////////////////////////////////////////////////

object BillyTestUtil {
  val runtimeConfig = RuntimeConfig(
    homeDirectory + "Bitcasa/data",
    new File("/tmp"),
    None,
    false,
    false)
}