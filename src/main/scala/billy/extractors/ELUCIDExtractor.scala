package billy.extractors

import java.awt.image.BufferedImage

import nebula._
import nebula.imageProcessing._
import nebula.util._

import billy._
import billy.brown._
import billy.mpie._
import billy.smallBaseline._
import billy.wideBaseline._
import billy.summary._

import nebula._
import org.opencv.core.Mat
import org.opencv.core.MatOfKeyPoint
import org.opencv.features2d.DescriptorExtractor
import org.opencv.features2d.KeyPoint

import breeze.linalg.DenseMatrix
import breeze.linalg.DenseVector
import imageProcessing.ImageUtil
import imageProcessing.Pixel

import nebula.imageProcessing.RichImage.bufferedImage
import nebula.util.JSONUtil.AddClassName
import nebula.util.JSONUtil.singletonObject
import spray.json.DefaultJsonProtocol
import spray.json.JsonFormat
import util.OpenCVUtil
import util.Util
import nebula.util._

///////////////////////////////////////////////////////////

case class ELUCIDExtractor(
  normalizeRotation: Boolean,
  normalizeScale: Boolean,
  numSamplesPerRadius: Int,
  stepSize: Double,
  numRadii: Int,
  blurWidth: Int,
  color: String)

object ELUCIDExtractor {
  implicit def ELUCIDExtractor2Extractor(
    self: ELUCIDExtractor): Extractor[SortDescriptor] =
    Extractor(
      (image: BufferedImage, keyPoint: KeyPoint) => {
        val numSamples = self.numRadii * self.numSamplesPerRadius + 1
        val radii = (1 to self.numRadii).map(_ * self.stepSize)
        val angles = (0 until self.numSamplesPerRadius).map(
          _ * 2 * math.Pi / self.numSamplesPerRadius)

        def samplePattern(
          scaleFactor: Double,
          rotationOffset: Double): Seq[DenseVector[Double]] = {
          requirey(scaleFactor > 0)
          Seq(DenseVector(0.0, 0.0)) ++ (for (
            angle <- angles;
            radius <- radii
          ) yield {
            val scaledRadius = scaleFactor * radius
            val offsetAngle = rotationOffset + angle
            DenseVector(
              scaledRadius * math.cos(offsetAngle),
              scaledRadius * math.sin(offsetAngle))
          })
        }

        def samplePoints(keyPoint: KeyPoint): Seq[DenseVector[Double]] = {
          val scaleFactor = if (self.normalizeScale) {
            asserty(keyPoint.size > 0)
            keyPoint.size / 10.0
          } else 1

          val rotationOffset = if (self.normalizeRotation) {
            asserty(keyPoint.angle != -1)
            keyPoint.angle * 2 * math.Pi / 360
          } else 0

          //    println(rotationOffset)

          samplePattern(scaleFactor, rotationOffset).map(
            _ + DenseVector(keyPoint.pt.x, keyPoint.pt.y))
        }

        val blurred = ImageUtil.boxBlur(self.blurWidth, image)

        val pointOptions = samplePoints(keyPoint).map(point =>
          blurred.getSubPixel(point(0), point(1)))
        if (pointOptions.contains(None)) None
        else {
          val unsorted = pointOptions.flatten.flatMap(
            Extractor.interpretColor(self.color))
          Some(SortDescriptor.fromUnsorted(unsorted))
        }
      })
}

trait ELUCIDExtractorJsonProtocol extends DefaultJsonProtocol {
  implicit val elucidExtractorJsonProtocol =
    jsonFormat7(ELUCIDExtractor.apply).addClassInfo("ELUCIDExtractor")
}

object ELUCIDExtractorJsonProtocol extends ELUCIDExtractorJsonProtocol