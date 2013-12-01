package st.sparse.billy.experiments.wideBaseline

import st.sparse.billy._
import st.sparse.billy.experiments._
import java.io.File
import st.sparse.sundry._
import com.sksamuel.scrimage._
import scala.pickling._
import binary._
import st.sparse.sundry._
import breeze.linalg._
import org.opencv.core.KeyPoint
import thirdparty.jhlabs.image.PixelUtils
import st.sparse.persistentmap._
import javax.imageio.ImageIO

///////////////////////////////////////////////////////////

/**
 * Represents experiments on the Oxford image dataset.
 */
case class BlurredMiddlebury[D <% PairDetector, E <% Extractor[F], M <% Matcher[F], F](
  similarityThreshold: Double,
  numSmoothingIterations: Int,
  scaleFactor: Double,
  middlebury: Middlebury[D, E, M, F]) extends ExperimentImplementation[D, E, M, F] with Logging {
  override val maxPairedDescriptors = middlebury.maxPairedDescriptors
  override val detector = middlebury.detector
  override val extractor = middlebury.extractor
  override val matcher = middlebury.matcher

//  private val smoothInner: (ImagePOD, ImagePOD) => ImagePOD =
//    (imagePOD, disparityPOD) => {
//      val image = RichImage.fromPOD(imagePOD)
//      val disparity = RichImage.fromPOD(disparityPOD)
//
//      val smoothed = Stream.iterate(image) { image =>
//        image.anisotropicDiffusion(
//          similarityThreshold,
//          disparity)
//      }
//
//      smoothed(numSmoothingIterations).toPOD
//    }
//
//  private def smoothInnerMemo(implicit runtimeConfig: RuntimeConfig) =
//    PersistentMemo(
//      runtimeConfig.database,
//      "blurredMiddlebury_smooth",
//      smoothInner.tupled)
//
//  def smooth(implicit runtimeConfig: RuntimeConfig): ((Image, Image)) => Image =
//    imageAndDisparity => {
//      val (image, disparity) = imageAndDisparity
//      logger.debug("Smoothing image")
//      RichImage.fromPOD(smoothInnerMemo.apply((image.toPOD, disparity.toPOD)))
//    }

    // Pickling is failing for no apparent reason, so I'm reduced to a workaround.
    def smooth(implicit runtimeConfig: RuntimeConfig): ((Image, Image)) => Image =
      (imageAndDisparity) => {
        def raw: (Image, Image) => Image = (image, disparity) => {
          val small = image.scale(scaleFactor)
          val smallSmoothedStream = Stream.iterate(small) {
            _.anisotropicDiffusion(
              similarityThreshold,
              disparity)
          }
  
          val smallSmoothed = smallSmoothedStream(numSmoothingIterations)
          val smoothed = smallSmoothed.scale(1.0 / scaleFactor)
          assert(image.width == smoothed.width)
          assert(image.height == smoothed.height)
          assert(image.awt.getType == smoothed.awt.getType)
  
          smoothed
        }
  
        val (image, disparity) = imageAndDisparity
        val cacheFile = new File(
          s"/home/eric/t/2013_q4/pilgrimOutput/smoothScratch/${imageAndDisparity.hashCode.abs}_$numSmoothingIterations.png")
        if (!cacheFile.exists) {
          logger.debug("Smooth cache miss.")
          raw(image, disparity).write(cacheFile)
        } else logger.debug("Smooth cache hit.")
  
        Image(cacheFile)
      }

  //  def smooth(implicit runtimeConfig: RuntimeConfig): ((Image, Image)) => Image = {
  //    def raw: (Image, Image) => Image = (image, disparity) => {
  //      val smoothed = Stream.iterate(image) { image =>
  //        image.anisotropicDiffusion(
  //          similarityThreshold,
  //          disparity)
  //      }
  //
  //      smoothed(numSmoothingIterations)
  //    }
  //
  //    PersistentMemo(
  //      runtimeConfig.database,
  //      "blurredMiddlebury_smooth",
  //      raw.tupled)
  //  }

  override def leftImage(implicit runtimeConfig: RuntimeConfig) = {
    val disparity = Image(ExistingFile(new File(
      middlebury.databaseRoot,
      "disp1.png")))
    val image = middlebury.leftImage
    smooth.apply((image, disparity))
  }
  override def rightImage(implicit runtimeConfig: RuntimeConfig) = {
    val disparity = Image(ExistingFile(new File(
      middlebury.databaseRoot,
      "disp5.png")))
    val image = middlebury.rightImage
    smooth.apply((image, disparity))
  }
  override def correspondenceMap(implicit runtimeConfig: RuntimeConfig) =
    middlebury.correspondenceMap
  override def experimentParametersString =
    s"${similarityThreshold}_${numSmoothingIterations}_${middlebury.experimentParametersString}"
}

object BlurredMiddlebury