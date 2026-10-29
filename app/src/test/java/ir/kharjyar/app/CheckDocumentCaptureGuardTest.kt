package ir.kharjyar.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class CheckDocumentCaptureGuardTest {
 @Test fun `each check accepts and displays original document from camera or gallery`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/ChecksScreen.kt").readText()
  listOf(
   "check-document-${'$'}{check.id}",
   "ActivityResultContracts.TakePicture()",
   "ActivityResultContracts.GetContent()",
   "permission.launch(Manifest.permission.CAMERA)",
   "gallery.launch(\"image/*\")",
   "check.copy(imagePath=file.absolutePath)",
   "vm.repo.db.checkDao().update(updated)",
   "Text(\"عکس با دوربین\")",
   "Text(\"انتخاب از گالری\")",
   "Text(\"عکس اصلی\")",
   "BitmapFactory.decodeFile(check.imagePath)",
   "contentScale=ContentScale.Fit"
  ).forEach{assertTrue(it,source.contains(it))}
 }
}
