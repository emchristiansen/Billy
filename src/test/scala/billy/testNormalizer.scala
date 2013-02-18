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

import org.scalatest.FunSuite
import javax.imageio.ImageIO
import java.io.File
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import scala.util.Random
import breeze.linalg.DenseMatrix
import nebula.util._

///////////////////////////////////////////////////////////

@RunWith(classOf[JUnitRunner])
class TestNormalizer extends FunSuite {
  def randomSeq = 100 times { new Random().nextInt }
  def randomMatrix = new DenseMatrix(10, randomSeq.toArray)
  
  implicit class Foo[A <% Any](self: PatchNormalizer.Raw.type) {
    def foo(a: A): A = a
  }  
  
  test("Raw should do nothing on Seq", InstantTest) {
    val in = randomSeq
    // TODO: Explicit cast required because of bug
    // http://stackoverflow.com/questions/14491303/limitations-of-implicit-resolution-or-type-inference
    val out = PatchNormalizer.Raw.to[Normalizer[Seq[Int], Seq[Int]]].normalize(in)
    asserty(in == out)
  }
  
  test("Raw should do nothing on DenseMatrix", InstantTest) {
    val in = randomMatrix
    val out = PatchNormalizer.Raw.to[Normalizer[DenseMatrix[Int], DenseMatrix[Int]]].normalize(in)
    asserty(in == out)
  }
  
  test("NCC should set mean to zero and norm to 1 on Seq", InstantTest) {
    val in = randomSeq
    val out = PatchNormalizer.NCC.to[Normalizer[Seq[Int], Seq[Double]]].normalize(in)
    assertNear(MathUtil.mean(out), 0)
    assertNear(MathUtil.l2Norm(out.toArray), 1)
  }
  
  test("NCC should set mean to zero and norm to 1 on DenseMatrix", InstantTest) {
    val in = randomMatrix
    val out = PatchNormalizer.NCC.to[Normalizer[DenseMatrix[Int], DenseMatrix[Double]]].normalize(in)
    assertNear(MathUtil.mean(out.data), 0)
    assertNear(MathUtil.l2Norm(out.data), 1)
  }
  
  test("Rank should produce a permutation on DenseMatrix", InstantTest) {
    val in = randomMatrix
    val out = PatchNormalizer.Rank.to[Normalizer[DenseMatrix[Int], DenseMatrix[Int]]].normalize(in)
    asserty(out.data.sorted.toIndexedSeq == (0 until in.rows * in.cols))
  }
}