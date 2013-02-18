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

import org.scalacheck.Properties
import org.scalacheck.Prop._
import org.scalacheck._

///////////////////////////////////////////////////////////

object CheckSortDescriptor extends Properties("SortDescriptor") {
  val vowel = Gen.oneOf('A', 'E', 'I', 'O', 'U', 'Y')

  val randomPermutation = for (
    list <- Arbitrary.arbitrary[List[Int]]
  ) yield SortDescriptor.fromUnsorted(list)

  implicit lazy val arbitraryPermutation: Arbitrary[SortDescriptor] = 
    Arbitrary(randomPermutation)

  property("fromOrdered") = forAll {
    s: SortDescriptor => 
      s.values.sorted == (0 until s.values.size)
  }

  property("invert") = forAll {
    sort: SortDescriptor => {
      val inverse = sort.invert
      forAll {
      	n: Int => sort.values.size > 0 ==> {
	  val i = (n % sort.values.size).abs
      	  (inverse.values(sort.values(i)) == i)
	}
      }
    }
  }

  val randomPermutationPair = for (
    left <- Arbitrary.arbitrary[SortDescriptor];
    seed <- Arbitrary.arbitrary[Int]
  ) yield {
    val right = SortDescriptor(new scala.util.Random(seed).shuffle(left.values))
    (left, right)
  }

  implicit lazy val arbitraryPermutationPair: Arbitrary[Tuple2[SortDescriptor, SortDescriptor]] =
    Arbitrary(randomPermutationPair)

  property("compose") = forAll {
    leftAndRight: Tuple2[SortDescriptor, SortDescriptor] => {
      val (left, right) = leftAndRight
      val composition = left.compose(right)
      forAll {
      	n: Int => left.values.size > 0 ==> {
	  val i = (n % left.values.size).abs
      	  (composition.values(i) == left.values(right.values(i)))
	}      
      }
    }
  }

  property("kendallTauBound") = forAll {
    leftAndRight: Tuple2[SortDescriptor, SortDescriptor] => {
      val (left, right) = leftAndRight
      val kendallTau = Matcher.kendallTau(left, right)
      val l1 = Matcher.l1(left, right)
      (kendallTau <= l1 && l1 <= 2 * kendallTau)
    }
  }

//  property("cayleySymmetric") = forAll {
//    leftAndRight: Tuple2[SortDescriptor, SortDescriptor] => {
//      val (left, right) = leftAndRight
//      val cayleyLeft = Matcher.cayley(left, right)
//      val cayleyRight = Matcher.cayley(right, left)
//      (cayleyLeft == cayleyRight)
//    }
//  }
//
//  property("cayleyBound") = forAll {
//    leftAndRight: Tuple2[SortDescriptor, SortDescriptor] => {
//      val (left, right) = leftAndRight
//      val cayley = Matcher.cayley(left, right)
//      val l0 = Matcher.l0(left, right)
//      (cayley <= l0 && l0 <= 2 * cayley)
//    }
//  }
}
