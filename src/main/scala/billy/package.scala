import nebula._
import nebula.imageProcessing._
import nebula.util._

import billy._
import billy.brown._
import billy.mpie._
import billy.smallBaseline._
import billy.wideBaseline._
import billy.summary._
import billy.detectors._
import billy.extractors._
import billy.matchers._

package object billy extends Distributed {
  // This can all be deleted when implcit macros are fixed.
  implicit val typeNameDoubleTODO =
    StaticTypeName.typeNameFromConcrete[Double]
  implicit val typeNameRuntimeConfigDoubleTODO =
    StaticTypeName.typeNameFromConcrete[RuntimeConfig => Double]
  implicit val typeNameRuntimeConfigTODO =
    StaticTypeName.typeNameFromConcrete[RuntimeConfig]
//  implicit val typeNamePatchNormalizerRawTODO =
//    StaticTypeName.typeNameFromConcreteInstance(PatchNormalizer.Raw)
//  implicit val typeNamePatchNormalizerNCCTODO =
//    StaticTypeName.typeNameFromConcreteInstance(PatchNormalizer.NCC)
//  implicit val typeNamePatchNormalizerRankTODO =
//    StaticTypeName.typeNameFromConcreteInstance(PatchNormalizer.Rank)
  implicit val typeNameTODO_0 =
    StaticTypeName.typeNameFromConcrete[OpenCVDetector.DENSE.type]
  implicit val typeNameTODO_1 =
    StaticTypeName.typeNameFromConcrete[OpenCVDetector.FAST.type]
  implicit val typeNameTODO_2 =
    StaticTypeName.typeNameFromConcrete[OpenCVDetector.BRISK.type]
  implicit val typeNameTODO_3 =
    StaticTypeName.typeNameFromConcrete[OpenCVDetector.SIFT.type]
  implicit val typeNameTODO_4 =
    StaticTypeName.typeNameFromConcrete[OpenCVDetector.SURF.type]
  implicit val typeNameTODO_5 =
    StaticTypeName.typeNameFromConcrete[OpenCVDetector.ORB.type]
  implicit val typeNameTODO_6 =
    StaticTypeName.typeNameFromConcrete[JSONAndTypeName]
  implicit val typeNameTODO_7 =
    StaticTypeName.typeNameFromConcrete[OpenCVExtractor.BRISK.type]
  implicit val typeNameTODO_8 =
    StaticTypeName.typeNameFromConcrete[OpenCVExtractor.FREAK.type]
  implicit val typeNameTODO_9 =
    StaticTypeName.typeNameFromConcrete[OpenCVExtractor.BRIEF.type]
  implicit val typeNameTODO_10 =
    StaticTypeName.typeNameFromConcrete[OpenCVExtractor.ORB.type]
  implicit val typeNameTODO_11 =
    StaticTypeName.typeNameFromConcrete[OpenCVExtractor.SIFT.type]
  implicit val typeNameTODO_12 =
    StaticTypeName.typeNameFromConcrete[OpenCVExtractor.SURF.type]
  implicit val typeNameTODO_13 =
    StaticTypeName.typeNameFromConcrete[VectorMatcher.L0.type]
  implicit val typeNameTODO_14 =
    StaticTypeName.typeNameFromConcrete[VectorMatcher.L1.type]
  implicit val typeNameTODO_15 =
    StaticTypeName.typeNameFromConcrete[VectorMatcher.L2.type]
  implicit val typeNameTODO_16 =
    StaticTypeName.typeNameFromConcrete[VectorMatcher.KendallTau.type]
  implicit val typeNameTODO_17 =
    StaticTypeName.typeNameFromConcrete[Seq[JSONAndTypeName]]
  implicit val typeNameTODO_18 =
    StaticTypeName.typeNameFromConcrete[PatchNormalizer.Raw.type]
  implicit val typeNameTODO_19 =
    StaticTypeName.typeNameFromConcrete[PatchNormalizer.NCC.type]
  implicit val typeNameTODO_20 =
    StaticTypeName.typeNameFromConcrete[PatchNormalizer.Rank.type]
  implicit val typeNameTODO_21 =
    StaticTypeName.typeNameFromConcrete[Seq[(Double, Double)]]

  // TODO: Uncomment this function and change the relevant constructors.
  // Currently this is impossible due to a probable Scala bug.
  //  implicit def experimentRunnerInsertRuntime[A <% RuntimeConfig => ExperimentRunner[B], B](
  //    a: A)(
  //      implicit runtimeConfig: RuntimeConfig): ExperimentRunner[B] =
  //    implicitly[A => RuntimeConfig => ExperimentRunner[B]].apply(a).apply(runtimeConfig)

  //  object JsonProtocols extends DetectorJsonProtocol with ExtractorJsonProtocol with PatchNormalizerJsonProtocol with MatcherJsonProtocol with WideBaselineJsonProtocol with BrownJsonProtocol with DMatchJsonProtocol

  //  val nebulaImports = Imports(Set(
  //    "nebula._",
  //    "nebula.smallBaseline._",
  //    "nebula.wideBaseline._",
  //    "nebula.summary._",
  //    "nebula.Distributed._"))
  //
  //  val jsonImports = Imports(Set(
  //    "spray.json._",
  //    "nebula.JsonProtocols._"))
  //
  //  val sparkImports = Imports(Set(
  //    "spark.SparkContext",
  //    "spark.SparkContext._"))
  //
  //  val shapelessImports = Imports(Set(
  //    "shapeless._"))
  //
  //  val reflectImports = Imports(Set(
  //    "reflect.runtime.universe._"))
  //
  //  implicit val allImports = Imports(nebulaImports ++ jsonImports ++ sparkImports ++ shapelessImports ++ reflectImports)
}