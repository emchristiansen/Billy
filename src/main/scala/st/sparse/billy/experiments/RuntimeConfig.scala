package st.sparse.billy.experiments

import st.sparse.sundry._

import scala.slick.session.Database

///////////////////////////////////////////////////////////

/**
 * Contains information necessary for the runtime operation of Billy.
 */
case class RuntimeConfig(
  /**
   * The directory containing the datasets (the input to Billy).
   * 
   * This directory must be laid out in a particular manner; see the docs.
   */
  dataRoot: ExistingDirectory,
  /** 
   *  A `Database` which contains data used internally by Billy.
   */
  database: Database,
  /**
   * The directory to which Billy's outputs are written.
   * 
   * The internal folder structure is hard-coded in Billy.
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
   * It may be useful to set this to `false` for debugging purposes.
   * Otherwise, a sensible default is `true`. 
   */
  deleteTemporaryFiles: Boolean,
  /**
   * Whether to skip experiments that have already been run.
   * If `false`, experiments may be repeated.
   * Experiments are never overwritten; rather they are timestamped, and
   * when loading experiments Billy uses the most recent experiment.
   */
  skipCompletedExperiments: Boolean)



