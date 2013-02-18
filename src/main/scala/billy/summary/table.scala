package nebula.summary

import java.io.File

import scala.reflect.ClassTag

import SummaryUtil.mapUnion
import SummaryUtil.summarizeStructure
import breeze.linalg.DenseMatrix
import grizzled.math.stats
import nebula.PimpFile
import nebula.RuntimeConfig
import nebula.util.DenseMatrixUtil.DenseMatrixToSeqSeq
import nebula.util.DenseMatrixUtil.SeqSeqToDenseMatrix
import nebula.util.JSONUtil
import spray.json.JsValue
import spray.json.JsonFormat
import spray.json.pimpAny

///////////////////////////////////////////////////////////////////////////////

// TODO: This class is a stop-gap. Lots of code duplication. Will be deleted
// when I HList everything and compile the main function at runtime.
case class TableTitles(
  title: String,
  rowLabels: IndexedSeq[String],
  columnLabels: IndexedSeq[String])

object TableTitles {
  def title(experiments: Seq[JsValue]): String = {
    val maps = experiments.map(JSONUtil.getParametersFromJson).toSet
    summarizeStructure(maps)
  }

  def entryTitles(experiments: Seq[JsValue]): IndexedSeq[String] = {
    val experimentMaps = experiments.map(JSONUtil.getParametersFromJson)
    val union = mapUnion(experimentMaps.toSet)
    val variableKeys = union.filter(_._2.size > 1).keys.toSet

    def entryTitle(experimentMap: Map[String, String]): String = {
      experimentMap.filterKeys(variableKeys).toSeq.map({ 
        case (k, v) => "%s-%s".format(k, v) }).mkString("_")
    }

    experimentMaps.map(entryTitle).toIndexedSeq
  }  
  
  def apply(experiments: Seq[Seq[JsValue]]): TableTitles = {
    // TODO: Replace with |everywhere| from shapeless when Scala 2.10 comes out.
    val experimentsFirstRow = experiments.head
    val experimentsFirstColumn = experiments.map(_.head)

    val tableTitle = title(experiments.flatten)
    val rowLabels = entryTitles(experimentsFirstColumn)
    val columnLabels = entryTitles(experimentsFirstRow)

    TableTitles(tableTitle, rowLabels, columnLabels)
  }
}
  
case class Table[A](
  title: String,
  rowLabels: IndexedSeq[String],
  columnLabels: IndexedSeq[String],
  entries: DenseMatrix[A]) {
  lazy val unixEpoch = System.currentTimeMillis / 1000L
}

object Table {
  implicit class AddNormalize(self: Table[Double]) {
    def normalizeColumns: Table[Double] = {
      val transpose = self.entries.toSeqSeq.transpose
      val transposeNormalized = transpose.map(
          row => row.map(_ / stats.mean(row: _*)))
      self.copy(entries = transposeNormalized.transpose.toMatrix)
    }
  }
  
  implicit class MoreFunctions[A](self: Table[A]) {
    def toTSV(toString: A => String): String = {
      val topRow = self.title +: self.columnLabels
      val stringEntries: Seq[Seq[String]] = self.entries.map(toString).toSeqSeq
      val otherRows: Seq[Seq[String]] = self.rowLabels.zip(stringEntries).map({
        case (title, entries) => Seq(title) ++ entries
      })
      val stringsTable = Seq(topRow) ++ otherRows

      stringsTable.map(_.mkString("\t")).mkString("\n")
    }

    def path(implicit runtime: RuntimeConfig): File = 
      new File(runtime.outputRoot, s"summary/${self.unixEpoch}_${self.title}.csv").parentMustExist 
  }

  def title[E: JsonFormat](experiments: Seq[E]): String = {
    val maps = experiments.map(_.toJson).map(JSONUtil.getParametersFromJson).toSet
    summarizeStructure(maps)
  }

  def entryTitles[E: JsonFormat](experiments: Seq[E]): IndexedSeq[String] = {
    val experimentMaps = experiments.map(_.toJson).map(JSONUtil.getParametersFromJson)
    val union = mapUnion(experimentMaps.toSet)
    val variableKeys = union.filter(_._2.size > 1).keys.toSet

    def entryTitle(experimentMap: Map[String, String]): String = {
      experimentMap.filterKeys(variableKeys).toSeq.map({ 
        case (k, v) => "%s-%s".format(k, v) }).mkString("_")
    }

    experimentMaps.map(entryTitle).toIndexedSeq
  }  
  
  def apply[E: JsonFormat : ClassTag](experiments: IndexedSeq[IndexedSeq[E]]): Table[E] = {
    // TODO: Replace with |everywhere| from shapeless when Scala 2.10 comes out.
    val experimentsFirstRow = experiments.head
    val experimentsFirstColumn = experiments.map(_.head)

    val tableTitle = title(experiments.flatten)
    val rowLabels = entryTitles(experimentsFirstColumn)
    val columnLabels = entryTitles(experimentsFirstRow)

    Table(tableTitle, rowLabels, columnLabels, experiments.toMatrix)
  }
}