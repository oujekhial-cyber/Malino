package ir.kharjyar.app
import java.io.File
import org.junit.Assert.*
import org.junit.Test
class VehiclesAndOilGuardTest {
 @Test fun `vehicles screen separates fines and oil reminders per car`(){val s=File("src/main/java/ir/kharjyar/app/ui/screens/VehiclesScreen.kt").readText();assertTrue(s.contains("جریمه جدید"));assertTrue(s.contains("تعویض روغن"));assertTrue(s.contains("vehicleId=selected.id"));assertTrue(s.contains("LifeReminderWorker.schedule"))}
 @Test fun `vehicle services migrate and drawer exposes destination`(){val db=File("src/main/java/ir/kharjyar/app/data/db/KharjYarDatabase.kt").readText();val root=File("src/main/java/ir/kharjyar/app/ui/AppRoot.kt").readText();assertTrue(db.contains("version = 11")&&db.contains("MIGRATION_10_11")&&db.contains("VehicleOilServiceEntity::class"));assertTrue(root.contains("وسایل نقلیه")&&root.contains("VehiclesScreen"))}
}
