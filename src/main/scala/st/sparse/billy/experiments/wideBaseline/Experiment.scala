package st.sparse.billy.experiments.wideBaseline

import st.sparse.billy._
import st.sparse.billy.internal._
import st.sparse.billy.experiments.RuntimeConfig
import scala.pickling._
import scala.pickling.binary._
import st.sparse.persistentmap._
import st.sparse.persistentmap.CustomPicklers._
import breeze.linalg.DenseMatrix
import org.joda.time.DateTime

trait Experiment {
  def run(implicit runtimeConfig: RuntimeConfig): Results
}

object Experiment extends Logging {
  /**
   * Adds a caching layer around an existing experiment, so that it need not
   * be recomputed if it has already been run.
   */
  def cached[E <% Experiment: SPickler: Unpickler: FastTypeTag](
    experiment: E)(
      implicit ftt2a: FastTypeTag[FastTypeTag[E]]): Experiment =
    new Experiment {
      override def run(implicit runtimeConfig: RuntimeConfig) = {
        val m = DenseMatrix.zeros[Double](3, 4)
        m.pickle.unpickle[DenseMatrix[Double]]

        // Each type is going to need its own table in the database.
        // This name should make very clear what type the table holds.
        val tableName = {
          val raw = 
            s"PilgrimExperiment_s${implicitly[FastTypeTag[E]].tpe.toString}"
          raw.replace(".", "_").replace(",", "_").replace("[", "_").replace("]", "_")        }
        logger.debug(s"Connecting to $tableName.")
        val cache = PersistentMap.connectElseCreate[E, Set[(DateTime, Results)]](
          "foo",
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
    }
}