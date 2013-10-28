package st.sparse.billy

import com.typesafe.scalalogging.slf4j.Logging
import com.sksamuel.scrimage.Image
import st.sparse.billy.experiments.RuntimeConfig
import java.io.File

/**
 * Adds some useful features to `Logging`.
 */
// TODO: Move to Sundry.
trait RichLogging extends Logging {
  def className = this.getClass.getName

  /**
   * Logs an image as a PNG.
   * 
   * `name` should not include the file extension, and $name.png must be
   * a legal filename.
   */
  def logImage(
    image: Image,
    name: String)(implicit runtimeConfig: RuntimeConfig) {
    val loggingRoot = new File(runtimeConfig.outputRoot, s"logging/$className")
    if (!loggingRoot.isDirectory) assert(loggingRoot.mkdirs())
    
    val outputFile = new File(loggingRoot, name + ".png")
    logger.info(s"Logging image to ${outputFile.getPath}")
    image.write(outputFile)
  }
}