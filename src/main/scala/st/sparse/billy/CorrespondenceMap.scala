package st.sparse.billy

import org.opencv.core.KeyPoint

trait CorrespondenceMap {
  def transformXYOnly(in: KeyPoint): KeyPoint
}