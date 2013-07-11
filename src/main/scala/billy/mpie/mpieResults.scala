//package billy.mpie
//
//import nebula._
//import nebula.imageProcessing._
//import nebula.util._
//
//import billy._
//import billy.brown._
//import billy.mpie._
//import billy.smallBaseline._
//import billy.wideBaseline._
//import billy.summary._
//
/////////////////////////////////////////////////////////////
//
//case class PredictionAndTruth(prediction: Double, truth: Boolean)
//
/////////////////////////////////////////////////////////////
//
//case class ResultsData(predictionsAndTruths: Seq[PredictionAndTruth])
//
//object ResultsData {
//  def sorted(predictions: Seq[Double], truths: Seq[Boolean]): ResultsData = {
//    val sortedPairs = predictions.zip(truths).sortBy(_._1)
//    val predictionsAndTruths =
//      sortedPairs.map(e => PredictionAndTruth(e._1, e._2))
//    ResultsData(predictionsAndTruths)
//  }
//}
//
////case class MPIEExperimentResults(val experiment: MPIEExperiment,
////                                 val resultsDataList: List[ResultsData])
////
////object MPIEExperimentResults {
////  def fromCompletedExperiment(
////    experiment: MPIEExperiment): MPIEExperimentResults = {
////    assert(experiment.alreadyRun)
////    val Some(file) = experiment.existingResultsFile
////    IO.fromJSONFile[MPIEExperimentResults](file)
////  }
////}
