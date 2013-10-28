package st.sparse.billy.experiments.wideBaseline

import st.sparse.billy._
import breeze.linalg._
import java.io.File
import st.sparse.sundry._
import grizzled.math.stats
import org.apache.commons.io.FileUtils

/////////////////////////////////////////////////////////////

case class Table(data: DenseMatrix[String]) {
  def csv: String = data.toSeqSeq.map(_.mkString(", ")).mkString("\n")
  def tsv: String = data.toSeqSeq.map(_.mkString("\t")).mkString("\n")
}

object Table extends Logging {
  /**
   * Creates a table of experimental results.
   *
   * The table is represented as a `DenseMatrix[String]`, so it can be
   * trivially mapped to a CSV file.
   * `rowView` and `columnView` should be two complementary ways of viewing
   * and experiment; for example, `rowView` could show the model parameters,
   * and `columnView` could show the experimental parameters.
   * `resultsView` should show what you're measuring, such as the recognition
   * rate or the AUC.
   */
  def apply[E](
    experimentsWithResults: Seq[(E, Results)],
    rowView: E => String,
    columnView: E => String,
    resultsView: Results => String): Table = {
    val uniqueRowViews =
      experimentsWithResults.map(_._1).map(rowView).toSet.toList.sorted
    logger.debug(s"uniqueRowViews: $uniqueRowViews")
    val uniqueColumnViews =
      experimentsWithResults.map(_._1).map(columnView).toSet.toList.sorted
    logger.debug(s"uniqueColumnViews: $uniqueColumnViews")

    // The row and column views have to start at indices (1, 0) and (0, 1),
    // respectively, thus the "+ 1".
    val tableRows = uniqueRowViews.size + 1
    val tableColumns = uniqueColumnViews.size + 1
    val data = DenseMatrix.fill[String](tableRows, tableColumns)("")
    logger.debug(s"data: $data")

    // Now we populate the table.
        // Oh, mutability.
    uniqueRowViews.zipWithIndex foreach {
      case (view, row) => data(row + 1, 0) = view
    }
    
    uniqueColumnViews.zipWithIndex foreach {
      case (view, column) => data(0, column + 1) = view
    }
    
    for ((experiment, results) <- experimentsWithResults) {
      val rowIndex = uniqueRowViews.indexOf(rowView(experiment)) + 1
      val columnIndex = uniqueColumnViews.indexOf(columnView(experiment)) + 1
      data(rowIndex, columnIndex) = resultsView(results)
    }

    Table(data)
  }
}