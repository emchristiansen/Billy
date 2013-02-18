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
import org.scalatest._
import javax.imageio.ImageIO
import java.io.File
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import nebula.util._
import billy.JsonProtocols._
import spray.json._
import com.sun.xml.internal.bind.v2.model.runtime.RuntimeClassInfo
import breeze.linalg._
import breeze.math._
import grizzled.math.stats
import org.scalacheck._
import org.scalatest.prop._
import org.scalatest._
import DenseMatrixUtil._
import org.opencv.features2d.{ DMatch, KeyPoint }
import java.util.Date

///////////////////////////////////////////////////////////

@RunWith(classOf[JUnitRunner])
@WrapWith(classOf[ConfigMapWrapperSuite])
class MeasureNCCLogPolarMatcher(
  val configMap: Map[String, Any]) extends ConfigMapFunSuite with GeneratorDrivenPropertyChecks with ShouldMatchers {
  val image = ImageIO.read(new File(
    getClass.getResource("/iSpy.png").getFile).mustExist)

  val random = new scala.util.Random(0)

  def randomPoint(width: Int, height: Int, buffer: Int): KeyPoint = {
    val x = random.nextFloat * (width - 2 * buffer) + buffer
    val y = random.nextFloat * (height - 2 * buffer) + buffer
    //    KeyPointUtil(x, y)
    KeyPointUtil(x.floor + 0.5.toFloat, y.floor + 0.5.toFloat)
  }

  val genRandomPoint = Gen(_ => Some(randomPoint(
    image.getWidth,
    image.getHeight,
    100)))

  val genRandomPointPair = Gen(_ => Some((
    genRandomPoint.sample.get,
    genRandomPoint.sample.get)))

  test("average match time", MediumTest) {
    val minSuccessful = 10
    implicit val generatorDrivenConfig =
      PropertyCheckConfig(minSuccessful = minSuccessful)

    var time: Long = 0
    forAll(genRandomPointPair) {
      case (left, right) =>
        val extractor = NCCLogPolarExtractor(LogPolarExtractor(
          false,
          2,
          32,
          16,
          32,
          3,
          "Gray"))

        val matcher = NCCLogPolarMatcher(
          true,
          8)

        def distance[E <% Extractor[F], M <% Matcher[F], F](
          extractor: E,
          matcher: M): Double = {
          val leftDescriptor = extractor.extractSingle(image, left).get
          val rightDescriptor = extractor.extractSingle(image, right).get

          val before = System.currentTimeMillis
          val d = matcher.distance(leftDescriptor, rightDescriptor)
          val after = System.currentTimeMillis
          time += after - before
          d
        }

        val nccDistance = distance(extractor, matcher)
        expecty(nccDistance >= 0)
    }

    println(s"Measure NCCLogPolar matcher: ${time.toDouble / minSuccessful}ms")
  }
}