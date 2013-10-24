package st.sparse.billy

import shapeless._
import shapeless.ops.hlist._
import shapeless.poly._

/**
 * Functions for HList magic.
 */
object HListUtil {
  // Much of the implementation is a copy-paste of:
  // https://github.com/milessabin/shapeless/blob/master/examples/src/main/scala/shapeless/examples/cartesianproduct.scala

  /**
   * A type class that helps us partially apply a polymorphic binary function
   * to some value and map the resulting function (which of course isn't
   * literally a Poly1) over an HList.
   */
  trait ApplyMapper[HF, A, X <: HList, Out <: HList] {
    def apply(a: A, x: X): Out
  }

  object ApplyMapper {
    implicit def hnil[HF, A] = new ApplyMapper[HF, A, HNil, HNil] {
      def apply(a: A, x: HNil) = HNil
    }

    implicit def hlist[HF, A, XH, XT <: HList, OutH, OutT <: HList](
      implicit applied: Case2.Aux[HF, A, XH, OutH],
      mapper: ApplyMapper[HF, A, XT, OutT]) =
      new ApplyMapper[HF, A, XH :: XT, OutH :: OutT] {
        def apply(a: A, x: XH :: XT) = applied(a, x.head) :: mapper(a, x.tail)
      }
  }

  /**
   * A type class that lets us "lift" a polymorphic binary function so that it
   * operates on HLists, in the manner of Haskell's Control.Applicative.liftA2.
   */
  trait LiftA2[HF, X <: HList, Y <: HList, Out <: HList] {
    def apply(x: X, y: Y): Out
  }

  object LiftA2 {
    implicit def hnil[HF, Y <: HList] = new LiftA2[HF, HNil, Y, HNil] {
      def apply(x: HNil, y: Y) = HNil
    }

    implicit def hlist[HF, XH, XT <: HList, Y <: HList, Out1 <: HList, Out2 <: HList](
      implicit mapper: ApplyMapper[HF, XH, Y, Out1],
      lift: LiftA2[HF, XT, Y, Out2],
      prepend: Prepend[Out1, Out2]) = new LiftA2[HF, XH :: XT, Y, prepend.Out] {
      def apply(x: XH :: XT, y: Y) = prepend(mapper(x.head, y), lift(x.tail, y))
    }
  }

  /**
   * A method that pulls together evidence that some higher rank function can be
   * lifted to work on two HLists.
   */
  def liftA2[HF, X <: HList, Y <: HList, Out <: HList](hf: HF)(x: X, y: Y)(
    implicit lift: LiftA2[HF, X, Y, Out]) = lift(x, y)

  /**
   * A polymorphic binary function that pairs its arguments.
   */
  object tuple2 extends Poly {
    implicit def whatever[A, B] = use((a: A, b: B) => (a, b))
  }

  ///////////////////////////////////////////////////////////

  /**
   * The Cartesian product of two HLists.
   */
  def cartesian2[A <: HList, B <: HList, Out <: HList](a: A, b: B)(
    implicit lift: LiftA2[tuple2.type, A, B, Out]) = liftA2(tuple2)(a, b)

  ///////////////////////////////////////////////////////////

  object Flatten3 extends Poly1 {
    implicit def default[A, B, C] = at[((A, B), C)] {
      case ((a, b), c) => (a, b, c)
    }
  }

  /**
   * The Cartesian product of three HLists.
   */
  def cartesian3[A <: HList, B <: HList, C <: HList, Out1 <: HList, Out2 <: HList](
    a: A,
    b: B,
    c: C)(
      implicit lift1: LiftA2[tuple2.type, A, B, Out1],
      lift2: LiftA2[tuple2.type, Out1, C, Out2],
      mapper: Mapper[Flatten3.type, Out2]) = {
    val ab = cartesian2(a, b)
    val tuples = cartesian2(ab, c)

    tuples map Flatten3
  }

  ///////////////////////////////////////////////////////////

  object Flatten4 extends Poly1 {
    implicit def default[A, B, C, D] = at[(((A, B), C), D)] {
      case (((a, b), c), d) => (a, b, c, d)
    }
  }

  /**
   * The Cartesian product of four HLists.
   */
  def cartesian4[A <: HList, B <: HList, C <: HList, D <: HList, Out1 <: HList, Out2 <: HList, Out3 <: HList](
    a: A,
    b: B,
    c: C,
    d: D)(
      implicit lift1: LiftA2[tuple2.type, A, B, Out1],
      lift2: LiftA2[tuple2.type, Out1, C, Out2],
      lift3: LiftA2[tuple2.type, Out2, D, Out3],
      mapper: Mapper[Flatten4.type, Out3]) = {
    val ab = cartesian2(a, b)
    val abc = cartesian2(ab, c)
    val abcd = cartesian2(abc, d)

    abcd map Flatten4
  }

  ///////////////////////////////////////////////////////////

  object Flatten5 extends Poly1 {
    implicit def default[A, B, C, D, E] = at[((((A, B), C), D), E)] {
      case ((((a, b), c), d), e) => (a, b, c, d, e)
    }
  }

  /**
   * The Cartesian product of five HLists.
   */
  def cartesian5[A <: HList, B <: HList, C <: HList, D <: HList, E <: HList, Out1 <: HList, Out2 <: HList, Out3 <: HList, Out4 <: HList](
    a: A,
    b: B,
    c: C,
    d: D,
    e: E)(
      implicit lift1: LiftA2[tuple2.type, A, B, Out1],
      lift2: LiftA2[tuple2.type, Out1, C, Out2],
      lift3: LiftA2[tuple2.type, Out2, D, Out3],
      lift4: LiftA2[tuple2.type, Out3, E, Out4],
      mapper: Mapper[Flatten5.type, Out4]) = {
    val ab = cartesian2(a, b)
    val abc = cartesian2(ab, c)
    val abcd = cartesian2(abc, d)
    val abcde = cartesian2(abcd, e)

    abcde map Flatten5
  }
}

