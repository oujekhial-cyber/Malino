package ir.kharjyar.app

import ir.kharjyar.app.core.nlp.SmartRecordMutationResolver
import ir.kharjyar.app.data.db.*
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class ConfirmedSmartMutationTest {
 @Test fun `resolver selects unique transaction from amount and account`() {
  val accounts=listOf(AccountEntity(id=1,title="روزمره",bankName="ملت",createdAt=1),AccountEntity(id=2,title="پس انداز",bankName="ملی",createdAt=1))
  val txs=listOf(TransactionEntity(id=10,accountId=1,amountRial=5_000_000,direction=TxDirection.DEPOSIT,description="واریز وام",occurredAt=1,recordedAt=1),TransactionEntity(id=11,accountId=2,amountRial=2_000_000,direction=TxDirection.WITHDRAW,description="خرید",occurredAt=2,recordedAt=2))
  val found=SmartRecordMutationResolver.resolve("transaction","واریز وام حساب ملت",5_000_000,null,"ملت","","",txs,accounts,emptyList(),emptyList(),emptyList(),emptyList())
  assertEquals(10,found?.id)
 }
 @Test fun `ambiguous destructive target is rejected`() {
  val assets=listOf(AssetEntity(id=1,kind=AssetKind.GOLD,title="طلا",quantity=18.0,purchasePriceRial=100,currentValueRial=100,purchasedAt=1),AssetEntity(id=2,kind=AssetKind.GOLD,title="طلا",quantity=18.0,purchasePriceRial=100,currentValueRial=100,purchasedAt=1))
  assertNull(SmartRecordMutationResolver.resolve("asset","طلا",100,18.0,"","طلا","",emptyList(),emptyList(),emptyList(),emptyList(),emptyList(),assets))
 }
 @Test fun `AI can only propose and UI owns explicit confirmation`() {
  val policy=File("src/main/java/ir/kharjyar/app/core/nlp/AiCommandPolicy.kt").readText();val ui=File("src/main/java/ir/kharjyar/app/ui/screens/QuickAddScreen.kt").readText()
  assertTrue(policy.contains("\"record_delete\", \"record_edit\""));assertTrue(ui.contains("title={Text(if(deleting)\"تأیید حذف\" else \"تأیید ویرایش\")}"));assertTrue(ui.contains("بله، حذف شود"));assertTrue(ui.contains("SmartRecordMutationResolver.resolve"))
 }
}
