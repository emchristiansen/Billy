package billy.matchers

import billy._

///////////////////////////////////////////////

/**
 * A Matcher which only requires that distance be defined by the user.
 */
trait MatcherSingle[F] extends Matcher[F] {
  override def matchCorresponding = (leftDescriptors, rightDescriptors) => {
    (leftDescriptors, rightDescriptors).zipped map distance
  }
  
  override def matchAll = (leftDescriptors, rightDescriptors) => {
    
  }
}