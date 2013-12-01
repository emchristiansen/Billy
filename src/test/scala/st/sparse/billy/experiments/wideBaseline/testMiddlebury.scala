package st.sparse.billy.experiments.wideBaseline

import st.sparse.billy._
import st.sparse.billy.experiments._
import st.sparse.billy.detectors._
import st.sparse.billy.extractors._
import st.sparse.billy.matchers._
import org.scalatest.fixture
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalacheck.Gen
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import scala.pickling._
import scala.pickling.binary._
import st.sparse.sundry._
import breeze.linalg.DenseMatrix
import scala.reflect.ClassTag
import com.sksamuel.scrimage._
import org.opencv.core.KeyPoint
import java.io.File

////////////////////////////////////////////////////////////////////////////////

@RunWith(classOf[JUnitRunner])
class TestMiddlebury extends FunGeneratorSuite with st.sparse.billy.experiments.TestUtil {
  test("pickling", InstantTest) {
    val experiment = Middlebury(
      2006,
      "Flowerpots",
      10,
      DoublyBoundedPairDetector(2, 10, 100, OpenCVDetector.FAST),
      OpenCVExtractor.SIFT,
      VectorMatcher.L1)
    val pickled = experiment.pickle
    val unpickled = pickled.unpickle[Middlebury[DoublyBoundedPairDetector[OpenCVDetector.FAST.type], OpenCVExtractor.SIFT.type, VectorMatcher.L1.type, IndexedSeq[Double]]]
    assert(unpickled == experiment)
  }

  test("pixels should roughly match on Flowerpots", MediumTest, InteractiveTest) {
    val experiment = Middlebury(
      2006,
      "Flowerpots",
      10,
      DoublyBoundedPairDetector(2, 10, 100, OpenCVDetector.SIFT),
      OpenCVExtractor.SIFT,
      VectorMatcher.L2)

    val leftImage = experiment.leftImage
    val rightImage = experiment.rightImage
    val stereoDisparity = experiment.stereoDisparity

    logImage("Flowerpots_leftImage", leftImage)
    logImage("Flowerpots_rightImage", rightImage)
    val disparityImage = {
      val image = Image.filled(
        stereoDisparity.data.cols,
        stereoDisparity.data.rows,
        0).toMutable
      stereoDisparity.data.mapPairs {
        case ((y, x), value) => value match {
          case None => image.setPixel(x, y, 0)
          case Some(offsetDouble) =>
            val offset = offsetDouble.toInt
            image.setPixel(x, y, PixelTools.argb(255, offset, offset, offset))
        }
      }
      image
    }
    logImage("Flowerpots_disparity", disparityImage)

    logger.debug(s"stereoDisparity: $stereoDisparity")

    // We will populate these two initially blank images with supposedly
    // corresponding pixels from the left and right images.
    // If the correspondence is correct, they should end up looking very
    // similar.
    val fromLeft =
      Image.filled(rightImage.width, rightImage.height, 0).toMutable
    val fromRight =
      Image.filled(rightImage.width, rightImage.height, 0).toMutable
    for (
      y <- 0 until leftImage.height;
      x <- 0 until leftImage.width
    ) {
      val warpedKeyPointOption = stereoDisparity.transformXYOnly(new KeyPoint(
        x,
        y,
        0))
      for (warpedKeyPoint <- warpedKeyPointOption) {
        val xWarped = warpedKeyPoint.pt.x.round.toInt
        assert(y == warpedKeyPoint.pt.y.round.toInt)

        // Warps should all go to the left.
        assert(xWarped <= x)

        logger.trace(s"x, y, xWarped: $x, $y, $xWarped")

        if (xWarped >= 0) {
          fromLeft.setPixel(xWarped, y, leftImage.pixel(x, y))
          fromRight.setPixel(xWarped, y, rightImage.pixel(xWarped, y))
        }
      }
    }

    val fromLeftPixels = fromLeft.copy.toSeqSeq.flatten flatMap {
      case (_, red, green, blue) => Seq(red, green, blue)
    }
    val fromRightPixels = fromRight.copy.toSeqSeq.flatten flatMap {
      case (_, red, green, blue) => Seq(red, green, blue)
    }

    val l2Distance = VectorMatcher.l2Distance(
      fromLeftPixels.toIndexedSeq,
      fromRightPixels.toIndexedSeq)
    val averageL2Distance = l2Distance / fromLeftPixels.size
    assert(averageL2Distance < 0.1)

    logImage("Flowerpots_fromLeft", fromLeft)
    logImage("Flowerpots_fromRight", fromRight)
  }

  test("pixels should roughly match on Moebius", MediumTest, InteractiveTest) {
    val experiment = Middlebury(
      2005,
      "Moebius",
      10,
      DoublyBoundedPairDetector(2, 10, 100, OpenCVDetector.SIFT),
      OpenCVExtractor.SIFT,
      VectorMatcher.L2)

    val leftImage = experiment.leftImage
    val rightImage = experiment.rightImage
    val stereoDisparity = experiment.stereoDisparity

    logImage("Moebius_leftImage", leftImage)
    logImage("Moebius_rightImage", rightImage)
    val disparityImage = {
      val image = Image.filled(
        stereoDisparity.data.cols,
        stereoDisparity.data.rows,
        0).toMutable
      stereoDisparity.data.mapPairs {
        case ((y, x), value) => value match {
          case None => image.setPixel(x, y, 0)
          case Some(offsetDouble) =>
            val offset = offsetDouble.toInt
            image.setPixel(x, y, PixelTools.argb(255, offset, offset, offset))
        }
      }
      image
    }
    logImage("Moebius_disparity", disparityImage)

    logger.debug(s"stereoDisparity: $stereoDisparity")

    // We will populate these two initially blank images with supposedly
    // corresponding pixels from the left and right images.
    // If the correspondence is correct, they should end up looking very
    // similar.
    val fromLeft =
      Image.filled(rightImage.width, rightImage.height, 0).toMutable
    val fromRight =
      Image.filled(rightImage.width, rightImage.height, 0).toMutable
    for (
      y <- 0 until leftImage.height;
      x <- 0 until leftImage.width
    ) {
      val warpedKeyPointOption = stereoDisparity.transformXYOnly(new KeyPoint(
        x,
        y,
        0))
      for (warpedKeyPoint <- warpedKeyPointOption) {
        val xWarped = warpedKeyPoint.pt.x.round.toInt
        assert(y == warpedKeyPoint.pt.y.round.toInt)

        // Warps should all go to the left.
        assert(xWarped <= x)

        logger.trace(s"x, y, xWarped: $x, $y, $xWarped")

        if (xWarped >= 0) {
          fromLeft.setPixel(xWarped, y, leftImage.pixel(x, y))
          fromRight.setPixel(xWarped, y, rightImage.pixel(xWarped, y))
        }
      }
    }

    val fromLeftPixels = fromLeft.copy.toSeqSeq.flatten flatMap {
      case (_, red, green, blue) => Seq(red, green, blue)
    }
    val fromRightPixels = fromRight.copy.toSeqSeq.flatten flatMap {
      case (_, red, green, blue) => Seq(red, green, blue)
    }

    val l2Distance = VectorMatcher.l2Distance(
      fromLeftPixels.toIndexedSeq,
      fromRightPixels.toIndexedSeq)
    val averageL2Distance = l2Distance / fromLeftPixels.size
    assert(averageL2Distance < 0.1)

    logImage("Moebius_fromLeft", fromLeft)
    logImage("Moebius_fromRight", fromRight)
  }

  test("run FAST SIFT L1", MediumTest) {
    val experiment = Middlebury(
      2006,
      "Flowerpots",
      10,
      DoublyBoundedPairDetector(2, 10, 100, OpenCVDetector.FAST),
      OpenCVExtractor.SIFT,
      VectorMatcher.L1)

    val results = experiment.run
    assert(results.distances.rows == results.distances.cols)
    results.distances.foreachValue(distance => assert(distance >= 0))
  }

  test("run SURF BRISK L0 cached", MediumTest) {
    val experiment = Middlebury(
      2006,
      "Flowerpots",
      10,
      DoublyBoundedPairDetector(2, 20, 100, OpenCVDetector.SURF),
      OpenCVExtractor.BRISK,
      VectorMatcher.L0)

    val cached = Experiment.cached(experiment)
    val (results0, time0) = time(cached.run)
    val (results1, time1) = time(cached.run)
    
    logger.info(s"time0: $time0")
    logger.info(s"time1: $time1")
    assert(results0 == results1)
    
    assert(results0.distances.rows == results0.distances.cols)
    results0.distances.foreachValue(distance => assert(distance >= 0))
  }
}