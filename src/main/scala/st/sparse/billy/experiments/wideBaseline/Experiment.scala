package st.sparse.billy.experiments.wideBaseline

import st.sparse.billy._
import st.sparse.billy.experiments.RuntimeConfig
import st.sparse.sundry._
import scala.pickling._
import scala.pickling.binary._
import st.sparse.persistentmap._
import st.sparse.persistentmap.CustomPicklers._
import breeze.linalg.DenseMatrix
import org.joda.time.DateTime
import com.sksamuel.scrimage.Image
import spray.json._
import spray.json.DefaultJsonProtocol._

trait Experiment {
  def run(implicit runtimeConfig: RuntimeConfig): Results

  /**
   * A string representation of the parameters of the model used in this
   * experiment.
   *
   * For example, this might include the detector, extractor, and matcher
   * types.
   * Used for reporting results.
   */
  def modelParametersString: String

  /**
   * A string representation of the experiment parameters.
   *
   * For example, this describe the actual task, such as the image used.
   * Used for reporting results.
   */
  def experimentParametersString: String
}

abstract class ExperimentImplementation[D <% PairDetector, E <% Extractor[F], M <% Matcher[F], F] extends Experiment with Logging {
  def leftImage(implicit runtimeConfig: RuntimeConfig): Image
  def rightImage(implicit runtimeConfig: RuntimeConfig): Image
  def correspondenceMap(implicit runtimeConfig: RuntimeConfig): CorrespondenceMap
  val maxPairedDescriptors: Int
  val detector: D
  val extractor: E
  val matcher: M

  def run(implicit runtimeConfig: RuntimeConfig): Results = {
    logger.info(s"Running ${this}")

    val (leftKeyPoints, rightKeyPoints) = detector.detectPair(
      correspondenceMap,
      leftImage,
      rightImage) unzip

    logger.info(s"Number of paired KeyPoints: ${leftKeyPoints.size}")

    val leftAndRightDescriptors = {
      val leftDescriptors = extractor.extract(leftImage, leftKeyPoints)
      val rightDescriptors = extractor.extract(rightImage, rightKeyPoints)

      for (
        (Some(left), Some(right)) <- leftDescriptors.zip(rightDescriptors)
      ) yield (left, right)
    }

    logger.info(s"Number of paired descriptors: ${leftAndRightDescriptors.size}")

    if (maxPairedDescriptors > leftAndRightDescriptors.size) {
      logger.warn(
        s"Extracted only ${leftAndRightDescriptors.size} paired descriptors, " +
          s"but maxPairedDescriptors was $maxPairedDescriptors.")
    }

    val (leftDescriptors, rightDescriptors) =
      leftAndRightDescriptors.take(maxPairedDescriptors).unzip

    val distances = matcher.matchAll(
      leftDescriptors,
      rightDescriptors)

    Results(distances)
  }

  override def modelParametersString =
    s"${detector.toString}_${extractor.toString}_${matcher.toString}"
}

object Experiment extends Logging {
  /**
   * Adds a caching layer around an existing experiment, so that it need not
   * be recomputed if it has already been run.
   */
  def cached[E <% Experiment: JsonFormat: FastTypeTag](
    experiment: E): Experiment = {
    checkJson(experiment)
    new Experiment {
      override def run(implicit runtimeConfig: RuntimeConfig) = {
        // Each type is going to need its own table in the database.
        // This name should make very clear what type the table holds.
        val tableName = {
          // Unfortunately, some DBs can't handle long table names :(.
          //          val raw =
          //            s"PilgrimExperiment_s${implicitly[FastTypeTag[E]].tpe.toString}"
          //          raw.replace(".", "_").replace(",", "_").replace("[", "_").replace("]", "_")

          s"Pilgrim${implicitly[FastTypeTag[E]].tpe.toString.hashCode.abs}"
        }
        logger.debug(s"Connecting to $tableName.")
        implicitly[JsonFormat[Results]]
        implicitly[JsonFormat[DateTime]]
        val cache = PersistentJsonMap.connectElseCreate[E, Set[(DateTime, Results)]](
          tableName,
          runtimeConfig.database)

        // Runs the experiment and stores the new results in the database.
        def run() {
          val existing = cache.getOrElse(experiment, Set[(DateTime, Results)]())
          val updated = existing ++ Set((new DateTime, experiment.run))
          cache += experiment -> updated
        }

        if (runtimeConfig.skipCompletedExperiments) {
          if (cache.contains(experiment))
            logger.info(s"$experiment cached; retrieving.")
          else {
            logger.info(s"$experiment not in cache; running.")
            run()
          }
        } else {
          run()
        }

        // We return the results of the most recent run.
        cache(experiment).toList.sortWith(_._1 isBefore _._1).head._2
      }

      override def modelParametersString = experiment.modelParametersString

      override def experimentParametersString =
        experiment.experimentParametersString

      override def toString = "Cached " + experiment
    }
  }

  //  // TODO: Remove duplication with above.
  //  def jsonCached[E <% Experiment: JsonFormat](
  //    experiment: E): Experiment =
  //    new Experiment {
  //      override def run(implicit runtimeConfig: RuntimeConfig) = {
  //        // Each type is going to need its own table in the database.
  //        // This name should make very clear what type the table holds.
  //        val tableName = {
  //          // Unfortunately, some DBs can't handle long table names :(.
  //          //          val raw =
  //          //            s"PilgrimExperiment_s${implicitly[FastTypeTag[E]].tpe.toString}"
  //          //          raw.replace(".", "_").replace(",", "_").replace("[", "_").replace("]", "_")
  //
  //          s"Pilgrim${implicitly[FastTypeTag[String]].tpe.toString.hashCode.abs}"
  //        }
  //        logger.debug(s"Connecting to $tableName.")
  //        val cache = PersistentMap.connectElseCreate[String, String](
  //          tableName,
  //          runtimeConfig.database)
  //
  ////        type Value = Set[(DateTime, Results)]
  //
  //        // Runs the experiment and stores the new results in the database.
  //        def run() {
  //          val existing = cache.get(experiment.toJson.compactPrint).map(
  //            _.asJson.convertTo[Results])
  //          val updated = experiment.run
  //          cache += experiment.toJson.compactPrint -> updated.toJson.compactPrint
  //        }
  //
  //        if (runtimeConfig.skipCompletedExperiments) {
  //          if (cache.contains(experiment.toJson.compactPrint))
  //            logger.info(s"$experiment cached; retrieving.")
  //          else {
  //            logger.info(s"$experiment not in cache; running.")
  //            run()
  //          }
  //        } else {
  //          run()
  //        }
  //
  //        // We return the results of the most recent run.
  //        cache(experiment.toJson.compactPrint).asJson.convertTo[Results]
  ////        cache(experiment.toJson.compactPrint).toList.map {
  ////          case (key, value) => (key.asJson.convertTo[E], value.asJson.convertTo[Value])
  ////        }.sortWith(_._1 isBefore _._1).head._2
  //      }
  //
  //      override def modelParametersString = experiment.modelParametersString
  //
  //      override def experimentParametersString =
  //        experiment.experimentParametersString
  //
  //      override def toString = "Cached " + experiment
  //    }

  //  def jsonCached[E <% Experiment: JsonFormat](
  //    experiment: E): Experiment = {
  //    implicit def toExperiment(string: String): Experiment = 
  //      string.asJson.convertTo[E]
  //    
  //    cached(experiment.toJson.compactPrint)
  //  }
}