package billy

import java.io.File

import org.apache.commons.io.FileUtils

import nebula._
import nebula.imageProcessing._
import nebula.util._

import billy._
import billy.brown._
import billy.mpie._
import billy.smallBaseline._
import billy.wideBaseline._
import billy.summary._

import nebula.util.JSONUtil
import spray.json.JsonFormat
import spray.json.pimpAny
import spray.json.pimpString

///////////////////////////////////////////////////////////

trait ExperimentRunner[R] {
  def run: R
}

trait StorageInfo[R] {
  def currentPath: File
  def mostRecentPath: Option[File]
  def save: R => Unit
  def load: Option[R]
  def name: String
}

object StorageInfo {
  // TODO: Refactor when Scala inference bug is fixed.
  class Experiment2StorageInfo[E <% RuntimeConfig => ExperimentRunner[R]: JsonFormat, R: JsonFormat](
    experiment: E)(runtimeConfig: RuntimeConfig) extends StorageInfo[R] {
    private implicit val iRC = runtimeConfig
    
    override def currentPath: File = new File(outDirectory, filename)

    override def mostRecentPath: Option[File] = existingResultsFiles.headOption

    override def save = results => {
      println(s"Saving to ${currentPath}")
      val json = results.toJson
      org.apache.commons.io.FileUtils.writeStringToFile(currentPath, json.prettyPrint)
    }

    override def load = mostRecentPath map { file =>
      println(s"Loading ${file}")
      val jsonString = FileUtils.readFileToString(file)
      jsonString.asJson.convertTo[R]
    }

    override def name = unixEpoch + "_" + nameNoTime

    ///////////////////////////////////////////////////////

    val unixEpoch = System.currentTimeMillis / 1000L

    def nameNoTime: String = {
      val fullString = JSONUtil.flattenJson(experiment.toJson)
      //      // Unfortunately, this string is too long to be part of a filename.
      //      fullString.take(100) + "_" + fullString.hashCode
      fullString
    }

    def filenameNoTime: String = nameNoTime + ".json"

    def filename: String = unixEpoch + "_" + filenameNoTime

    def outDirectory: File = new File(runtimeConfig.outputRoot, "results/experiment_data/").mustExist

    def existingResultsFiles: Seq[File] = {
      val allPaths = outDirectory.list.toList.map(path => outDirectory + "/" + path.toString)
      val matchingPaths = allPaths.filter(_.toString.contains(filenameNoTime))
      matchingPaths.sorted
    }
  }
  
    // TODO: Refactor when Scala inference bug is fixed.
  def WTFExperiment2StorageInfo[E <% RuntimeConfig => ExperimentRunner[R]: JsonFormat, R: JsonFormat](
    experiment: E)(implicit runtimeConfig: RuntimeConfig) = new Experiment2StorageInfo(experiment)(runtimeConfig)    
}





