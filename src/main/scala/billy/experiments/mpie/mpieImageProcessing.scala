package billy.experiments.mpie;
//package billy.mpie
//
//import nebula._
//import nebula.imageProcessing._
//import nebula.util._
//
//import billy._
//import billy.brown._
//
//import billy.smallBaseline._
//import billy.wideBaseline._
//import billy.summary._
//
//import java.awt.geom.AffineTransform
//import java.awt.image.AffineTransformOp
//import java.awt.image.AffineTransformOp.TYPE_BILINEAR
//import java.awt.image.ConvolveOp
//import java.awt.image.Kernel
//import java.io.File
//
//import scala.Array.canBuildFrom
//import scala.annotation.elidable
//import scala.annotation.elidable.ASSERTION
//
//import javax.imageio.ImageIO
//import nebula.imageProcessing.Pixel
//import nebula.util.Geometry
//import nebula.util.MathUtil
//import java.awt.image.BufferedImage
//
//class LazyImage(
//  originalPath: String,
//  condition: MPIECondition,
//  roiString: String)(
//    implicit runtime: MPIERuntimeConfig) {
//  private val mpieProperties = MPIEProperties.parseMPIEPath(originalPath)
//
//  val id = mpieProperties.id
//  // A larger value reduces the probability an ROI will try to access an
//  // out-of-bounds pixel, but increases computation time.
//  val padding = 200
//
//  lazy val path = {
//    val image = Image.read(ExistingFile(originalPath))
//
//    def warp(image: Image): Image = {
//      // first get the file with the alignment matrix in it
//      val pathSegment = mpieProperties.pathSegment
//      val filename =
//        s"${mpieProperties.id}_${mpieProperties.session}_" +
//          s"${mpieProperties.expression}_${mpieProperties.pose}_mean_align.txt"
//      val path = s"${runtime.piSliceRoot}/processed/${pathSegment}/${filename}"
//
//      val tformParams =
//        io.Source.fromFile(path).mkString.split("\t|\n").map(_.toDouble)
//
//      val transformMatrix = new AffineTransform(
//        tformParams(0), tformParams(3), // column 1
//        tformParams(1), tformParams(4), // column 2
//        tformParams(2), tformParams(5)) // column 3
//
//      val transformOp = new AffineTransformOp(transformMatrix, TYPE_BILINEAR)
//
//      val warped = Image(transformOp.filter(image, null))
//      warped.transparentToGreen
//    }
//
//    def scale(image: Image): Image = {
//      val scaleMatrix = new AffineTransform(
//        runtime.scaleFactor,
//        0,
//        0,
//        runtime.scaleFactor,
//        0,
//        0)
//      val scaleOp = new AffineTransformOp(scaleMatrix, TYPE_BILINEAR)
//      Image(scaleOp.filter(image, null))
//    }
//
//    def illumination(image: Image): Image = {
//      val illum = condition.illumination
//      if (!illum.contains("x")) {
//        image
//      } else {
//        // When doing artificial illumination, we start with neutral.
//        assert(mpieProperties.illumination == "00")
//
//        val factor = illum.init.toDouble
//
//        val raw = image.toRaw
//        for (
//          y <- 0 until raw.getHeight;
//          x <- 0 until raw.getWidth
//        ) {
//          val pixel = Pixel.scale(Pixel.getPixel(raw, x, y), factor)
//          raw.setRGB(x, y, pixel.argb)
//        }
//        Image(raw).fromRaw
//      }
//    }
//
//    def blur(image: Image): Image = {
//      val std = condition.blur.toDouble
//      if (std == 0) {
//        image
//      } else {
//        val kernelData = MathUtil.gaussianKernel(std)
//        val kernel = new Kernel(
//          kernelData.size,
//          kernelData.size,
//          kernelData.flatten.toArray.map(_.toFloat))
//        val op = new ConvolveOp(kernel, ConvolveOp.EDGE_ZERO_FILL, null)
//        Image(op.filter(image, null))
//      }
//    }
//
//    def noise(image: Image): Image = {
//      val std = condition.noise.toDouble
//      if (std != 0) {
//        for (
//          y <- 0 until image.getHeight;
//          x <- 0 until image.getWidth
//        ) {
//          val pixel = Pixel.getPixel(image, x, y)
//          val List(rNoise, gNoise, bNoise) = (0 until 3).toList.map(
//            _ => runtime.random.nextGaussian * std).map(_.toInt)
//          val sum = Pixel.add(pixel, rNoise, gNoise, bNoise)
//          image.setRGB(x, y, sum.argb)
//        }
//      }
//      image
//    }
//
//    def jpeg(image: Image): Image = {
//      val quality = condition.jpeg.toFloat
//      if (quality == 0) {
//        image
//      } else {
//        throw new Exception("not implemented")
//      }
//    }
//
//    def misalignment(image: Image): Image = {
//      val std = condition.misalignment.toDouble
//
//      if (std == 0) {
//        image
//      } else {
//        val fiducials = {
//          val Parser = """.*\((\S+), (\S+)\)""".r
//
//          val pathSegment = mpieProperties.pathSegment
//          val filename =
//            s"${mpieProperties.id}_${mpieProperties.session}_" +
//              s"${mpieProperties.expression}_${mpieProperties.pose}_" +
//              "mean_fiducials.txt"
//          val path =
//            s"${runtime.piSliceRoot}/processed/${pathSegment}/${filename}"
//          val lines =
//            io.Source.fromFile(path).mkString.split("\n").filter(_.size > 0)
//
//          val all = for (l <- lines) yield {
//            val Parser(x, y) = l
//            (padding + x.toDouble * runtime.scaleFactor,
//              padding + y.toDouble * runtime.scaleFactor)
//          }
//
//          // We 4 fiducials for the misalignment for each pose. Note the 
//          // frontal pose has 6 fiducials, so we drop the fiducials at 
//          // the inside corners of the eyes. These are currently at locations 
//          // 1 and 2.
//          // TODO: Stop assuming the fiducials come in a particular order.
//          val culled = if (all.size == 6) {
//            all.take(1) ++ all.drop(3)
//          } else {
//            all
//          }
//          assert(culled.size == 4)
//          culled.toList
//        }
//
//        def perturb(xy: Tuple2[Double, Double]): Tuple2[Double, Double] = {
//          val xNoise = runtime.random.nextGaussian * std
//          val yNoise = runtime.random.nextGaussian * std
//
//          val (x, y) = xy
//          (x + xNoise, y + yNoise)
//        }
//
//        val perturbed = fiducials.map(perturb)
//
//        val tformParams = Geometry.fitSimilarity(fiducials, perturbed).flatten
//
//        val transformMatrix = new AffineTransform(
//          tformParams(0), tformParams(3), // column 1
//          tformParams(1), tformParams(4), // column 2
//          tformParams(2), tformParams(5)) // column 3
//
//        val transformOp = new AffineTransformOp(transformMatrix, TYPE_BILINEAR)
//
//        val transformed = Image(new BufferedImage(
//          image.getWidth,
//          image.getHeight,
//          image.getType))
//        transformOp.filter(image, transformed)
//
//        assert(
//          transformed.getWidth == image.getWidth &&
//            transformed.getHeight == image.getHeight)
//
//        Image(transformed).transparentToGreen
//      }
//    }
//
//    def padImage(image: Image): Image = {
//      val padded = Image(new BufferedImage(
//        2 * padding + image.getWidth,
//        2 * padding + image.getHeight,
//        image.getType))
//      for (
//        y <- 0 until image.getHeight;
//        x <- 0 until image.getWidth
//      ) {
//        padded.setRGB(padding + x, padding + y, image.getRGB(x, y))
//      }
//      Image(padded)
//    }
//
//    def background(unpaddedImage: Image): Image = {
//      val image = padImage(unpaddedImage)
//
//      val mask = {
//        val pathSegment = mpieProperties.pathSegment
//        val filename =
//          s"${mpieProperties.id}_${mpieProperties.session}_" +
//            s"${mpieProperties.expression}_${mpieProperties.pose}_mean_alpha.txt"
//        val path = s"${runtime.piSliceRoot}/processed/${pathSegment}/${filename}"
//        padImage(scale(warp(Image.read(ExistingFile(path)))))
//      }
//
//      lazy val syntheticBackground = {
//        val directory =
//          s"${runtime.backgroundRoot}/session${mpieProperties.session}"
//        val file = {
//          val files = (new File(directory)).listFiles.toList.filter(
//            !_.toString.contains(".DS_Store"))
//          runtime.random.shuffle(files).head
//        }
//        val synthetic = ImageIO.read(file)
//        val x = runtime.random.nextInt(synthetic.getWidth - image.getWidth)
//        val y = runtime.random.nextInt(synthetic.getHeight - image.getHeight)
//        synthetic.getSubimage(x, y, image.getWidth, image.getHeight)
//      }
//
//      for (
//        h <- 0 until image.getHeight;
//        w <- 0 until image.getWidth
//      ) {
//        val gray = {
//          val Pixel(_, r, g, b) = Pixel.getPixel(mask, w, h)
//          (r + g + b) / 3
//        }
//        if (gray < 127) {
//          condition.background match {
//            case "synthetic" =>
//              image.setRGB(w, h, syntheticBackground.getRGB(w, h))
//            case "blank" => image.setRGB(w, h, Pixel.green)
//            case _ => throw new Exception
//          }
//        }
//      }
//
//      Image(image)
//    }
//
//    def roi(image: Image): Image = {
//      // figure out the path to the mask
//      val poseUnderscore = condition.pose match {
//        case "240" => "24_0"
//        case "190" => "19_0"
//        case "051" => "05_1"
//      }
//
//      val roiPath =
//        runtime.piSliceRoot ++
//          "/processed/roi/" ++
//          poseUnderscore ++
//          "/" ++
//          roiString ++
//          ".png"
//      val roiImg = padImage(scale(Image.read(ExistingFile(roiPath))))
//
//      Image(image.extractROI(Image(roiImg)))
//    }
//
//    val scaled = scale(warp(image))
//    // TODO: The order of operations matters, and it might be more 
//    // correct if background is split into two phases, so that 
//    // blur / noise / etc applies to synthetic backgrounds but not greenscreens.
//    val conditioned = 
//      roi(misalignment(background(jpeg(noise(blur(illumination(scaled)))))))
//
//    val tempPrefix = "%s_%s_%s".format(id, roiString, condition.toString)
//    val path: File = sys.error("fix me") //IO.createTempFile(tempPrefix, ".png")
//
//    if (runtime.deleteTemporaryFiles) path.deleteOnExit
//
//    ImageIO.write(conditioned, "png", path)
//    path.toString
//  }
//}
