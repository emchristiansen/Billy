package billy.testing

import nebula._
import java.io.File
import billy._

///////////////////////////////////////////////////////////

trait RuntimeConfigTest {
  implicit def runtimeConfig(
    implicit configMap: Map[String, Any]): RuntimeConfig = {
    val dataRoot = ExistingDirectory(configMap("dataRoot").toString)
    val outputRoot = ExistingDirectory(configMap("outputRoot").toString)
    val tempRoot = configMap.get("tempRoot") map { tr =>
      ExistingDirectory(tr.toString)
    }
    val deleteTemporaryFiles =
      configMap.get("deleteTemporaryFiles") == Some("true")
    val skipCompletedExperiments =
      configMap.get("skipCompletedExperiments") == Some("true")

    RuntimeConfig(
      dataRoot,
      outputRoot,
      tempRoot,
      deleteTemporaryFiles,
      skipCompletedExperiments)
  }
}
