package st.sparse.billy.experiments

import java.io.File
import java.nio.file.Files
import scala.slick.session.Database

import st.sparse.sundry._

trait TestUtil extends st.sparse.billy.TestUtil {
  val database: Database = {
    val tempFile = File.createTempFile("TestBilly", "sqlite")
    tempFile.deleteOnExit
    Database.forURL(s"jdbc:sqlite:$tempFile", driver = "org.sqlite.JDBC")
  }

  implicit val runtimeConfig = RuntimeConfig(
    ExistingDirectory(new File(resourceRoot, "/data")),
    database,
    ExistingDirectory(Files.createTempDirectory("TestBillyOutputRoot").toFile),
    None,
    false,
    true)
}