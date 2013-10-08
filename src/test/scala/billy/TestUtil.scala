package billy

import java.io.File

import st.sparse.sundry._

import com.sksamuel.scrimage._

object TestUtil {
  val resourceRoot = ExistingDirectory(
    new File(getClass.getResource("/").getPath))
    
  val goldfishGirl = Image(ExistingFile(new File(
    billy.TestUtil.resourceRoot,
    "/goldfish_girl.png")))
}