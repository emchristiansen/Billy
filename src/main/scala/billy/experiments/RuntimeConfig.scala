package billy

import nebula._
import nebula.util._

///////////////////////////////////////////////////////////

/**
 * Contains information necessary for the runtime operation of Billy.
 */
case class RuntimeConfig(
  /**
   * The directory containing the datasets.
   * This directory must be laid out in a particular manner; see the docs.
   * TODO
   */
  dataRoot: ExistingDirectory,
  /**
   * Raw experiment results, summaries, and the like will be written here
   * according to a hard-coded format.
   */
  outputRoot: ExistingDirectory,
  /**
   * Billy sometimes needs to create temporary files.
   * By default, the system-wide temp directory is used (/tmp in Linux).
   * You can optionally set a different temp directory.
   */
  tempRoot: Option[ExistingDirectory],
  /**
   * Whether Billy should delete the temporary files it creates.
   * If so, the deletion will happen at program termination.
   * It may be useful to set this to |false| for debugging purposes.
   * Otherwise, a sensible default is |true|. 
   */
  deleteTemporaryFiles: Boolean,
  /**
   * Whether to skip experiments that have already been run.
   * If |false|, experiments may be repeated.
   * Experiments are never overwritten; rather they are timestamped, and
   * when loading experiments Billy uses the most recent experiment.
   */
  skipCompletedExperiments: Boolean)



