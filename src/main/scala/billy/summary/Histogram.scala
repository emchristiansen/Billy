package billy.summary

import nebula._
import nebula.imageProcessing._

import billy._
import billy.brown._
import billy.mpie._
import billy.smallBaseline._
import billy.wideBaseline._
import billy.summary._

import java.awt.image.BufferedImage
import java.io.File

import javax.imageio.ImageIO
import nebula.PimpFile
import billy.RuntimeConfig
import nebula.getResource


///////////////////////////////////////////////////////////////////////////////

case class Histogram(
  title: String,
  sameDistances: Seq[Double],
  differentDistances: Seq[Double])(
      implicit runtime: RuntimeConfig) {
  def draw {
    val image = render
    ImageIO.write(image, "png", path)

    println("wrote %s".format(path))
  }
  
  // TODO: Improve this and the above name.
  def render: BufferedImage = {
    val tempContents = "%s\n%s\n%s".format(
      title,
      sameDistances.sorted.mkString(" "),
      differentDistances.sorted.mkString(" "))
    val tempFile = IO.createTempFile("histogramData", ".txt")
    org.apache.commons.io.FileUtils.writeStringToFile(tempFile, tempContents)

    // TODO: Fix path
    val pythonScript = getResource("python/distance_histogram.py")
    val outputFile = IO.createTempFile("histogram", ".png")
    val command = "python %s %s %s".format(pythonScript, tempFile, outputFile)
    nebula.util.IO.runSystemCommand(command)

    ImageIO.read(outputFile)
  }

  def path: File = {
    val filename = title.replace(" ", "_") + ".png"
    new File(runtime.outputRoot, s"summary/histograms/${filename}").parentMustExist
  }
}

object Histogram {
  def apply(
      results: WideBaselineExperimentResults[_, _, _, _], 
      title: String)(
      implicit runtime: RuntimeConfig): Histogram = {
    val (same, different) = results.dmatches.partition(dmatch => dmatch.queryIdx == dmatch.trainIdx)
    Histogram(title, same.map(_.distance.toDouble), different.map(_.distance.toDouble))
  }
}