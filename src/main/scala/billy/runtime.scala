package billy

import java.io.File

import nebula._
import nebula.imageProcessing._
import nebula.util._

import billy._
import billy.brown._
import billy.mpie._
import billy.smallBaseline._
import billy.wideBaseline._
import billy.summary._

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



