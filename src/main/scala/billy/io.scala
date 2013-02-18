package billy

///////////////////////////////////////////////////////////

object IO {
  def createTempFile(
    prefix: String,
    suffix: String)(
      implicit runtime: RuntimeConfig) = {
    val file = runtime.tempDirectory match {
      case Some(file) => File.createTempFile(prefix, suffix, file)
      case None => File.createTempFile(prefix, suffix)
    }

    if (runtime.deleteTemporaryFiles) file.deleteOnExit
    file
  }
}