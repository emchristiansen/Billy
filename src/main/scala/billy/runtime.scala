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
import spray.json._

import JSONUtil._

///////////////////////////////////////////////////////////

case class RuntimeConfig(
  dataRoot: File,
  outputRoot: File,
  tempDirectory: Option[File],
  deleteTemporaryFiles: Boolean,
  skipCompletedExperiments: Boolean) {
  for (temp <- tempDirectory) {
    if (!temp.isDirectory) assert(temp.mkdir)
  }
}

trait RuntimeConfigJsonProtocol extends DefaultJsonProtocol {
  // TODO: Move the file wrapper stuff somewhere more appropriate.
  private case class FileWrapper(file: String)

  private implicit def jsonFileWrapper = jsonFormat1(FileWrapper.apply)

  implicit def jsonFile: RootJsonFormat[File] = {
    object NoScalaClass extends RootJsonFormat[File] {
      override def write(self: File) = FileWrapper(
        self.toString).toJson
      override def read(value: JsValue) = {
        val wrapper = value.convertTo[FileWrapper]
        new File(wrapper.file)
      }
    }

    NoScalaClass.addClassInfo("File")
  }

  implicit val runtimeConfigJsonProtocol =
    jsonFormat5(RuntimeConfig.apply).addClassInfo("RuntimeConfig")
}

object RuntimeConfig extends RuntimeConfigJsonProtocol



