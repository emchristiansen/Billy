package billy

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
import org.scalatest.FunSuite
import javax.imageio.ImageIO
import java.io.File
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import nebula.util._
import billy.wideBaseline.WideBaselineExperiment
import shapeless._
import spray.json._
import reflect.runtime.universe._
import billy.JsonProtocols._

///////////////////////////////////////////////////////////

@RunWith(classOf[JUnitRunner])
class TestDistributed extends FunSuite {
  test("ensure implicits are found for WideBaselineExperiment", InstantTest) {
    val imageClasses = Seq(
      "graffiti",
      "trees",
      "jpeg",
      "boat",
      "bark",
      "bikes",
      "light",
      "wall").sorted

    val otherImages = Seq(2, 3, 4, 5, 6)

    val detectors =
      BoundedPairDetector(
        BoundedDetector(OpenCVDetector.FAST, 5000),
        200) :: HNil

    val logPolarExtractor = LogPolarExtractor(
      false,
      2,
      24,
      16,
      16,
      3,
      "Gray")

    val extractors = logPolarExtractor :: OpenCVExtractor.BRIEF :: OpenCVExtractor.SIFT :: OpenCVExtractor.SURF :: HNil

    val logPolarMatcherNCCL1 = LogPolarMatcher(
      PatchNormalizer.NCC,
      Matcher.L1,
      true,
      true,
      8)

    val logPolarMatcherNCCL2 = LogPolarMatcher(
      PatchNormalizer.NCC,
      Matcher.L2,
      true,
      true,
      8)

    val logPolarMatcherL1 = LogPolarMatcher(
      PatchNormalizer.Rank,
      Matcher.L1,
      true,
      true,
      8)

    val logPolarMatcherL2 = LogPolarMatcher(
      PatchNormalizer.Rank,
      Matcher.L2,
      true,
      true,
      8)

    val matchers = logPolarMatcherNCCL1 :: logPolarMatcherNCCL2 :: logPolarMatcherL1 :: logPolarMatcherL2 :: Matcher.L1 :: Matcher.L2 :: HNil

    val transposed = for (
      imageClass <- imageClasses;
      otherImage <- otherImages
    ) yield {
      val tuples = HListUtil.mkTuple3(detectors, extractors, matchers)

      object constructExperiment extends Poly1 {
        implicit def default[D <% PairDetector, E <% Extractor[F], M <% Matcher[F], F] = at[(D, E, M)] {
          case (detector, extractor, matcher) => {
            WideBaselineExperiment(imageClass, otherImage, detector, extractor, matcher)
          }
        }
      }

      // This lifting, combined with flatMap, filters out types that can't be used                                                                                                                                                                                                              
      // to construct experiments.                                                                                                                                                                                                                                                              
      object constructExperimentLifted extends Lift1(constructExperiment)

      val experiments = tuples flatMap constructExperimentLifted

      object constructCapstone extends Poly1 {
        implicit def default[E <% RuntimeConfig => ExperimentRunner[R] <% RuntimeConfig => StorageInfo[R]: JsonFormat: TypeTag, R <% RuntimeConfig => ExperimentSummary: TypeTag] = at[E] {
          experiment => Distributed.unsafeCapstone(experiment)
        }
      }

      object getJson extends Poly1 {
        implicit def default[E: JsonFormat] = at[E] { experiment =>
          {
            experiment.toJson
          }
        }
      }

      val capstones = experiments map constructCapstone
      val jsons = experiments map getJson
      capstones.toList zip jsons.toList
    }

    transposed.transpose
  }

  test("ensure implicits are found for another WideBaselineExperiment case") {
    val imageClasses = Seq(
      "graffiti",
      "trees",
      "jpeg",
      "boat",
      "bark",
      "bikes",
      "light",
      "wall").sorted

    val otherImages = Seq(2, 3, 4, 5, 6)

    val detector =
      BoundedPairDetector(
        BoundedDetector(OpenCVDetector.FAST, 5000),
        200)

    val transposed = for (
      imageClass <- imageClasses;
      otherImage <- otherImages
    ) yield {
      val experiments = (for (
        minRadius <- Seq(1, 2, 3, 4, 5);
        maxRadius <- Seq(8, 16, 24, 32, 40, 48, 56, 64);
        numScales <- Seq(1, 2, 4, 8, 16, 32, 64);
        numAngles <- Seq(1, 2, 4, 8, 16, 32, 64);
        blurWidth <- Seq(1, 2, 3, 4, 6, 8, 10);
        color <- Seq("Gray")
      ) yield {
        val extractor = LogPolarExtractor(
          false,
          minRadius,
          maxRadius,
          numScales,
          numAngles,
          blurWidth,
          color)

        for (scaleSearchRadiusFactor <- Seq(0, 0.05, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7)) yield {
          val scaleSearchRadius = (scaleSearchRadiusFactor * numScales).round.toInt

          val matcher = LogPolarMatcher(
            PatchNormalizer.NCC,
            Matcher.L2,
            true,
            true,
            scaleSearchRadius)

          WideBaselineExperiment(imageClass, otherImage, detector, extractor, matcher)
        }
      }).flatten

      val capstones = experiments map (e => Distributed.unsafeCapstone(e))
      val jsons = experiments map (_.toJson)
      capstones.toList zip jsons.toList
    }

    transposed.transpose

  }

  test("ensure implicits are found for BrownExperiment", InstantTest) {
    val datasets = Seq("liberty")
    val numMatchess = Seq(1000)
    val extractors = OpenCVExtractor.SIFT :: OpenCVExtractor.SURF :: HNil
    val matchers = Matcher.L2 :: HNil

    val transposed = for (
      dataset <- datasets;
      numMatches <- numMatchess
    ) yield {
      val tuples = HListUtil.mkTuple2(extractors, matchers)

      object constructExperiment extends Poly1 {
        implicit def default[E <% Extractor[F], M <% Matcher[F], F] = at[(E, M)] {
          case (extractor, matcher) => {
            BrownExperiment(dataset, numMatches, extractor, matcher)
          }
        }
      }

      // This lifting, combined with flatMap, filters out types that can't be used                                                                                                                                                                                                              
      // to construct experiments.                                                                                                                                                                                                                                                              
      object constructExperimentLifted extends Lift1(constructExperiment)

      val experiments = tuples flatMap constructExperimentLifted

      object constructCapstone extends Poly1 {
        implicit def default[E <% RuntimeConfig => ExperimentRunner[R] <% RuntimeConfig => StorageInfo[R]: JsonFormat: TypeTag, R <% RuntimeConfig => ExperimentSummary: TypeTag] = at[E] {
          experiment => Distributed.unsafeCapstone(experiment)
        }
      }

      object getJson extends Poly1 {
        implicit def default[E: JsonFormat] = at[E] { experiment =>
          {
            experiment.toJson
          }
        }
      }

      val capstones = experiments map constructCapstone
      val jsons = experiments map getJson
      capstones.toList zip jsons.toList
    }

    transposed.transpose
  }

  test("hlist stuff") {
    object mkBrown extends Poly2 {
      implicit def default[E <% Extractor[F], M <% Matcher[F], F, B] = at[(E, M), B] {
        case ((e, m), b) => {
          val exp = BrownExperiment("liberty", 1000, e, m)
          exp.toString
        }
      }
    }

    def foo[H <: HList, Out <: HList](h: HList)(
      implicit lift: nebula.LiftA2[mkBrown.type, shapeless.HList, shapeless.HList, Out]) {
      HListUtil.liftA2(mkBrown)(h, h)
    }

    //    val extractors = OpenCVExtractor.SIFT :: OpenCVExtractor.SURF :: HNil
    //    val matchers = Matcher.L2 :: HNil
    //    
    //    val tuples = HListUtil.mkTuple2(extractors, matchers)
    //    
    //    foo(tuples)
  }
}