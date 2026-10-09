package ir.kharjyar.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ModernCheckListCardGuardTest {
 @Test fun `issued and received checks use modern status aware cards`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/ChecksScreen.kt").readText()
  listOf(
   "private fun ModernCheckListCard(",
   "Brush.linearGradient",
   "RoundedCornerShape(24.dp)",
   "چک دریافت‌شده",
   "چک صادرشده",
   "Money.format(check.amountRial,moneyUnit)",
   "CheckInfoChip(Icons.Filled.EventAvailable,\"سررسید\"",
   "CheckInfoChip(Icons.Filled.AccountBalance,\"بانک\"",
   "شناسه صیادی:",
   "لمس برای نمایش چک صیادی",
   "وصول‌شده",
   "پاس‌شده",
   "برگشت‌خورده"
  ).forEach{assertTrue(it,source.contains(it))}
 }
}
