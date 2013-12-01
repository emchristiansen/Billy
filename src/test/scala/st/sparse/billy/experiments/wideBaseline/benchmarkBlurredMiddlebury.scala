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

import org.scalameter.api._

//object BenchmarkBlurredMiddlebury extends PerformanceTest.Microbenchmark {
//  val ranges = for {
//    size <- org.scalameter.api.Gen.range("size")(300000, 1500000, 300000)
//  } yield 0 until size
//
//  measure method "map" in {
//    using(ranges) curve("Range") in {
//      _.map(_ + 1)
//    }
//  }
//}