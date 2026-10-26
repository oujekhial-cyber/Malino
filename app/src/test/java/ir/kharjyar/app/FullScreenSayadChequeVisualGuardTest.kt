package ir.kharjyar.app

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FullScreenSayadChequeVisualGuardTest {
 @Test fun `saved cheque fields overlay exact raw sayad layout without receipt stub`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/ChecksScreen.kt").readText()
  assertTrue(source.contains("R.drawable.sayad_cheque_template"))
  assertTrue(source.contains("\"تصویر خام چک صیادی\""))
  assertTrue(source.contains("CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr)"))
  assertTrue(source.contains("Box(Modifier.matchParentSize())"))
  assertTrue(source.contains("animateDpAsState(if(immersive)10.dp else 16.dp"))
  assertTrue(source.contains("fillMaxWidth(if(immersive) 0.90f else 0.82f)"))
  assertTrue(source.contains("check.bankName.ifBlank{\"نام بانک\"}"))
  assertTrue(source.contains("Modifier.absoluteOffset(x(.40f),y(.135f))"))
  assertTrue(source.contains("check.bankBranch.ifBlank{\"شعبه\"}"))
  assertTrue(source.contains("Modifier.absoluteOffset(x(.43f),y(.205f))"))
  assertTrue(source.contains("label={Text(\"نام بانک\")}"))
  assertTrue(source.contains("label={Text(\"نام یا کد شعبه\")}"))
  assertFalse(source.contains("label={Text(\"بانک و شعبه\")}"))
  assertTrue(source.contains("aspectRatio(2.066f)"))
  assertTrue(source.contains("chequeDate.year.toString().padStart(4,'0')"))
  assertTrue(source.contains("chequeDate.month.toString().padStart(2,'0')"))
  assertTrue(source.contains("chequeDate.day.toString().padStart(2,'0')"))
  assertTrue(source.contains("Modifier.absoluteOffset(x(.651f),y(.126f)).width(chequeWidth*.205f)"))
  assertTrue(source.contains("chequeDateDigits.forEach{digit->"))
  assertTrue(source.contains("Modifier.absoluteOffset(x(.18f),y(.34f))"))
  assertTrue(source.contains("PersianNumberWords.rial(check.amountRial)"))
  assertTrue(source.contains("takeLast(16).padStart(16,' ')"))
  assertTrue(source.contains("sayadDigits.forEach{digit->"))
  assertTrue(source.contains("Modifier.absoluteOffset(x(.160f),y(.235f))"))
  assertTrue(source.contains("takeLast(15).padStart(15,' ')"))
  assertTrue(source.contains("numericAmountDigits.forEach{digit->"))
  assertTrue(source.contains("Modifier.absoluteOffset(x(.054f),y(.665f))"))
  assertTrue(source.contains("check.amountInWords"))
  assertTrue(source.contains("check.chequeAccountNumber"))
  assertTrue(source.contains("check.chequeIban"))
  assertTrue(source.contains("کد/شناسه ملی: \${if(check.nationalId.isBlank())"))
  assertTrue(source.contains("Modifier.absoluteOffset(x(.20f),y(.48f))"))
  assertTrue(source.contains("Modifier.absoluteOffset(x(.55f),y(.465f))"))
  assertTrue(source.contains("MaterialTheme.typography.titleMedium"))
  assertTrue(source.contains("کد/شناسه ملی فردِ درج‌شده در وجه چک"))
  assertTrue(source.contains("check.serialNumber"))
 }
}
