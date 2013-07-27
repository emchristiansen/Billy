package billy

import java.io.File

import org.apache.commons.io.FileUtils

import nebula._
import nebula.imageProcessing._
import nebula.util._

import billy._
import billy.brown._

import billy.smallBaseline._
import billy.wideBaseline._
import billy.summary._

///////////////////////////////////////////////////////////

trait ExperimentRunner[R] {
  def run: R
}





