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

///////////////////////////////////////////////////////////

/**
 * Returns the expected distance for the random output of a Normalizer
 * under a given Matcher, as a function of the size of the Normalizer
 * output.
 *
 * This is used by LogPolarMatcher to properly compare distances between
 * descriptor pairs with different overlap sizes.
 */
trait ExpectedDistance {
  def expectedDistance: Int => Double
}

object ExpectedDistance {
  implicit class NCCAndL12ExpectedDistance(self: (PatchNormalizer.NCC.type, Matcher.L1.type)) extends ExpectedDistance {
    // Fitted using ZunZun.com (Simple Equation 32 With Offset)
    override def expectedDistance = size => {
      val a = 7.4442627202579173E-02
      val b = 1.1575863971718447E+00
      val c = 4.9973591676413598E-01
      val Offset = -2.2590415747638554E-02
      a / size + b * math.pow(size, c) + Offset
    }
  }

  implicit class NCCAndL22ExpectedDistance(self: (PatchNormalizer.NCC.type, Matcher.L2.type)) extends ExpectedDistance {
    override def expectedDistance = size => math.sqrt(2)
  }

  implicit class RankAndL12ExpectedDistance(self: (PatchNormalizer.Rank.type, Matcher.L1.type)) extends ExpectedDistance {
    // Fitted using ZunZun.com (Simple Equation 14)
    override def expectedDistance = size => {
      val a = 3.3371413204051159E-01
      val b = 1.9998356034619147E+00
      val c = 8.2160416696899854E-09
      a * math.pow(size, b + c * size)
    }
  }

  implicit class RankAndL22ExpectedDistance(self: (PatchNormalizer.Rank.type, Matcher.L2.type)) extends ExpectedDistance {
    // Fitted using ZunZun.com (Simple Equation 27 With Offset)
    override def expectedDistance = size => {
      val a = 6.3765625443234372E-02
      val b = 5.5006528174937408E-01
      val c = 1.5000760960937245E+00
      val Offset = -8.8073858576547226E-01
      math.pow(a + b * size, c) + Offset
    }
  }
}