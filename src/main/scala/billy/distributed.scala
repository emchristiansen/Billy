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
import org.apache.commons.io.FileUtils.readFileToString
import javax.imageio.ImageIO
import billy.summary.ExperimentSummary
import spray.json.JsonFormat
import spray.json.pimpAny
import spray.json._
import billy.DetectorJsonProtocol._
import billy.ExtractorJsonProtocol._
import billy.MatcherJsonProtocol._
import scala.reflect.runtime.universe._
import org.apache.commons.io.FileUtils
import shapeless._
import billy.wideBaseline._
import spray.json.DefaultJsonProtocol

///////////////////////////////////////////////////////////

// TODO: Rename JsonAndTypeName
case class JSONAndTypeName(val json: JsValue, val typeName: String)

trait JSONAndTypeNameJsonProtocol extends DefaultJsonProtocol {
  implicit def jsonJSONAndTypeName =
    jsonFormat2(JSONAndTypeName.apply)
}

object JSONAndTypeName extends JSONAndTypeNameJsonProtocol {
  def apply[A: JsonFormat: TypeTag](
    experiment: A): JSONAndTypeName = JSONAndTypeName(
    experiment.toJson,
    instanceToTypeName(experiment))

  implicit class ToSource(self: JSONAndTypeName) {
    def toSource: String = {
      val file = File.createTempFile("JSONAndTypeName_toSource", ".json")
      FileUtils.writeStringToFile(file, self.json.compactPrint)
      s"""
{
  val jsonFile = new java.io.File("${file.toString}")
  val json = org.apache.commons.io.FileUtils.readFileToString(jsonFile).asJson 
  json.convertTo[${self.typeName}]
}
"""
    }
  }
}

trait Distributed {
  /**
   * Verifies that an experiment of the given type can be run.
   * It is a good idea to run this on experiments before they are sent
   * across the wire.
   */
  // TODO: Should I be sending just pure source code across the wire?
  def typecheckExperiment(
    experiment: JSONAndTypeName)(
      implicit imports: Imports): () => RuntimeConfig => JSONAndTypeName = {
    val source = s"""
    loadOpenCV  
     
    val experiment = ${experiment.toSource}    
    
    def addRuntime(runtimeConfig: RuntimeConfig): JSONAndTypeName = {
      implicit val rC = runtimeConfig
      val results = experiment.run
      JSONAndTypeName(results.toJson, instanceToTypeName(results))
    }
    
    addRuntime _
    """

    // TODO: Move elsewhere
    GlobalLock.synchronized {
      typeCheck[RuntimeConfig => JSONAndTypeName](source.addImports)
    }
  }

  /**
   * Actually runs an experiment.
   * The experiment and type are provided as Json and a String, respectively.
   * This method builds the source needed for the experiment, then invokes
   * the compiler to turn it into something that can be run.
   */
  def runExperiment(
    experiment: JSONAndTypeName)(
      implicit imports: Imports,
      runtimeConfig: RuntimeConfig): JSONAndTypeName = {
    println("Running experiment.")
    val needsRuntime = typecheckExperiment(experiment).apply()

    val results = needsRuntime(runtimeConfig)
    println("Done running experiment.")
    results
  }

  def typecheckExperimentResults(
    results: JSONAndTypeName)(
      implicit imports: Imports): () => RuntimeConfig => JSONAndTypeName = {
    val source = s"""
    loadOpenCV  
     
    val results = ${results.toSource}    
    
    def addRuntime(runtimeConfig: RuntimeConfig): JSONAndTypeName = {
      implicit val rC = runtimeConfig
      val summary: ExperimentSummary = results
      JSONAndTypeName(summary.toJson, instanceToTypeName(summary))
    }
    
    addRuntime _
    """

    // TODO: Move elsewhere    
    GlobalLock.synchronized {
      typeCheck[RuntimeConfig => JSONAndTypeName](source.addImports)
    }
  }

  def getSummary(
    results: JSONAndTypeName)(
      implicit imports: Imports,
      runtimeConfig: RuntimeConfig): JSONAndTypeName = {
    println("Getting summary from results.")
    val needsRuntime = typecheckExperimentResults(results).apply()
    val summary = needsRuntime(runtimeConfig)
    println("Done getting summary from results.")
    summary
  }

  //  def runExperiment[E <% ExperimentRunner[R] <% StorageInfo[R], R <% ExperimentSummary](
  //    writeImages: Boolean, experiment: E)(
  //      implicit runtime: RuntimeConfig): ExperimentSummary = {
  //    logError(s"Running experiment: ${experiment}")
  //    val results =
  //      if (runtime.skipCompletedExperiments && experiment.load.isDefined)
  //        experiment.load.get
  //      else {
  //        val results = experiment.run
  //        experiment.save(results)
  //        results
  //      }
  //
  //    val summary = results.to[ExperimentSummary]
  //    println(summary.summaryNumbers.mapValues(_.apply))
  //    if (writeImages) {
  //      val histogram = summary.summaryImages("histogram")()
  //      val outDirectory = new File(summary.outDirectory, "histograms")
  //      val outPath = new File(
  //        outDirectory,
  //        experiment.name + "_histogram.png")
  //      ImageIO.write(histogram, "png", outPath)
  //    }
  //    summary
  //  }
  //
  //  type Capstone = (Boolean, RuntimeConfig) => ExperimentSummary
  //
  //  /**
  //   * Returns a closure of type Capstone.
  //   * This method is unsafe, in that the needed implicits are not found at
  //   * compile time. Rather, they are found by invoking the compiler at
  //   * runtime, possibly resulting in a runtime compilation error.
  //   * This appears to be necessary to overcome serialization limitations with
  //   * Spark, namely Spark won't (automatically) serialize the implicit
  //   * typeclass instances, probably because it can't serialize arbitrary
  //   * functions. So instead of sending the instances across the wire, we just
  //   * find them again on the other side.
  //   */
  //  def unsafeCapstone[E <% RuntimeConfig => ExperimentRunner[R] <% RuntimeConfig => StorageInfo[R]: JsonFormat: TypeTag, R <% RuntimeConfig => ExperimentSummary: TypeTag](experiment: E): Capstone = {
  //    val experimentExpression = generateExpression(experiment).addImports
  //
  //    // We need to resolve these implicits outside the closure, otherwise
  //    // the closure will secretly depend on TypeTag instances, which
  //    // aren't serializable.
  //    implicit val eTypeName = typeName[E]
  //    implicit val errTypeName = typeName[E => RuntimeConfig => ExperimentRunner[R]]
  //    implicit val ersTypeName = typeName[E => RuntimeConfig => StorageInfo[R]]
  //    implicit val rrsTypeName = typeName[R => RuntimeConfig => ExperimentSummary]
  //
  //    val capstone: Capstone = (writeImages, runtimeConfig) => {
  //      loadOpenCV
  //
  //      def dropMiddle[A, B, C](f: A => B => C, b: B): A => C = a => f(a)(b)
  //
  //      def resurrectedExperiment = eval[E](experimentExpression)
  //      implicit def evRunner = dropMiddle(
  //        runtimeImplicitly[E => RuntimeConfig => ExperimentRunner[R]],
  //        runtimeConfig)
  //      implicit def evStorageInfo = dropMiddle(
  //        runtimeImplicitly[E => RuntimeConfig => StorageInfo[R]],
  //        runtimeConfig)
  //      implicit def evSummary = dropMiddle(
  //        runtimeImplicitly[R => RuntimeConfig => ExperimentSummary],
  //        runtimeConfig)
  //      implicit def r = runtimeConfig
  //
  //      runExperiment(writeImages, resurrectedExperiment)
  //    }
  //
  //    capstone
  //  }
  //  
  //  /**
  //   * Work in progress.
  //   */
  //  def capstonesFromTuples[T <: HList](tuples: T): Seq[(Capstone, JsValue)] = {
  //    val source = s"""
  //      val tuples = ${tuples} 
  //
  //      object constructExperiment extends Poly1 {
  //        implicit def default[D <% PairDetector, E <% Extractor[F], M <% Matcher[F], F] = at[(D, E, M)] {
  //          case (detector, extractor, matcher) => {
  //            WideBaselineExperiment(imageClass, otherImage, detector, extractor, matcher)
  //          }
  //        }
  //      }
  //
  //      // This lifting, combined with flatMap, filters out types that can't be used                                                                                                                                                                                                              
  //      // to construct experiments.                                                                                                                                                                                                                                                              
  //      object constructExperimentLifted extends Lift1(constructExperiment)
  //
  //      val experiments = tuples flatMap constructExperimentLifted
  //
  //      println(experiments)
  //
  //      object constructCapstone extends Poly1 {
  //        implicit def default[E <% RuntimeConfig => ExperimentRunner[R] <% RuntimeConfig => StorageInfo[R]: JsonFormat: TypeTag, R <% RuntimeConfig => ExperimentSummary: TypeTag] = at[E] {
  //          experiment => Distributed.unsafeCapstone(experiment)
  //        }
  //      }
  //
  //      object getJson extends Poly1 {
  //        implicit def default[E: JsonFormat] = at[E] { experiment =>
  //          {
  //            experiment.toJson
  //          }
  //        }
  //      }
  //
  //      val capstones = experiments map constructCapstone
  //      val jsons = experiments map getJson
  //      capstones.toList zip jsons.toList
  //    """
  //    eval[Seq[(Capstone, JsValue)]](source.addImports)
  //  }
}

object Distributed extends Distributed