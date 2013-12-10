package st.sparse.billy.experiments

import java.io.File
import java.nio.file.Files
import scala.slick.session.Database
import st.sparse.billy._

import st.sparse.sundry._

trait TestUtil extends st.sparse.billy.TestUtil {
  implicit val runtimeConfig = RuntimeConfig(
    ExistingDirectory(new File(resourceRoot, "/data")),
    database,
    outputRoot,
    None,
    None,
    false,
    true)
}