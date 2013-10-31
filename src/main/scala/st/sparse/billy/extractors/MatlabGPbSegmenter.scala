package st.sparse.billy.extractors

import st.sparse.sundry._
import st.sparse.billy._
import org.opencv.core.Point
import com.sksamuel.scrimage.Image
import st.sparse.sundry.ExistingDirectory
import java.io.File
import st.sparse.billy.MatlabUtil
import breeze.linalg.DenseMatrix
import com.sksamuel.scrimage.PixelTools
import scala.collection.mutable.Queue

trait Segmentation {
  /**
   * The probability two points belong to the same segment.
   *
   * Sometimes this cannot be computed, such as when a point is on a
   * boundary.
   */
  def probabilityInSameSegment: (Point, Point) => Option[Double]
}

object Segmentation {
  def fromBoundariesImage(boundariesImage: Image): Segmentation = {
    val boundaries = boundariesImage.toGrayMatrix mapValues { _ / 255.0 }

    val step = 0.04
    val layers = (0.0 until 1.0 by step) map { probability =>
      val segments = boundaries mapValues (_ < probability)
      (MatlabGPbSegmenter.connectedComponentsLabels(segments), probability)
    }

    new Segmentation {
      override def probabilityInSameSegment = (left: Point, right: Point) => {
        // Floor is here because the segmentation logic doesn't work with
        // subpixels.
        val leftX = left.x.floor.toInt
        val leftY = left.y.floor.toInt
        val rightX = right.x.floor.toInt
        val rightY = right.y.floor.toInt

        require(leftX >= 0 && leftX < boundariesImage.width)
        require(leftY >= 0 && leftY < boundariesImage.height)
        require(rightX >= 0 && rightX < boundariesImage.width)
        require(rightY >= 0 && rightY < boundariesImage.height)

        val probabilityDifferent = layers.find {
          case (layer, _) =>
            val left = layer(leftY, leftX)
            val right = layer(rightY, rightX)
            left.isDefined && right.isDefined && left.get == right.get
        } map (_._2)

        probabilityDifferent map (1 - _)
      }
    }
  }
}

trait Segmenter {
  def segmentation: Image => Segmentation

  def boundaries: Image => DenseMatrix[Double]
}

trait SegmenterBoundaries extends Segmenter {
  override def segmentation = (image: Image) => {
    val boundaries = this.boundaries(image)

    val step = 0.04
    val layers = (0.0 until 1.0 by step) map { probability =>
      val segments = boundaries mapValues (_ < probability)
      (MatlabGPbSegmenter.connectedComponentsLabels(segments), probability)
    }

    new Segmentation {
      override def probabilityInSameSegment = (left: Point, right: Point) => {
        val leftX = left.x.round.toInt
        val leftY = left.y.round.toInt
        val rightX = right.x.round.toInt
        val rightY = right.y.round.toInt

        require(leftX >= 0 && leftX < image.width)
        require(leftY >= 0 && leftY < image.height)
        require(rightX >= 0 && rightX < image.width)
        require(rightY >= 0 && rightY < image.height)

        val probabilityDifferent = layers.find {
          case (layer, _) =>
            val left = layer(leftY, leftX)
            val right = layer(rightY, rightX)
            left.isDefined && right.isDefined && left.get == right.get
        } map (_._2)

        probabilityDifferent map (1 - _)
      }
    }
  }
}

object MatlabGPbSegmenter extends Logging {
  // Otherwise the Matlab code will blow up memory.
  // TODO: Make parameter.
  val maxComputablePixels = 400 * 400

  /**
   * Returns a map of the boundary probabilities.
   */
  def boundariesImage(image: Image): Image = {
    // `+ 1000` is fudge factor is case resizing isn't perfect.
    require(image.width * image.height <= maxComputablePixels + 1000)

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

    Image(boundariesPath)
  }

  def boundariesImageScaling(image: Image): Image = {
    val scaleFactor = {
      val numPixels = image.width * image.height
      List(1.0, maxComputablePixels.toDouble / numPixels).min
    }

    val smallImage = image.scale(scaleFactor)
    logger.debug(s"Image was ${image.width} by ${image.height}, and rescaled by a factor of ${scaleFactor} to get an image of size ${smallImage.width} by ${smallImage.height}.")
    
    val smallBoundaries = boundariesImage(smallImage)
    smallBoundaries.scaleTo(image.width, image.height)
  }

  def boundaries(image: Image): DenseMatrix[Double] = {
    val boundariesImage = this.boundariesImage(image)
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

    // Fill a connected component with the given label via something
    // like depth-first search.
    // We're not using the Wikipedia algorithm; it was faster to just write this
    // than bother to read that article.
    def fillRegionWithLabel(
      label: Int,
      rootRow: Int,
      rootColumn: Int) {
      val set = collection.mutable.Set((rootRow, rootColumn))

      while (set.nonEmpty) {
        val (row, column) = set.head
        set -= ((row, column))

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
          set += ((row, column))
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