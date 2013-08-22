package billy.extractors






/////////////////////////////////////////////////////////////
// TODO
//case class NormalizedExtractor[E, N, F1, F2](
//  extractor: E,
//  normalizer: N)(
//    implicit evExtractor: E => Extractor[F1],
//    evNormalizer: N => Normalizer[F1, F2])
//
//object NormalizedExtractor {
//  implicit def toExtractor[E, N, F1, F2](normalizedExtractor: NormalizedExtractor[E, N, F1, F2])(
//    implicit evExtractor: E => Extractor[F1],
//    evNormalizer: N => Normalizer[F1, F2]): Extractor[F2] = Extractor.fromAction(
//    (image: Image, keyPoints: Seq[KeyPoint]) => {
//      val unnormalized = normalizedExtractor.extractor.extract(image, keyPoints)
//      unnormalized.map(_.map(normalizedExtractor.normalizer.normalize))
//    })
//}
