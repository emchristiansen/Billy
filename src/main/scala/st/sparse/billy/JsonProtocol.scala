package st.sparse.billy

import spray.json._
import spray.json.DefaultJsonProtocol
import breeze.linalg._
import scala.reflect.ClassTag

trait JsonProtocol extends DefaultJsonProtocol {
  def jsonFormat0[A](singleton: A): RootJsonFormat[A] = new RootJsonFormat[A] {
    override def write(a: A) = {
      assert(a == singleton)
      JsArray(JsString(a.toString))
    }

    override def read(value: JsValue) = value match {
      case JsArray(JsString(name) :: Nil) if (name == singleton.toString) =>
        singleton
      case _ => deserializationError(s"$singleton expected, got $value.")
    }
  }

  implicit def colorFormat = new RootJsonFormat[Color] {
    override def write(color: Color) = {
      JsString(color.toString)
    }

    override def read(value: JsValue) = value match {
      case JsString(colorString) if (colorString == Gray.toString) => Gray
      case JsString(colorString) if (colorString == RGB.toString) => RGB
      case _ => deserializationError(s"Color expected, got $value.")
    }
  }

  private case class DenseMatrixData[A](rows: Int, data: List[A])
  private implicit def denseMatrixDataFormat[A: JsonFormat] =
    jsonFormat2(DenseMatrixData.apply[A])

  implicit def denseMatrixFormat[A: JsonFormat: ClassTag] =
    new RootJsonFormat[DenseMatrix[A]] {
      override def write(matrix: DenseMatrix[A]) =
        DenseMatrixData(matrix.rows, matrix.data.toList).toJson

      override def read(value: JsValue) = {
        val data = value.convertTo[DenseMatrixData[A]]
        new DenseMatrix(data.rows, data.data.toArray)
      }
    }
}