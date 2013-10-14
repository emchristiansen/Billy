package st.sparse.billy.experiments

import java.io.File

///////////////////////////////////////////////////////////

object IO {
  // TODO: Remove if this isn't used.
  def createTempFile(
    prefix: String,
    suffix: String)(
      implicit runtime: RuntimeConfig) = {
    val file = runtime.tempRoot match {
      case Some(file) => File.createTempFile(prefix, suffix, file)
      case None => File.createTempFile(prefix, suffix)
    }

    if (runtime.deleteTemporaryFiles) file.deleteOnExit
    file
  }
}
