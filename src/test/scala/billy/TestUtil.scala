package billy

import java.io.File

import st.sparse.sundry._

object TestUtil {
  val resourceRoot = ExistingDirectory(
    new File(getClass.getResource("/").getPath))
}