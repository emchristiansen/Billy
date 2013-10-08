package billy.experiments

import java.io.File
import java.nio.file.Files
import scala.slick.session.Database

import st.sparse.sundry._

object TestUtil {
  def databaseMock: Database = {
    val tempFile = File.createTempFile("TestBilly", "sqlite")
    tempFile.deleteOnExit
    Database.forURL(s"jdbc:sqlite:$tempFile", driver = "org.sqlite.JDBC")
  }

  def runtimeConfigMock = RuntimeConfig(
    ExistingDirectory(new File(billy.TestUtil.resourceRoot, "/data")),
    databaseMock,
    ExistingDirectory(Files.createTempDirectory("TestBillyOutputRoot").toFile),
    None,
    false,
    true)
}