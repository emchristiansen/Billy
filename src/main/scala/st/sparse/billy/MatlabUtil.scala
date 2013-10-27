package st.sparse.billy

import sys.process._
import st.sparse.sundry.ExistingDirectory

object MatlabUtil {
  def runInDirectory(directory: ExistingDirectory, command: String) {
    val fullCommand =
      s"""matlab -nodisplay -nojvm -r "cd ${directory.getPath}; $command; exit" """
    val returnCode = fullCommand !
    
    assert(returnCode == 0)
  }
}