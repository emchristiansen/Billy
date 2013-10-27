package st.sparse.billy.extractors

import org.opencv.core.Point
import com.sksamuel.scrimage.Image
import st.sparse.sundry.ExistingDirectory
import java.io.File
import st.sparse.billy.MatlabUtil
import breeze.linalg.DenseMatrix
import com.sksamuel.scrimage.PixelTools

trait Segmentation {
  def probabilityInSameSegment(left: Point, right: Point): Double
}

object MatlabGPbSegmenter {
  def boundaries(image: Image): DenseMatrix[Double] = {
    val gPbDirectory =
      ExistingDirectory(getClass.getResource("/matlab/gPb").getPath)

    val imagePath = File.createTempFile("MatlabGPbSegmenterImage", ".png")
    image.write(imagePath)

    val boundariesPath =
      File.createTempFile("MatlabGPbSegmenterBoundaries", ".png")

    // This Matlab script reads the image from `imagePath` and writes the
    // boundary image to `boundariesPath`.
    // The boundaries are probabilistic, and scaled in the range [0, 255].
    val command = s"gPbBoundaries $imagePath $boundariesPath"
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
}