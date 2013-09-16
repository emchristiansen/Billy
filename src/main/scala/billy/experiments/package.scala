import java.util.Date

package object experiments {
  type Timestamped[A] = (Date, A)
}