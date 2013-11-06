package st.sparse.billy.experiments

import java.io.File
import java.nio.file.Files
import scala.slick.session.Database
import st.sparse.billy._

import st.sparse.sundry._

trait MatlabTestUtil extends st.sparse.billy.experiments.TestUtil with st.sparse.billy.MatlabTestUtil {
  override val runtimeConfig = RuntimeConfig(
    ExistingDirectory(new File(resourceRoot, "/data")),
    database,
    outputRoot,
    None,
    Some(matlabLibraryRoot),
    false,
    true)
}