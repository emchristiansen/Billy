package st.sparse.billy

import sys.process._
import st.sparse.sundry.ExistingDirectory
import st.sparse.sundry._
import java.io.File

case class MatlabLibraryRoot(
  data: ExistingDirectory) extends Box[ExistingDirectory]

object MatlabUtil extends Logging {
  def runInDirectory(directory: ExistingDirectory, command: String) {
    val process = Process(
      Seq("matlab", "-nodisplay", "-nojvm", "-r", s"$command; exit;"),
      Some(directory.data))

    logger.info(s"Executing process:\n${process.toString}\nin directory\n${directory.getPath}")

    val returnCode = process !

    assert(returnCode == 0)
  }

  def runInDirectory(
    relativePath: String,
    command: String)(
      implicit matlabLibraryRoot: MatlabLibraryRoot): Unit = runInDirectory(
    ExistingDirectory(new File(matlabLibraryRoot.data, relativePath)),
    command)
}