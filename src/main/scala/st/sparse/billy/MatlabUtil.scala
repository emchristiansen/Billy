package st.sparse.billy

import sys.process._
import st.sparse.sundry.ExistingDirectory
import st.sparse.sundry._

object MatlabUtil extends Logging {
  def runInDirectory(directory: ExistingDirectory, command: String) {
    val process = Process(
      Seq("matlab", "-nodisplay", "-nojvm", "-r", s"$command; exit;"),
      Some(directory.data))
      
    logger.info(s"Executing process:\n${process.toString}\nin directory\n${directory.getPath}")
      
    val returnCode = process !

    assert(returnCode == 0)
  }
}