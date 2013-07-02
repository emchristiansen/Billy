package billy.mpie

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

import scala.annotation.elidable
import scala.annotation.elidable.ASSERTION

import org.apache.commons.io.FilenameUtils

// TODO: Duplication. Grr.
case class MPIERuntimeConfig(
  projectRoot: File,
  dataRoot: File,
  nebulaRoot: File,
  tempDirectory: Option[File],
  deleteTemporaryFiles: Boolean,
  skipCompletedExperiments: Boolean,
  maxSimultaneousExperiments: Int,
  runtimeConfig: RuntimeConfig,
  piSliceRoot: String,
  lfwRoot: String,
  backgroundRoot: String,
  scaleFactor: Double,
  numFolds: Int,
  numIdentities: Int) {
  val random = new scala.util.Random(0)
}

case class MPIEExperimentConfig(
  roi: List[String],
  distance: List[String],
  pose: List[String],
  poseIsCrossCondition: Boolean,
  illumination: List[String],
  illuminationIsCrossCondition: Boolean,
  blur: List[String],
  blurIsCrossCondition: Boolean,
  noise: List[String],
  noiseIsCrossCondition: Boolean,
  jpeg: List[String],
  jpegIsCrossCondition: Boolean,
  misalignment: List[String],
  misalignmentIsCrossCondition: Boolean,
  background: List[String],
  backgroundIsCrossCondition: Boolean)

case class MPIECondition(
  pose: String,
  illumination: String,
  blur: String,
  noise: String,
  jpeg: String,
  misalignment: String,
  background: String)

object MPIECondition {
  def validPair(left: MPIECondition, right: MPIECondition): Boolean = {
    (left.pose <= right.pose) &&
      (left.illumination <= right.illumination) &&
      (left.blur <= right.blur) &&
      (left.noise <= right.noise) &&
      (left.jpeg <= right.jpeg) &&
      (left.misalignment <= right.misalignment) &&
      (left.background <= right.background)
  }

  def respectsCrossCondition(config: MPIEExperimentConfig,
                             left: MPIECondition,
                             right: MPIECondition): Boolean = {
    val pose = config.poseIsCrossCondition || left.pose == right.pose
    val illumination = config.illuminationIsCrossCondition ||
      left.illumination == right.illumination
    val blur = config.blurIsCrossCondition || left.blur == right.blur
    val noise = config.noiseIsCrossCondition || left.noise == right.noise
    val jpeg = config.jpegIsCrossCondition || left.jpeg == right.jpeg
    val misalignment = config.misalignmentIsCrossCondition ||
      left.misalignment == right.misalignment
    val background = config.backgroundIsCrossCondition ||
      left.background == right.background
    pose && illumination && blur && noise && jpeg && misalignment && background
  }
}

//case class MPIEExperiment(
//  val roi: String,
//  val distance: String,
//  val leftCondition: MPIECondition,
//  val rightCondition: MPIECondition) extends Experiment {
//  if (roi == "CFR") assert(leftCondition.pose == "051" && rightCondition.pose == "051")
//
//  val parameterAbbreviations: List[String] =
//    "R D P I L N J M B".split(" ").toList
//
//  val parameterValues = {
//    def xPattern(left: String, right: String): String = left + "x" + right
//
//    val pose = xPattern(leftCondition.pose, rightCondition.pose)
//    val illumination = xPattern(leftCondition.illumination,
//      rightCondition.illumination)
//    val blur = xPattern(leftCondition.blur, rightCondition.blur)
//    val noise = xPattern(leftCondition.noise, rightCondition.noise)
//    val jpeg = xPattern(leftCondition.jpeg, rightCondition.jpeg)
//    val misalignment = xPattern(leftCondition.misalignment,
//      rightCondition.misalignment)
//    val background = xPattern(leftCondition.background,
//      rightCondition.background)
//    val conditions =
//      List(pose, illumination, blur, noise, jpeg, misalignment, background)
//
//    List(roi, distance) ++ conditions
//  }
//}
//
//object MPIEExperiment {
//  val parameterNames: List[String] = {
//    "roi distance pose illumination blur noise jpeg misalignment background".split(" ").toList
//  }
//
//  def fromConfig(config: MPIEExperimentConfig): List[MPIEExperiment] = {
//    val conditions =
//      for (
//        p <- config.pose;
//        i <- config.illumination;
//        b <- config.blur;
//        n <- config.noise;
//        j <- config.jpeg;
//        m <- config.misalignment;
//        k <- config.background
//      ) yield {
//        MPIECondition(p, i, b, n, j, m, k)
//      }
//
//    val validPairs = for (
//      l <- conditions;
//      r <- conditions;
//      if MPIECondition.validPair(l, r)
//    ) yield (l, r)
//
//    val finalPairs = validPairs.filter({ case (l, r) => MPIECondition.respectsCrossCondition(config, l, r) })
//
//    for (
//      o <- config.roi;
//      d <- config.distance;
//      (l, r) <- finalPairs;
//      if ((o != "CFR" || (l.pose == "051" && r.pose == "051")) &&
//        (o != "SPB" || (l.pose == "240" && r.pose == "240")))
//    ) yield {
//      MPIEExperiment(o, d, l, r)
//    }
//  }
//}

case class MPIEProperties(id: String, session: String, expression: String, pose: String, illumination: String) {
  assert(id.size == 3)
  assert(List("01", "02", "03", "04").contains(session))
  assert(expression == "01")
  assert(List("240", "190", "051").contains(pose))
  assert(List("00", "04", "06").contains(illumination))

  def pathSegment = {
    val poseUnderscore = pose match {
      case "240" => "24_0"
      case "190" => "19_0"
      case "051" => "05_1"
    }
    "session%s/multiview/%s/%s/%s".format(session, id, expression, poseUnderscore)
  }
}

object MPIEProperties {
  def parseMPIEPath(path: String): MPIEProperties = {
    val filename = FilenameUtils.removeExtension((new File(path)).getName)
    val Parser = """(\S+)_(\S+)_(\S+)_(\S+)_(\S+)""".r
    val Parser(id, session, expression, pose, illumination) = filename
    MPIEProperties(id, session, expression, pose, illumination)
  }
}
