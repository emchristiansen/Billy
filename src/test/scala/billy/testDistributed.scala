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

import billy.detectors._
import billy.extractors._
import billy.matchers._

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

///////////////////////////////////////////////////////////

@RunWith(classOf[JUnitRunner])
class TestDistributed extends FunSuite {
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