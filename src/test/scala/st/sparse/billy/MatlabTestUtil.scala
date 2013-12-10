package st.sparse.billy

import java.io.File
import java.nio.file.Files
import scala.slick.session.Database
import st.sparse.billy._

import st.sparse.sundry._

trait MatlabTestUtil extends st.sparse.billy.TestUtil {
  implicit val matlabLibraryRoot = {
    val env = System.getenv("MATLAB_LIBRARY_ROOT")
    val root =
      if (env != null) env
      else "/home/eric/Dropbox/t/2013_q4/matlabLibraryRoot"
    MatlabLibraryRoot(ExistingDirectory(root))
  }
}