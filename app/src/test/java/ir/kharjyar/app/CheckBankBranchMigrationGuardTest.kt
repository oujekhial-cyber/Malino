package ir.kharjyar.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class CheckBankBranchMigrationGuardTest {
 @Test fun `check branch is persisted migrated entered separately and rendered under bank`() {
  val entity=File("src/main/java/ir/kharjyar/app/data/db/ObligationEntities.kt").readText()
  val db=File("src/main/java/ir/kharjyar/app/data/db/KharjYarDatabase.kt").readText()
  val screen=File("src/main/java/ir/kharjyar/app/ui/screens/ChecksScreen.kt").readText()
  assertTrue(entity.contains("val bankBranch: String = \"\""))
  assertTrue(db.contains("version = 24"))
  assertTrue(db.contains("MIGRATION_23_24"))
  assertTrue(db.contains("ALTER TABLE checks ADD COLUMN bankBranch TEXT NOT NULL DEFAULT ''"))
  assertTrue(screen.contains("bankBranch=bankBranch.trim()"))
  assertTrue(screen.contains("نام یا کد شعبه"))
  assertTrue(screen.contains("check.bankBranch.ifBlank{\"شعبه\"}"))
  assertTrue(screen.contains("check.chequeAccountNumber"))
  assertTrue(screen.contains("check.chequeIban"))
  assertTrue(screen.contains("Modifier.absoluteOffset(x(.06f),y(.815f))"))
  assertTrue(screen.contains("Modifier.absoluteOffset(x(.06f),y(.895f))"))
 }
}
