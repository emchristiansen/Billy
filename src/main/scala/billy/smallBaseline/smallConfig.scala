package nebula.smallBaseline

import java.awt.image.BufferedImage
import scala.Option.option2Iterable
import org.opencv.features2d.{ DMatch, KeyPoint }
import breeze.linalg.DenseMatrix


import nebula.util.{ ExperimentIO, IO, JSONUtil }
import nebula._
import ExtractorJsonProtocol._
import MatcherJsonProtocol._
import nebula.summary.ExperimentSummary

import DetectorJsonProtocol._
import ExtractorJsonProtocol._
import MatcherJsonProtocol._



import nebula.util._

import DetectorJsonProtocol._
import ExtractorJsonProtocol._
import MatcherJsonProtocol._


import spray.json._

///////////////////////////////////////////////////////////

/* TODO

case class SmallBaselineExperiment(
  searchRadius: Int,
  imageClass: String,
  extractor: Extractor,
  matcher: Matcher)

object SmallBaselineExperiment {
  implicit def implicitExperiment(self: SmallBaselineExperiment): Experiment =
    new Experiment {
//      override def name = "SmallBaselineExperiment"
//      override def parameters = Seq(
//        ("SR", self.searchRadius.toString),
//        ("IC", self.imageClass),
//        ("E", JSONUtil.abbreviate(self.extractor)),
//        ("M", JSONUtil.abbreviate(self.matcher)))
      override def original = self

      override def getResults(implicit runtime: RuntimeConfig) =
        SmallBaselineExperimentResults(self)
    }

  implicit def implicitImagePairLike(
    self: SmallBaselineExperiment)(
      implicit runtime: RuntimeConfig): HasImagePair with HasGroundTruth[FlowField] =
    new HasImagePair with HasGroundTruth[FlowField] {
      override val SmallBaselinePair(
        leftImage,
        rightImage,
        groundTruth) = SmallBaselinePair.apply(
        runtime.projectChildPath("data/middleburyImages"),
        self.imageClass)
    }

  def estimateFlow(
    searchRadius: Int,
    extractor: Extractor,
    matcher: Matcher,
    leftImage: BufferedImage,
    rightImage: BufferedImage): FlowField = {
    // TODO: Make parameter
    val numSamples = 100

    val leftKeyPoints = {
      val all = for (
        y <- 0 until leftImage.getHeight;
        x <- 0 until leftImage.getWidth
      ) yield new KeyPoint(
        x,
        y,
        -1,
        -1,
        -1,
        -1,
        -1)
      (new scala.util.Random).shuffle(all).take(numSamples)
    }

    val flow = DenseMatrix.fill[Option[FlowVector]](
      leftImage.getHeight,
      leftImage.getWidth)(None)
    for (
      leftKeyPoint <- leftKeyPoints;
      leftDescriptor <- extractor.extract(leftImage, Seq(leftKeyPoint)).head
    ) {
      val leftX = leftKeyPoint.pt.x.round.toInt
      val leftY = leftKeyPoint.pt.y.round.toInt

      val rightKeyPoints = {
        // The first KeyPoint is the center, for sane behavior on flat regions.
        val firstKeyPoint = leftKeyPoint
        val otherKeyPoints =
          for (
            y <- leftY - searchRadius to leftY + searchRadius;
            x <- leftX - searchRadius to leftX + searchRadius;
            if y >= 0 && y < rightImage.getHeight;
            if x >= 0 && x < rightImage.getWidth;
            if x != leftX || y != leftY
          ) yield new KeyPoint(
            x,
            y,
            -1,
            -1,
            -1,
            -1,
            -1)
        Seq(firstKeyPoint) ++ otherKeyPoints
      }

      val (remainingRightKeyPoints, distances) = {
        val (remainingRightKeyPoints, rightDescriptors) = {
          val rightDescriptors = extractor.extract(rightImage, rightKeyPoints)
          rightKeyPoints.zip(rightDescriptors).filter(_._2.isDefined).unzip
        }
        val dmatches = matcher.doMatch(true, Seq(leftDescriptor), rightDescriptors.flatten)
        asserty(dmatches.size == rightDescriptors.size)
        (remainingRightKeyPoints, dmatches.map(_.distance))
      }

      val (bestKeyPoint, bestDistance) = remainingRightKeyPoints.zip(distances).minBy(_._2)

      val flowVector = FlowVector(
        bestKeyPoint.pt.x - leftKeyPoint.pt.x,
        bestKeyPoint.pt.y - leftKeyPoint.pt.y)

      flow(leftY, leftX) = Some(flowVector)
    }
    FlowField(flow)
  }

  implicit def implicitHasEstimate(
    self: SmallBaselineExperiment)(
      implicit runtime: RuntimeConfig): HasEstimate[FlowField] =
    new HasEstimate[FlowField] {
      override def estimate = {
        estimateFlow(
          self.searchRadius,
          self.extractor,
          self.matcher,
          self.leftImage,
          self.rightImage)
      }
    }
}

///////////////////////////////////////////////////////////

// TODO
case class SmallBaselineExperimentResults(
  experiment: SmallBaselineExperiment,
  groundTruth: FlowField,
  estimate: FlowField)

object SmallBaselineExperimentResults {
  def apply(
    experiment: SmallBaselineExperiment)(
      implicit runtime: RuntimeConfig): SmallBaselineExperimentResults = {
    val noResults = SmallBaselineExperimentResults(experiment, null, null)
    if (noResults.alreadyRun && runtime.skipCompletedExperiments) {
      val Some(file) = noResults.existingResultsFile
      println("Reading %s".format(file))
      val jsonString = org.apache.commons.io.FileUtils.readFileToString(file)
      jsonString.asJson.convertTo[SmallBaselineExperimentResults]
    } else run(experiment)
  }

  private def run(self: SmallBaselineExperiment)(
    implicit runtime: RuntimeConfig): SmallBaselineExperimentResults = {
    //    {
    //      // TODO: Remove this block
    //      val zeros = DenseMatrix.fill[Option[FlowVector]](
    //        self.groundTruth.rows,
    //        self.groundTruth.cols)(None)
    //      val xys = for (
    //        y <- 0 until zeros.rows;
    //        x <- 0 until zeros.cols
    //      ) yield (x, y)
    //
    //      for ((x, y) <- (new scala.util.Random).shuffle(xys).take(100)) zeros(y, x) = 
    //        Some(FlowVector(0, 0))
    //
    ////      println("l2 distance from zeros is: %.4f".format(
    ////        FlowField(zeros).l2Distance(self.groundTruth)))
    //    }

    //    println("l2 distance is: %.4f".format(self.estimate.l2Distance(self.groundTruth)))

    val results = SmallBaselineExperimentResults(self, self.groundTruth, self.estimate)
    results.save
    results
  }

  implicit def implicitExperimentResults(self: SmallBaselineExperimentResults): ExperimentResults =
    new ExperimentResults {
      override def experiment = self.experiment
      override def save(implicit runtime: RuntimeConfig) = {
        println("Writing to %s".format(self.path))
        val json = self.toJson
        org.apache.commons.io.FileUtils.writeStringToFile(self.path, json.prettyPrint)
      }
      override def original = self

      // TODO: Pull into another record.
      override def toSummary(implicit runtime: RuntimeConfig) = new ExperimentSummary {
        def original = self
        def results = self
        def summaryNumbers = Map(
          "mse" -> Memoize(() => self.groundTruth.mse(self.estimate)))
        def summaryImages = Map()
      }
    }
}

///////////////////////////////////////////////////////////

object SmallBaselineJsonProtocol extends DefaultJsonProtocol {
  implicit val smallBaselineExperiment =
    jsonFormat4(SmallBaselineExperiment.apply).addClassInfo("SmallBaselineExperiment")
    
  implicit val smallBaselineExperimentResults: RootJsonFormat[SmallBaselineExperimentResults] =
    jsonFormat3(SmallBaselineExperimentResults.apply).addClassInfo(
      "SmallBaselineExperimentResults")      
}

//object SmallBaselineExperimentSummary {
//  implicit def implicitSmallBaselineExperimentResults(self: SmallBaselineExperimentResults)(
//    implicit runtime: RuntimeConfig) = new {
//    def toSummary = new ExperimentSummary {
//      def original = self
//      def results = self
//      def summaryNumbers = Map(
//        "mse" -> Memoize(() => self.groundTruth.mse(self.estimate)))
//      def summaryImages = Map()
//    }
//  }
//}

*/