package st.sparse.billy.experiments.wideBaseline

import st.sparse.billy._

import breeze.linalg._

import java.io.File

import st.sparse.sundry._
import grizzled.math.stats

/////////////////////////////////////////////////////////////

object Summary {
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
  def table[E](
    experimentsWithResults: Seq[(E, Results)],
    rowView: E => String,
    columnView: E => String,
    resultsView: Results => String): DenseMatrix[String] = {
    val uniqueRowViews =
      experimentsWithResults.map(_._1).map(rowView).toSet.toList.sorted
    val uniqueColumnViews =
      experimentsWithResults.map(_._1).map(columnView).toSet.toList.sorted

    // The row and column views have to start at indices (1, 0) and (0, 1),
    // respectively, thus the "+ 1".
    val tableRows = uniqueRowViews.size + 1
    val tableColumns = uniqueColumnViews.size + 1
    val table = new DenseMatrix[String](tableRows, tableColumns)
    
    // Now we populate the table.
    // Oh, mutability.
    for ((experiment, results) <- experimentsWithResults) {
      val rowIndex = uniqueRowViews.indexOf(rowView(experiment))
      val columnIndex = uniqueColumnViews.indexOf(columnView(experiment))
      table(rowIndex, columnIndex) = resultsView(results)
    }
      
    table
  }
}