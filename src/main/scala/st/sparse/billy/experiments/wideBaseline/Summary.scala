package st.sparse.billy.experiments.wideBaseline

import st.sparse.billy._

import breeze.linalg._

import java.io.File

import st.sparse.sundry._
import grizzled.math.stats

/////////////////////////////////////////////////////////////

object SummaryUtil {
  def recognitionRate(results: Results): Double = {
    val distances = results.distances
    val numCorrect = (0 until distances.rows) map { rowIndex =>
      val row = distances(rowIndex, ::)
      row.argmin._2 == rowIndex
    } count (identity)

    numCorrect.toDouble / distances.rows.toDouble
  }

  def precisionRecall(results: Results): IndexedSeq[(Double, Double)] = {
    val distancesAndMatches = results.distances.mapPairs {
      case ((rowIndex, columnIndex), distance) =>
        (distance, rowIndex == columnIndex)
    }

    val indicators = distancesAndMatches.data.sortBy(_._1) map (_._2) map {
      case false => 0
      case true => 1
    }

    val precisions = for (init <- indicators.inits) yield {
      if (init.size == 0) 1
      else stats.mean(init: _*)
    }

    val recalls = for (init <- indicators.inits) yield {
      init.sum.toDouble / indicators.sum
    }

    val allPoints = recalls.toIndexedSeq zip precisions.toIndexedSeq

    def mkFunction(points: Seq[(Double, Double)]): IndexedSeq[(Double, Double)] = {
      // The mapping from recall to precision should be a function.
      // Take the max precision for each recall.
      val groups: IndexedSeq[(Double, Seq[(Double, Double)])] =
        points.groupBy(_._1).toIndexedSeq.sortBy(_._1)

      groups.map(_._2).map {
        seq => seq.maxBy(_._2)
      }
    }

    val fcn = mkFunction(allPoints)

    assert(fcn.map(_._1).distinct.size == fcn.size)

    fcn
  }
  //  /**
  //   * The error rate for a given recall.
  //   */
  //  def errorRateAtRecall(recall: Double, dmatches: Seq[DMatch]): Double = {
  //    require(recall >= 0)
  //    require(recall <= 1)
  //
  //    val sorted = dmatches.sortBy(_.distance)
  //
  //    def positive(dmatch: DMatch): Boolean = dmatch.queryIdx == dmatch.trainIdx
  //
  //    def numPositive(dmatches: Seq[DMatch]): Int = dmatches.count(positive)
  //
  //    val totalPositive = numPositive(sorted)
  //    if (totalPositive == 0) Double.NaN
  //    else {
  //      val allSplits = (0 to sorted.size).toStream map { i => sorted.splitAt(i) }
  //
  //      val (left, right) = (allSplits find {
  //        case (left, right) => {
  //          numPositive(left).toDouble / totalPositive >= recall
  //        }
  //      }).get
  //
  //      val errorLeft = left.size - numPositive(left)
  //      val errorRight = numPositive(right)
  //
  //      (errorLeft + errorRight).toDouble / sorted.size
  //    }
  //  }
}