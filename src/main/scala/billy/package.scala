package object billy extends RichKeyPointImplicits with RichImageImplicits with RichMatImplicits with RichDenseMatrixImplicits with RichSeqSeqImplicits {
  lazy val loadOpenCV =
    System.load("/usr/local/share/OpenCV/java/libopencv_java.so")

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
