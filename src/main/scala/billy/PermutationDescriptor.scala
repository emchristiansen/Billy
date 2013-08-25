package billy

////////////////////////////////////////////////

/**
 * A vector like descriptor where the underlying data is guaranteed to
 * be a permutation.
 */
case class PermutationDescriptor(data: IndexedSeq[Int]) {
  assert(data.sorted == (0 until data.size))
}