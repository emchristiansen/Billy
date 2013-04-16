package billy.summary

import nebula._
import nebula.imageProcessing._
import nebula.util._

import billy._
import billy.brown._
import billy.mpie._
import billy.smallBaseline._
import billy.wideBaseline._
import billy.summary._

import java.awt.image.BufferedImage
import java.io.File

import org.opencv.features2d.DMatch

import nebula.PimpFile
import billy.RuntimeConfig
import billy.wideBaseline.WideBaselineExperiment
import spray.json._
import java.io.ByteArrayOutputStream
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO
import JSONUtil._

///////////////////////////////////////////////////////////////////////////////

// TODO: Better place for this
trait BufferedImageJsonProtocol extends DefaultJsonProtocol {
  implicit def jsonBufferedImage: RootJsonFormat[BufferedImage] = {
    object NoScalaClass extends RootJsonFormat[BufferedImage] {
      override def write(self: BufferedImage) = {
        val baos = new ByteArrayOutputStream
        ImageIO.write(self, "png", baos)
        val array: Array[Byte] = baos.toByteArray
        array.toJson
      }
      override def read(value: JsValue) = {
        val array = value.convertTo[Array[Byte]]
        val bois = new ByteArrayInputStream(array)
        ImageIO.read(bois)
      }
    }

    NoScalaClass.addClassInfo("BufferedImage")
  }
}

case class ExperimentSummary(
  summaryNumbers: Map[String, Double],
  summaryCurves: Map[String, Seq[(Double, Double)]],
  summaryImages: Map[String, BufferedImage])

trait ExperimentSummaryJsonProtocol extends DefaultJsonProtocol with BufferedImageJsonProtocol {
  implicit def jsonExperimentSummary = jsonFormat3(ExperimentSummary.apply)
}

object ExperimentSummary extends ExperimentSummaryJsonProtocol {
  implicit class ExperimentSummaryOps(self: ExperimentSummary) {
    def outDirectory(implicit runtime: RuntimeConfig) =
      new File(runtime.outputRoot, "summary").mustExist
  }
}

object SummaryUtil {
  /**
   * The error rate for a given recall.
   */
  def errorRateAtRecall(recall: Double, dmatches: Seq[DMatch]): Double = {
    requirey(recall >= 0)
    requirey(recall <= 1)

    val sorted = dmatches.sortBy(_.distance)

    def positive(dmatch: DMatch): Boolean = dmatch.queryIdx == dmatch.trainIdx

    def numPositive(dmatches: Seq[DMatch]): Int = dmatches.count(positive)

    val totalPositive = numPositive(sorted)
    if (totalPositive == 0) Double.NaN
    else {
      val allSplits = (0 to sorted.size).toStream map { i => sorted.splitAt(i) }

      val (left, right) = (allSplits find {
        case (left, right) => {
          numPositive(left).toDouble / totalPositive >= recall
        }
      }).get

      val errorLeft = left.size - numPositive(left)
      val errorRight = numPositive(right)

      (errorLeft + errorRight).toDouble / sorted.size
    }
  }

  /**
   * The precision-recall curve.
   */
  def precisionRecall(dmatches: Seq[DMatch]): Seq[(Double, Double)] = {
    val sorted = dmatches.sortBy(_.distance)

    def positive(dmatch: DMatch): Boolean = dmatch.queryIdx == dmatch.trainIdx

    def booleanToInt(boolean: Boolean) = boolean match {
      case false => 0
      case true => 1
    }

    val results = sorted map positive map booleanToInt

    val precisions = for (init <- results.inits) yield {
      if (init.size == 0) 1
      else MathUtil.mean(init)

    }

    val recalls = for (init <- results.inits) yield {
      init.sum.toDouble / results.sum
    }

    val allPoints = recalls.toIndexedSeq zip precisions.toIndexedSeq

    def mkFunction(points: Seq[(Double, Double)]): Seq[(Double, Double)] = {
      // The mapping from recall to precision should be a function.
      // Take the max precision for each recall.
      val groups: Seq[(Double, Seq[(Double, Double)])] =
        points.groupBy(_._1).toIndexedSeq.sortBy(_._1)

      groups.map(_._2).map {
        seq => seq.maxBy(_._2)
      }
    }
    
    val fcn = mkFunction(allPoints)    
    
    asserty(fcn.map(_._1).distinct.size == fcn.size)
    
    fcn
    
//    var smallest = 1.0
//    val monotonic = for ((recall, precision) <- fcn.sortBy(_._1)) yield {
//      smallest = Seq(precision, smallest).min
//      (recall, smallest)
//    }
//    monotonic
    
//    var lastRecall = -1
//    monotonic = for ((recall, precision) <- monotonic; if recall > lastRecall) yield {
//      lastRecall = recall
//      (recall, precision)
//    }
    
//    def mkFunction(points: Seq[(Double, Double)]): Seq[(Double, Double)] = {
//      // The mapping from recall to precision should be a function.
//      // Take the max precision for each recall.
//      val groups: Seq[(Double, Seq[(Double, Double)])] =
//        points.groupBy(_._1).toIndexedSeq.sortBy(_._1)
//
//      groups.map(_._2).map {
//        seq => seq.maxBy(_._2)
//      }
//    }
//    
//    val one = mkFunction(allPoints)
//    val two = mkFunction(one map (_.swap)) map (_.swap)
//    mkFunction(two)

    //    val recallGroups: Seq[(Double, Seq[((Double, Double), Int)])] = 
    //      allPoints.zipWithIndex.groupBy(_._1._1).toIndexedSeq.sortBy(_._1)
    //      
    //    recallGroups.map(_._2).map {
    //      seq => seq.minBy()
    //    }
  }

  def recognitionRate(dmatches: Seq[DMatch]): Double = {
    // The base image feature index is |queryIdx|, and the other 
    // image is |trainIdx|. This weirdness is caused by a convention
    // clash.
    val groupedByLeft = dmatches.groupBy(_.queryIdx)
    val groups = groupedByLeft.values.map(_.sortBy(_.distance))
    val numCorrect = groups.count(group => group.head.queryIdx == group.head.trainIdx)
    numCorrect.toDouble / groups.size
  }

  // TODO: More general than WideBaseline
  def experimentTable[D, E, M, F](
    baseExperiment: WideBaselineExperiment[D, E, M, F],
    rowMutations: Seq[WideBaselineExperiment[D, E, M, F] => WideBaselineExperiment[D, E, M, F]],
    columnMutations: Seq[WideBaselineExperiment[D, E, M, F] => WideBaselineExperiment[D, E, M, F]]): Seq[Seq[WideBaselineExperiment[D, E, M, F]]] = {
    for (row <- rowMutations) yield {
      for (column <- columnMutations) yield {
        column(row(baseExperiment))
      }
    }
  }

  // Turns Set(
  // Map(1 -> 12, 2 -> 13),
  // Map(1 -> 10, 3 -> 10, 2 -> 13))
  // into
  // Map(1 -> Set(12, 10), 2 -> Set(13), 3 -> Set(10))
  def mapUnion[A, B](maps: Set[Map[A, B]]): Map[A, Set[B]] = {
    val set = maps.map(_.toSeq).flatten.toSet
    val groups = set.groupBy(_._1)
    groups.mapValues(_.map(_._2))
  }

  // Turns Seq(
  // Map(1 -> 10, 2 -> 20),
  // Map(1 -> 10, 2 -> 30))
  // into
  // Seq(Map(2 -> 20), Map(2 -> 30)).
  def changingFields[A, B](maps: Seq[Map[A, B]]): Seq[Map[A, B]] = {
    val fields = mapUnion(maps.toSet).filter(_._2.size > 1).keys.toSet
    maps.map(_.filterKeys(fields))
  }

  def summarizeStructure(maps: Set[Map[String, String]]): String = {
    val union = mapUnion(maps)
    val constantPairs = union.filter(_._2.size == 1).mapValues(_.head).toSet
    val variablePairs = {
      val keys = union.filter(_._2.size > 1).keys.toSet
      keys.map(k => k -> "*").toMap
    }
    val summaryMap = constantPairs ++ variablePairs
    val components = summaryMap.toSeq.sortBy(_._1).map({ case (k, v) => "%s-%s".format(k, v) })
    components.mkString("_")
  }
}

