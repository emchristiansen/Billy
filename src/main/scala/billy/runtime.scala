package nebula

import java.io.File

///////////////////////////////////////////////////////////

case class RuntimeConfig(
  dataRoot: File,
  outputRoot: File,
  tempDirectory: Option[File],
  deleteTemporaryFiles: Boolean,
  skipCompletedExperiments: Boolean) {
  for (temp <- tempDirectory) {
    if (!temp.isDirectory) asserty(temp.mkdir)
  }
}



