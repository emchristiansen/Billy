package st.sparse.billy.experiments.wideBaseline

import st.sparse.billy.experiments.RuntimeConfig

trait Experiment {
  def run(implicit runtimeConfig: RuntimeConfig): Results
}