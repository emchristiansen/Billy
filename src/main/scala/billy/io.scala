package billy

import nebula._
import nebula.imageProcessing._
import nebula.util._

import billy._
import billy.brown._
import billy.mpie._
import billy.smallBaseline._
import billy.wideBaseline._
import billy.summary._

import java.io.File

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