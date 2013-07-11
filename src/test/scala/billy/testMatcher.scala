package billy

import nebula._
import nebula.imageProcessing._
import nebula.util._

import billy._
import billy.brown._

import billy.smallBaseline._
import billy.wideBaseline._
import billy.summary._

import org.scalacheck.Prop.{forAll, propBoolean}
import org.scalacheck.Properties
import org.scalatest.FunSuite
import nebula.util.Util
import billy.Matcher._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import scalatestextra._

///////////////////////////////////////////////////////////

@RunWith(classOf[JUnitRunner])
class TestMatcher extends FunSuite {
  val sort0 = SortDescriptor(IndexedSeq(0, 1, 2, 3))
  val sort1 = SortDescriptor(IndexedSeq(2, 1, 3, 0))
  val sort2 = SortDescriptor(IndexedSeq(3, 2, 1, 0))

  test("l0", InstantTest) {
    assert(Matcher.l0(sort0, sort0) == 0)
    assert(Matcher.l0(sort0, sort1) == 3)
  }

  test("l1", InstantTest) {
    assert(Matcher.l1(sort0, sort0) == 0)
    assert(Matcher.l1(sort0, sort1) == 6)
  }

  test("kendallTau", InstantTest) {
    assert(Matcher.kendallTau(sort0, sort0) == 0)
    assert(Matcher.kendallTau(sort0, sort1) == 4)
    assert(Matcher.kendallTau(sort1, sort0) == 4)
    assert(Matcher.kendallTau(sort1, sort1) == 0)
    assert(Matcher.kendallTau(sort0, sort2) == 6)
  }
  
  test("countSort", InstantTest) {
    val input = List(1, 0, 2, 3, 4, 2, 3, 4)
    val countSorted = Util.countSort(input, 0, 4)
    assert(input.sorted == countSorted)
  }
  
  test("permutation", InstantTest) {
    val input = Array(1, 0, 2, 3, 4, 2, 3, 4)
    val permutation = Util.permutation(input, 4)
    assert(permutation === Array(1, 0, 2, 5, 3, 6, 4, 7))
  }

  test("numCycles", InstantTest) {
    assert(sort0.numCycles == 4)
    assert(sort1.numCycles == 2)
    assert(sort2.numCycles == 2)
  }

//  test("robustCayley") {
//    assert(robustCayley(
//      IndexedSeq(1, 2, 3),
//      IndexedSeq(1, 2, 3)) == 0)
//
//    assert(robustCayley(
//      IndexedSeq(1, 1, 2),
//      IndexedSeq(2, 1, 1)) == 1)
//  }
//
//  test("generalizedL0") {
//    assert(generalizedL0(
//      IndexedSeq(0, -16),
//      IndexedSeq(0, 1)) === 2)
//
//    assert(generalizedL0(
//      IndexedSeq(-1, 589828345, -1),
//      IndexedSeq(0, -1, 0)) === 2)
//
//    assert(generalizedL0(
//      IndexedSeq(10, 1, -10, 1),
//      IndexedSeq(0, -10, 0, -10)) === 2)
//  }
//
//  test("intervalRanking") {
//    assert(
//      intervalRanking(
//        IndexedSeq(2, 2, 3, 3, 3)).values ===
//        IndexedSeq((0, 1), (0, 1), (2, 4), (2, 4), (2, 4)))
//
//    assert(
//      intervalRanking(
//        IndexedSeq(4, 4, 4)).values ===
//        IndexedSeq((0, 2), (0, 2), (0, 2)))
//
//    assert(
//      intervalRanking(
//        IndexedSeq(4, 4, 2, 4, 5)).values ===
//        IndexedSeq((1, 3), (1, 3), (0, 0), (1, 3), (4, 4)))
//  }
//
//  test("l1IntervalDistance") {
//    assert(
//      l1IntervalDistance(
//        IndexedSeq(2, 2, 3, 3, 3),
//        IndexedSeq(4, 4, 2, 4, 5)) == 0 + 0 + 2 + 0 + 0)
//
//    assert(
//      l1IntervalDistance(
//        IndexedSeq(1, 2, 3, 4, 4),
//        IndexedSeq(5, 4, 3, 2, 1)) == 4 + 2 + 0 + 2 + 3)
//  }
}

object CheckMatcher extends Properties("Matcher") {
//  property("generalized l0 returns minimum legal distance") = forAll {
//    (leftLong: List[Int], rightLong: List[Int]) =>
//      {
//        val (left, right) = leftLong.take(6).zip(rightLong).toIndexedSeq.unzip
//        val minimumL0Distance = generalizedL0(left, right)
//
//        val bruteMinimum =
//          (for (
//            (leftPermutation, rightPermutation) <- Util.nonDistinctPermutations(left).zip(Util.nonDistinctPermutations(right))
//          ) yield {
//            l0(
//              SortDescriptor.fromUnsorted(leftPermutation),
//              SortDescriptor.fromUnsorted(rightPermutation))
//          }).min
//
//        minimumL0Distance == bruteMinimum
//      }
//  }
}
