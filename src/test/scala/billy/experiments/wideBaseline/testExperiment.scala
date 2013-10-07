package billy.experiments.wideBaseline

import billy._
import billy.experiments._
import billy.detectors._
import billy.extractors._
import billy.matchers._

import org.scalatest.fixture
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalacheck.Gen
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.pickling._
import scala.pickling.binary._

import scalatestextra._
import breeze.linalg._
import scala.reflect.ClassTag

////////////////////////////////////////////////////////////////////////////////

@RunWith(classOf[JUnitRunner])
class TestWideBaselineExperiment extends FunGeneratorSuite {
  test("pickling", InstantTest) {
    //    object Foo
    //    
    //    implicit def fooToList(foo: Foo.type): List[Int] = List(1, 2, 3)
    //    
    //    def bar[A, C <% List[A]](x: Int) = 

    //    trait Trait1[A]
    //
    //    implicit def trait1ToList[A](trait1: Trait1[A]): List[A] = ???
    //
    //    trait Trait2[C]
    //
    //    {
    //      implicit def trait2Implicit[A, C <% List[A]]: Trait2[C] = ???
    //      
    //      // Compiles.
    //      implicitly[Trait2[Trait1[Int]]]
    //    }
    //
    //    {
    //      implicit def trait2Pimp[A, C <% List[A]](int: Int): Trait2[C] = ???
    //
    //      // Compiles.
    //      implicitly[Int => Trait2[Trait1[Int]]]
    //      
    //      // Does not compile.
    //      // This is weird, because the implicit conversion is in scope,
    //      // which we know because the previous line compiles.
    //      2: Trait2[Trait1[Int]]
    //    }

    //    implicitly[Trait2[Trait1[Int]]]

    //    implicit def denseMatrixToIndexedSeq[A: ClassTag](
    //      denseMatrix: DenseMatrix[IndexedSeq[A]]): IndexedSeq[A] =
    //      denseMatrix.data.flatten.toIndexedSeq
    //
    //    implicit def l0ToMatcher[C, C2[_], A](
    //      l0: VectorMatcher.L0.type)(
    //          implicit a: C => C2[A],
    //          b: C2[A] => IndexedSeq[A]): Matcher[C] = new MatcherSingle[C] {
    //      override def distance = ???
    //    }
    //
    //    VectorMatcher.L0: Matcher[DenseMatrix[IndexedSeq[Int]]]

    //        VectorMatcher.L0: Matcher[DenseMatrix[IndexedSeq[Int]]]
    //        
    //        VectorMatcher.L0: Matcher[IndexedSeq[Int]]
    //    
    //        (OpenCVDetector.FAST: Detector).pickle.unpickle[Detector]
    //        
    //        (OpenCVExtractor.SIFT: Extractor[IndexedSeq[Double]]).pickle.unpickle[Extractor[IndexedSeq[Double]]]
    //        
    //        (VectorMatcher.L0: Matcher[IndexedSeq[Double]]).pickle.unpickle[Matcher[IndexedSeq[Double]]]

//    val experiment = WideBaselineExperiment(
//      "bikes",
//      4,
//      OpenCVDetector.FAST,
//      OpenCVExtractor.SIFT,
//      VectorMatcher.L0)
//    val pickle = experiment.pickle
//    pickle.unpickle[WideBaselineExperiment[OpenCVDetector.FAST.type, OpenCVExtractor.SIFT.type, VectorMatcher.L0.type, IndexedSeq[Double]]]

    val experiment = Experiment(
      "bikes",
      4,
      OpenCVDetector.FAST,
      OpenCVExtractor.SIFT,
      MyMatcher("hi"))
    val pickle = experiment.pickle
    pickle.unpickle[Experiment[OpenCVDetector.FAST.type, OpenCVExtractor.SIFT.type, MyMatcher, IndexedSeq[Double]]]

    //        pickle.unpickle[WideBaselineExperiment[DenseMatrix[IndexedSeq[Int]]]]
  }

  //  test("acts like a map", InstantTest) {
  //
  //  }
  //
  //  test("a generator driven test", InstantTest) {
  //    val evenInts = for (n <- Gen.choose(-1000, 1000)) yield 2 * n
  //    forAll(evenInts) { x =>
  //      assert(x % 2 == 0)
  //    }
  //  }
}