package st.sparse.billy.extractors

import st.sparse.billy.internal._
import org.opencv.core.Point
import com.sksamuel.scrimage.Image
import st.sparse.sundry.ExistingDirectory
import java.io.File
import st.sparse.billy.MatlabUtil
import breeze.linalg.DenseMatrix
import com.sksamuel.scrimage.PixelTools
import scala.collection.mutable.Queue

trait Segmentation {
  def probabilityInSameSegment(left: Point, right: Point): Double
}

object MatlabGPbSegmenter {
  /**
   * Returns a map of the boundary probabilities.
   */
  def boundaries(image: Image): DenseMatrix[Double] = {
    // Otherwise the Matlab code will blow up memory.
    require(image.width <= 500 && image.height <= 500)

    val gPbDirectory =
      ExistingDirectory(getClass.getResource("/matlab/gPb").getPath)

    val imagePath = File.createTempFile("MatlabGPbSegmenterImage", ".png")
    image.write(imagePath)

    val boundariesPath =
      File.createTempFile("MatlabGPbSegmenterBoundaries", ".png")

    // This Matlab script reads the image from `imagePath` and writes the
    // boundary image to `boundariesPath`.
    // The boundaries are probabilistic, and scaled in the range [0, 255].
    val command = s"gPbBoundaries('$imagePath', '$boundariesPath');"
    MatlabUtil.runInDirectory(gPbDirectory, command)

    val boundariesImage = Image(boundariesPath)
    DenseMatrix.tabulate[Double](
      boundariesImage.height,
      boundariesImage.width) {
        case (y, x) => {
          PixelTools.gray(boundariesImage.pixel(x, y)) / 255.0
        }
      }
  }

  /**
   * The connected components labels:
   * http://en.wikipedia.org/wiki/Connected-component_labeling
   *
   * This is like `bwlabel` in Matlab.
   */
  def connectedComponentsLabels(
    mask: DenseMatrix[Boolean]): DenseMatrix[Option[Int]] = {
    val labels = DenseMatrix.fill[Option[Int]](mask.rows, mask.cols)(None)

    // Fill a connected component with the given label via depth-first search.
    // We're not using the Wikipedia algorithm; it was faster to just write this
    // than bother to read that article.
    def fillRegionWithLabel(
      label: Int,
      rootRow: Int,
      rootColumn: Int) {
      val queue = Queue((rootRow, rootColumn))

      while (queue.nonEmpty) {
        val (row, column) = queue.dequeue()
        labels(row, column) = Some(label)

        val west = (row, column - 1)
        val east = (row, column + 1)
        val north = (row - 1, column)
        val south = (row + 1, column)

        for (
          (row, column) <- Seq(west, east, north, south);
          if row >= 0 &&
            row < mask.rows &&
            column >= 0 &&
            column < mask.cols &&
            mask(row, column) &&
            !labels(row, column).isDefined
        ) {
          queue enqueue ((row, column))
        }
      }
    }

    var nextLabel = 0
    for (
      row <- 0 until mask.rows;
      column <- 0 until mask.cols;
      if mask(row, column) && !labels(row, column).isDefined
    ) {
      fillRegionWithLabel(nextLabel, row, column)
      nextLabel += 1
    }

    labels
  }
}