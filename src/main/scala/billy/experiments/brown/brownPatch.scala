package billy.experiments.brown

import nebula._
import com.sksamuel.scrimage.Image
import nebula.util._

import billy._
import billy.brown._

import billy.smallBaseline._
import billy.wideBaseline._
import billy.summary._

import java.io.File

import scala.annotation.elidable
import scala.annotation.elidable.ASSERTION

import org.apache.commons.io.FileUtils

import javax.imageio.ImageIO
import billy.Extractor
import billy.Matcher
import nebula.PimpFile
import nebula.util.KeyPointUtil

///////////////////////////////////////////////////////////

case class BrownPatch(image: Image, id: Int)

case class PatchPair(left: BrownPatch, right: BrownPatch)

object PatchPair {
  implicit class PatchPairOps(self: PatchPair) {
    def corresponds = self.left.id == self.right.id

    def getDistance[E <% Extractor[F], M <% Matcher[F], F](
      extractor: E,
      matcher: M): Option[Double] = {
      val PatchPair(left, right) = self
      val leftDescriptor = extractDescriptorAtCenter(extractor, left.image)
      val rightDescriptor = extractDescriptorAtCenter(extractor, right.image)
      for (
        l <- leftDescriptor;
        r <- rightDescriptor
      ) yield matcher.distance(l, r)
    }
  }

  def extractDescriptorAtCenter[E <% Extractor[F], F](
    extractor: E,
    image: Image): Option[F] = {
    require(image.width == 64)
    require(image.height == 64)

    // TODO: Scale down
    //    val scaleFactor = 8
    //    val scaled = ImageUtil.scale(scaleFactor, image)._2
    //    val keyPoint = KeyPointUtil(scaleFactor * 32, scaleFactor * 32)
    //    extractor.extractSingle(scaled, keyPoint)

    val keyPoint = KeyPointUtil(32, 32)
    extractor.extractSingle(image, keyPoint)
  }

  def loadPatchPairs(
    datasetName: String,
    numMatches: Int,
    dataRoot: File): Stream[PatchPair] = {
    val directory = ExistingDirectory(new File(
      dataRoot,
      s"brownImages/${datasetName}"))

    val manifest = ExistingFile(new File(
      directory,
      s"m50_${numMatches}_${numMatches}_0.txt"))

    val lines = FileUtils.readFileToString(manifest).split("\n")
    assert(lines.size == numMatches)

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

  def loadPatch(directory: File)(index: Int): Image = {
    val numPatchesPerFile = 256

    val imageIndex = index / numPatchesPerFile
    assert(imageIndex >= 0)
    val imageFile = new File(directory, f"patches${imageIndex}%04d.bmp").mustExist
    val image = ImageIO.read(imageFile)

    val patchIndex = index - numPatchesPerFile * imageIndex
    assert(patchIndex >= 0)
    assert(patchIndex < numPatchesPerFile)
    val numPatchesPerRow = 16
    val patchRow = patchIndex / numPatchesPerRow
    assert(patchRow >= 0)
    assert(patchRow < numPatchesPerFile / numPatchesPerRow)
    val patchColumn = patchIndex - numPatchesPerRow * patchRow
    assert(patchColumn >= 0)
    assert(patchColumn < numPatchesPerRow)

    // Patches are square.
    val patchWidth = 64
    val xBegin = patchWidth * patchColumn
    val yBegin = patchWidth * patchRow
    Image(image.getSubimage(xBegin, yBegin, patchWidth, patchWidth))
  }
}
