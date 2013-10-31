package st.sparse.billy.extractors

import com.sksamuel.scrimage.filter.GaussianBlurFilter
import org.opencv.core.Point
import st.sparse.sundry._
import st.sparse.sundry.ExpectyOverrides._
import scala.util.Try
import com.sksamuel.scrimage.Image
import st.sparse.billy.Gray
import st.sparse.billy._
import com.sksamuel.scrimage.PixelTools
import java.io.File
import org.apache.commons.io.FileUtils

// TODO: Figure out what to do with `logRoot`.
case class BRIEFSExtractor(
  numPairs: Int,
  patchWidth: Int,
  blurWidth: Int)(
    implicit logRoot: LogRoot) extends ExtractorSeveral[IndexedSeq[(Boolean, Option[Double], Option[Double])]] with Logging {
  require(numPairs > 0)
  require(patchWidth > 0)
  require(blurWidth > 0)

  val random = new util.Random(0)
  val pointPairs = numPairs times {
    def nextCoordinate() = random.nextDouble * patchWidth

    val left = new Point(nextCoordinate(), nextCoordinate())
    val right = new Point(nextCoordinate(), nextCoordinate())
    (left, right)
  }

  override def extract = (image, keyPoints) => {
    val rgbPatchOptions = {
      val blurred = image.filter(GaussianBlurFilter(blurWidth))
      assert(blurred.width == image.width)
      assert(blurred.height == image.height)
      keyPoints map { keyPoint =>
        Try(blurred.subpixelSubimageCenteredAtPoint(
          keyPoint.pt.x,
          keyPoint.pt.y,
          patchWidth.toDouble / 2,
          patchWidth.toDouble / 2)).toOption
      }
    }

    val boundariesPatchOptions = {
      val boundaries = MatlabGPbSegmenter.boundariesImageScaling(image)
      assert(boundaries.width == image.width)
      assert(boundaries.height == image.height)
      keyPoints map { keyPoint =>
        Try(boundaries.subpixelSubimageCenteredAtPoint(
          keyPoint.pt.x,
          keyPoint.pt.y,
          patchWidth.toDouble / 2,
          patchWidth.toDouble / 2)).toOption
      }
    }

    assert(rgbPatchOptions.size == boundariesPatchOptions.size)

    for (
      (rgbPatchOption, boundariesPatchOption) <- rgbPatchOptions.zip(boundariesPatchOptions)
    ) yield {
      for (
        rgbPatch <- rgbPatchOption;
        boundariesPatch <- boundariesPatchOption
      ) yield {
        val segmentation = Segmentation.fromBoundariesImage(boundariesPatch)
        val centerPoint = new Point(patchWidth / 2.0, patchWidth / 2.0)
        def probabilityFromCenterSegment(otherPoint: Point) =
          segmentation.probabilityInSameSegment(centerPoint, otherPoint)

        pointPairs map {
          case (leftPoint, rightPoint) =>
            val leftPixel =
              PixelTools.gray(rgbPatch.subpixel(leftPoint.x, leftPoint.y))
            val rightPixel =
              PixelTools.gray(rgbPatch.subpixel(rightPoint.x, rightPoint.y))

            val comparison =
              PixelTools.gray(leftPixel) < PixelTools.gray(rightPixel)
            val leftProbability = probabilityFromCenterSegment(leftPoint)
            val rightProbability = probabilityFromCenterSegment(rightPoint)

            logDirectory("BRIEFS_features") { directory: ExistingDirectory =>
              rgbPatch.write(new File(directory, "rgbPatch.png"))
              boundariesPatch.write(new File(directory, "boundariesPatch.png"))

              val message = Seq(
                "leftPoint: " + leftPoint,
                "rightPoint: " + rightPoint,
                "leftPixel: " + leftPixel,
                "rightPixel: " + rightPixel,
                "comparison: " + comparison,
                "leftProbability: " + leftProbability,
                "rightProbability: " + rightProbability).mkString("\n")
              FileUtils.writeStringToFile(
                new File(directory, "log.txt"),
                message)
            }

            (comparison, leftProbability, rightProbability)
        }
      }
    }
  }
}