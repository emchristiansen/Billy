package billy

import java.io.File

import org.apache.commons.io.FileUtils

import nebula._
import nebula.imageProcessing._
import nebula.util._

import billy._
import billy.brown._

import billy.smallBaseline._
import billy.wideBaseline._
import billy.summary._

///////////////////////////////////////////////////////////

trait StorageInfo[R] {
  def currentPath: File
  def mostRecentPath: Option[File]
  def save: R => Unit
  def load: Option[R]
  def name: String
}

object StorageInfo {
  // TODO: Refactor when Scala inference bug is fixed.
  class Experiment2StorageInfo[E <% RuntimeConfig => ExperimentRunner[R], R](
    experiment: E)(runtimeConfig: RuntimeConfig) extends StorageInfo[R] {
    private implicit val iRC = runtimeConfig
    
    override def currentPath: File = new File(outDirectory, filename)

    override def mostRecentPath: Option[File] = existingResultsFiles.headOption

    override def save = results => {
      println(s"Saving to ${currentPath}")
      ???
//      val json = results.toJson
//      currentPath.compressedWriteString(json.prettyPrint)
    }

    override def load = mostRecentPath map { file =>
      println(s"Loading ${file}")
      val jsonString = file.compressedReadString
//      val jsonString = file.readString
      ???
//      jsonString.asJson.convertTo[R]
    }

    override def name = unixEpoch + "_" + nameNoTime

    ///////////////////////////////////////////////////////

    val unixEpoch = System.currentTimeMillis / 1000L

    def nameNoTime: String = {
      ???
      //      // Unfortunately, this string is too long to be part of a filename.
      //      fullString.take(100) + "_" + fullString.hashCode
//      fullString
    }

    def filenameNoTime: String = nameNoTime + ".json.gz"
//    def filenameNoTime: String = nameNoTime + ".json"

    def filename: String = unixEpoch + "_" + filenameNoTime

    def outDirectory: File = {
      val experimentDataRoot = 
        ExistingDirectory(runtimeConfig.outputRoot + "results/experiment_data/")
      val outDirectory = experimentDataRoot + nameNoTime
      ExistingFile.mkIfNeeded(outDirectory)
    }
      
      new File(
        runtimeConfig.outputRoot, 
        "results/experiment_data/").mustExist

    def existingResultsFiles: Seq[File] = {
      val allPaths = outDirectory.list.toList.map(path => outDirectory + "/" + path.toString)
      val matchingPaths = allPaths.filter(_.toString.contains(filenameNoTime))
      matchingPaths.sorted
    }
  }
  
    // TODO: Refactor when Scala inference bug is fixed.
  def WTFExperiment2StorageInfo[E <% RuntimeConfig => ExperimentRunner[R], R](
    experiment: E)(implicit runtimeConfig: RuntimeConfig) = new Experiment2StorageInfo(experiment)(runtimeConfig) 
}





