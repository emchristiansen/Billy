package billy

import nebula._
import com.sksamuel.scrimage.Image
import nebula.util._

import billy._
import billy.brown._

import billy.smallBaseline._
import billy.wideBaseline._
import billy.summary._ 

///////////////////////////////////////////////////////////

@deprecated("", "")
trait HasImagePair {
  def leftImage: Image
  def rightImage: Image
}

///////////////////////////////////////////////////////////

@deprecated("", "")
trait HasGroundTruth[A] {
  def groundTruth: A
}

///////////////////////////////////////////////////////////

@deprecated("", "")
trait HasEstimate[A] {
  def estimate: A
}
