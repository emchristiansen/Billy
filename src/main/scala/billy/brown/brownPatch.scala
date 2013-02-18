package nebula.brown

import nebula._
import java.awt.image.BufferedImage
import java.io.File

import scala.annotation.elidable
import scala.annotation.elidable.ASSERTION

import org.apache.commons.io.FileUtils

import javax.imageio.ImageIO
import nebula.Extractor
import nebula.Matcher
import nebula.PimpFile
import nebula.util.KeyPointUtil

///////////////////////////////////////////////////////////

case class BrownPatch(image: BufferedImage, id: Int)

case class PatchPair(left: BrownPatch, right: BrownPatch)

object PatchPair {
  implicit class PatchPairOps(self: PatchPair) {
    def corresponds = self.left.id == self.right.id

    def getDistance[E <% Extractor[F], M <% Matcher[F], F](extractor: E, matcher: M): Option[Double] = {
      val PatchPair(left, right) = self
      val leftDescriptor = extractDescriptorAtCenter(extractor, left.image)
      val rightDescriptor = extractDescriptorAtCenter(extractor, right.image)
      for (l <- leftDescriptor; r <- rightDescriptor) yield matcher.distance(l, r)
    }
  }

  def extractDescriptorAtCenter[E <% Extractor[F], F](extractor: E, image: BufferedImage): Option[F] = {
    requirey(image.getWidth == 64)
    requirey(image.getHeight == 64)

    // TODO: Scale down
//    val scaleFactor = 8
//    val scaled = ImageUtil.scale(scaleFactor, image)._2
//    val keyPoint = KeyPointUtil(scaleFactor * 32, scaleFactor * 32)
//    extractor.extractSingle(scaled, keyPoint)
    
    val keyPoint = KeyPointUtil(32, 32)
    extractor.extractSingle(image, keyPoint)
  }

  def loadPatchPairs(datasetName: String, numMatches: Int, dataRoot: File): Stream[PatchPair] = {
    val directory = new File(dataRoot, s"brownImages/${datasetName}").mustExist

    val manifest = new File(directory, s"m50_${numMatches}_${numMatches}_0.txt").mustExist

    val lines = FileUtils.readFileToString(manifest).split("\n")
    asserty(lines.size == numMatches)

    lines.toStream.map(manifestLineToPatchPair(directory))
  }

  def manifestLineToPatchPair(directory: File)(line: String): PatchPair = {
    val (leftIndex, leftID, rightIndex, rightID) = parseManifestLine(line)

    val load = loadPatch(directory) _
    val leftImage = load(leftIndex)
    val rightImage = load(rightIndex)

    PatchPair(BrownPatch(leftImage, leftID), BrownPatch(rightImage, rightID))
  }

  def parseManifestLine(line: String): Tuple4[Int, Int, Int, Int] = {
    val Parser = """(\d+) (\d+) (\d+) (\d+) (\d+) (\d+) (\d+)""".r
    val Parser(leftIndex, leftID, _, rightIndex, rightID, _, _) = line
    return (leftIndex.toInt, leftID.toInt, rightIndex.toInt, rightID.toInt)
  }

  def loadPatch(directory: File)(index: Int): BufferedImage = {
    val numPatchesPerFile = 256

    val imageIndex = index / numPatchesPerFile
    asserty(imageIndex >= 0)
    val imageFile = new File(directory, f"patches${imageIndex}%04d.bmp").mustExist
    val image = ImageIO.read(imageFile)

    val patchIndex = index - numPatchesPerFile * imageIndex
    asserty(patchIndex >= 0)
    asserty(patchIndex < numPatchesPerFile)
    val numPatchesPerRow = 16
    val patchRow = patchIndex / numPatchesPerRow
    asserty(patchRow >= 0)
    asserty(patchRow < numPatchesPerFile / numPatchesPerRow)
    val patchColumn = patchIndex - numPatchesPerRow * patchRow
    asserty(patchColumn >= 0)
    asserty(patchColumn < numPatchesPerRow)

    // Patches are square.
    val patchWidth = 64
    val xBegin = patchWidth * patchColumn
    val yBegin = patchWidth * patchRow
    image.getSubimage(xBegin, yBegin, patchWidth, patchWidth)
  }
}