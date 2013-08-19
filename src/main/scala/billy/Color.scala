package billy

////////////////////////////////////////////////////

/**
 * A simple ADT for encoding image colors.
 */
sealed trait Color

object Gray extends Color

object RGB extends Color