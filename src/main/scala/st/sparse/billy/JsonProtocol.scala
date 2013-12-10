package st.sparse.billy

import spray.json._
import spray.json.DefaultJsonProtocol
import breeze.linalg._
import scala.reflect.ClassTag
import org.joda.time.DateTime
import com.sksamuel.scrimage.Image

trait JsonProtocol extends DefaultJsonProtocol {
  def jsonFormat0[A](singleton: A): RootJsonFormat[A] = new RootJsonFormat[A] {
    override def write(a: A) = {
      assert(a == singleton)
      JsArray(JsString(a.toString))
    }

    override def read(json: JsValue) = json match {
      case JsArray(JsString(name) :: Nil) if (name == singleton.toString) =>
        singleton
      case _ => deserializationError(s"$singleton expected, got $json.")
    }
  }

  implicit def colorFormat = new RootJsonFormat[Color] {
    override def write(color: Color) = {
      JsString(color.toString)
    }

    override def read(json: JsValue) = json match {
      case JsString(colorString) if (colorString == Gray.toString) => Gray
      case JsString(colorString) if (colorString == RGB.toString) => RGB
      case _ => deserializationError(s"Color expected, got $json.")
    }
  }

  private case class DenseMatrixData[A](rows: Int, data: List[A])
  private implicit def denseMatrixDataFormat[A: JsonFormat]: JsonFormat[DenseMatrixData[A]] =
    jsonFormat2(DenseMatrixData.apply[A])

  implicit def denseMatrixFormat[A: JsonFormat: ClassTag] =
    new RootJsonFormat[DenseMatrix[A]] {
      override def write(matrix: DenseMatrix[A]) =
        DenseMatrixData(matrix.rows, matrix.data.toList).toJson

      override def read(json: JsValue) = {
        val data = json.convertTo[DenseMatrixData[A]]
        new DenseMatrix(data.rows, data.data.toArray)
      }
    }

  implicit def dateTimeFormat = new RootJsonFormat[DateTime] {
    override def write(dateTime: DateTime) = dateTime.toString().toJson
    override def read(json: JsValue) = new DateTime(json.convertTo[String])
  }

  private case class ImageData(
    width: Int,
    height: Int,
    `type`: Int,
    pixels: Array[Int])
  private implicit def imageDataFormat: JsonFormat[ImageData] =
    jsonFormat4(ImageData)

  implicit def imageFormat = new RootJsonFormat[Image] {
    override def write(image: Image) = ImageData(
      image.width,
      image.height,
      image.awt.getType,
      image.pixels).toJson
    override def read(json: JsValue) = {
      val ImageData(width, height, _, pixels) = json.convertTo[ImageData]
      Image(width, height, pixels)
    }
  }
}