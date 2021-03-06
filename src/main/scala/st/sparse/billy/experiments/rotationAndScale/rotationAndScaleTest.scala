package st.sparse.billy.experiments.rotationAndScale;
//package billy.experiments.rotationAndScale
//
//import java.io.File
//import nebula._
//import nebula.imageProcessing._
//import nebula.util._
//import billy._
//import billy.brown._
//import billy.smallBaseline._
//import billy.wideBaseline._
//import billy.summary._
//import billy.detectors._
//import billy.extractors._
//import billy.matchers._
//import javax.imageio.ImageIO
//import java.awt.geom.AffineTransform
//import java.awt.image.{AffineTransformOp, BufferedImage}
//import org.apache.commons.math3.linear._
//
/////////////////////////////////////////////////////////////
//
//// TODO: Clean this up. This is very similar to WideBaselineExperiment
//case class RotationAndScaleExperiment[D <% PairDetector, E <% Extractor[F], M <% Matcher[F], F](
//  imageClass: String,
//  detector: D,
//  extractor: E,
//  matcher: M,
//  scaleFactor: Double,
//  theta: Double) {
//  assert(scaleFactor > 0)
//  assert(theta >= 0)
//  assert(theta < 2 * math.Pi)
//}
//
//trait RotationAndScaleExperiment2StorageInfo {
//  // TODO: Refactor when Scala type inference bug is fixed.      
//  implicit def WTFRotationAndScaleExperiment2StorageInfoImplicit[D <% PairDetector, E <% Extractor[F], M <% Matcher[F], F](
//    self: RotationAndScaleExperiment[D, E, M, F])(
//      implicit runtimeConfig: RuntimeConfig): StorageInfo[RotationAndScaleExperimentResults[D, E, M, F]] =
//    new StorageInfo.Experiment2StorageInfo(self)(runtimeConfig)
//
//  // TODO: Refactor when Scala type inference bug is fixed. 
//  implicit def WTFRotationAndScaleExperiment2StorageInfo[D <% PairDetector, E <% Extractor[F], M <% Matcher[F], F](
//    self: RotationAndScaleExperiment[D, E, M, F])(
//      runtimeConfig: RuntimeConfig): StorageInfo[RotationAndScaleExperimentResults[D, E, M, F]] =
//    new StorageInfo.Experiment2StorageInfo(self)(runtimeConfig)
//}
//
//trait RotationAndScaleExperiment2ExperimentRunner {
//  // TODO: Refactor when Scala inference bug is fixed.
//  implicit def WTFRotationAndScaleExperiment2ExperimentRunner[D <% PairDetector, E <% Extractor[F], M <% Matcher[F], F](
//    self: RotationAndScaleExperiment[D, E, M, F])(
//      implicit runtimeConfig: RuntimeConfig) =
//    new RotationAndScaleExperiment2ExperimentRunner(self)(runtimeConfig)
//
//  implicit class RotationAndScaleExperiment2ExperimentRunner[D <% PairDetector, E <% Extractor[F], M <% Matcher[F], F](
//    self: RotationAndScaleExperiment[D, E, M, F])(
//      runtimeConfig: RuntimeConfig) extends ExperimentRunner[RotationAndScaleExperimentResults[D, E, M, F]] {
//    implicit val iRC = runtimeConfig
//    override def run = {
//      if (runtimeConfig.skipCompletedExperiments &&
//        self.mostRecentPath.isDefined) {
//        self.load.get
//      } else {
//        def leftImage = {
//          val file = new File(
//            runtimeConfig.dataRoot,
//            s"oxfordImages/${self.imageClass}/images/img1.bmp").mustExist
//          Image.read(file)
//        }
//
//        val translationTransform =
//          AffineTransform.getTranslateInstance(
//            -leftImage.getWidth / 2.0,
//            -leftImage.getHeight / 2.0)
//        val inverseTranslationTransform =
//          AffineTransform.getTranslateInstance(
//            leftImage.getWidth / 2.0,
//            leftImage.getHeight / 2.0)
//        val scaleTransform =
//          AffineTransform.getScaleInstance(self.scaleFactor, self.scaleFactor)
//        val rotationTransform =
//          AffineTransform.getRotateInstance(self.theta)
//        val similarityTransform = {
//          val identity = new AffineTransform
//          identity.concatenate(inverseTranslationTransform)
//          identity.concatenate(scaleTransform)
//          identity.concatenate(rotationTransform)
//          identity.concatenate(translationTransform)
//          identity
//        }
//
//        val similarityOp = new AffineTransformOp(
//          similarityTransform,
//          AffineTransformOp.TYPE_BILINEAR)
//
//        val groundTruth: Homography = {
//          val affineMatrix = Array(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
//          similarityTransform.getMatrix(affineMatrix)
//          assert(affineMatrix.size == 6)
//          val homographyData = new Array2DRowRealMatrix(3, 3)
//          homographyData.setEntry(0, 0, affineMatrix(0))
//          homographyData.setEntry(1, 0, affineMatrix(1))
//          homographyData.setEntry(2, 0, 0)
//          homographyData.setEntry(0, 1, affineMatrix(2))
//          homographyData.setEntry(1, 1, affineMatrix(3))
//          homographyData.setEntry(2, 1, 0)
//          homographyData.setEntry(0, 2, affineMatrix(4))
//          homographyData.setEntry(1, 2, affineMatrix(5))
//          homographyData.setEntry(2, 2, 1)
//          Homography(homographyData)
//        }
//
//        println(groundTruth)
//
//        //////////////////////
//
//        val rightImage: Image = {
//          //          if (self.scaleFactor == 1 && self.theta == 0) {
//          //            println("here")
//          //            leftImage
//          //          }
//          //          else {
//          val image = new BufferedImage(
//            leftImage.getWidth,
//            leftImage.getHeight,
//            leftImage.getType)
//          similarityOp.filter(leftImage, image)
//          Image(image)
//          //          }
//        }
//
//        def debug {
//          def saveNoOverwrite(image: Image, file: File) {
//            ImageIO.write(image, "png", file)
//          }
//
//          val root =
//            homeDirectory + s"Dropbox/transfer/rotationAndScale/${self.imageClass}"
//          val baseFile = root + "/base.png"
//          saveNoOverwrite(leftImage, baseFile)
//
//          val warpedName = s"warped${self.scaleFactor}_${self.theta}.png"
//          val warpedFile = root + s"/$warpedName"
//          saveNoOverwrite(rightImage, warpedFile)
//        }
//
//        //        debug
//
//        //      nebula.TestUtil.dumpImage("imageLeft", leftImage)
//        //      nebula.TestUtil.dumpImage("imageRight", rightImage)
//
//        //      println(leftImage.getType)
//        //      println(rightImage.getType)
//        //      
//        //      println(similarityTransform)
//        //      println(similarityOp)
//        //      println(rightImage.getWidth)
//        //      println(rightImage.getHeight)
//
//        val (leftKeyPoints, rightKeyPoints) = self.detector.detectPair(
//          groundTruth,
//          leftImage,
//          rightImage) unzip
//
//        println(s"Number of KeyPoints: ${leftKeyPoints.size}")
//
//        //        (leftKeyPoints zip rightKeyPoints) foreach {
//        //          case (left, right) =>
//        //            println(left)
//        //            println(right)
//        //        }
//        //
//        //        (leftKeyPoints zip rightKeyPoints) foreach {
//        //          case (left, right) =>
//        //            println(left)
//        //            println(right)
//        //            assert(left.pt.x == right.pt.x)
//        //            assert(left.pt.y == right.pt.y)
//        //            assert(left.size == right.size)
//        //            assert(left.angle == right.angle)
//        //            assert(left.response == right.response)
//        //            assert(left.octave == right.octave)
//        //            assert(left.class_id == right.class_id)
//        //        }
//
//        val (leftDescriptors, rightDescriptors) = {
//          val leftDescriptors = self.extractor.extract(leftImage, leftKeyPoints)
//          val rightDescriptors = self.extractor.extract(rightImage, rightKeyPoints)
//
//          for ((Some(left), Some(right)) <- leftDescriptors.zip(rightDescriptors)) yield (left, right)
//        } unzip
//
//        println(s"Number of surviving KeyPoints: ${leftDescriptors.size}")
//
//        //        (leftDescriptors zip rightDescriptors) foreach {
//        //          case (left, right) =>
//        //            println(left)
//        //            println(right)
//        //        }
//
//        val dmatches = self.matcher.doMatch(true, leftDescriptors, rightDescriptors)
//
//        val results = RotationAndScaleExperimentResults(self, dmatches)
//        self.save(results)
//        results
//      }
//    }
//  }
//}
//
//object RotationAndScaleExperiment extends RotationAndScaleExperiment2StorageInfo with RotationAndScaleExperiment2ExperimentRunner
//
////////////////////////////////////////
//
//case class RotationAndScaleExperimentResults[D, E, M, F](
//  experiment: RotationAndScaleExperiment[D, E, M, F],
//  dmatches: Seq[DMatch])
//
//object RotationAndScaleExperimentResults {
//  implicit def implicitExperimentSummary[D, E, M, F](
//    self: RotationAndScaleExperimentResults[D, E, M, F])(
//      runtimeConfig: RuntimeConfig) = {
//    implicit val iRC = runtimeConfig
//    ExperimentSummary(
//      Map(
//        "recognitionRate" -> (SummaryUtil.recognitionRate(self.dmatches))),
//      // TODO
//      //      Map(
//      //        "histogram" -> (Histogram(self, "").render)),
//      Map("precisionRecall" -> SummaryUtil.precisionRecall(self.dmatches)),
//      Map())
//  }
//
//  // TODO: Remove when Scala inference bug is fixed.
//  implicit def WTFImplicitRotationAndScaleExperimentSummary[D, E, M, F](
//    self: RotationAndScaleExperimentResults[D, E, M, F])(
//      implicit runtimeConfig: RuntimeConfig) = implicitExperimentSummary(self)(runtimeConfig)
//}
