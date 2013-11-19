package st.sparse.billy

import sys.process._
import st.sparse.sundry.ExistingDirectory
import st.sparse.sundry._
import java.io.File

case class MatlabLibraryRoot(
  data: ExistingDirectory) extends Box[ExistingDirectory]