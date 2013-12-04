package st.sparse.billy

////////////////////////////////////////////////////

/**
 * A simple ADT for encoding image colors.
 */
sealed trait Color

case object Gray extends Color

case object RGB extends Color
