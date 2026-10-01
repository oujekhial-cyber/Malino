package ir.kharjyar.app
import ir.kharjyar.app.core.sms.CivicSmsClassifier
import ir.kharjyar.app.data.db.CivicMessageKind
import java.io.File
import org.junit.Assert.*
import org.junit.Test
class CivicCenterAndProfileGuardTest {
 @Test fun `classifies civic and legal messages`(){assertEquals(CivicMessageKind.TRAFFIC_FINE,CivicSmsClassifier.classify("RAHVAR","جریمه پلاک ۱۲الف۳۴"));assertEquals(CivicMessageKind.UTILITY_BILL,CivicSmsClassifier.classify("TAVANIR","قبض برق و شناسه پرداخت"));assertEquals(CivicMessageKind.INSURANCE,CivicSmsClassifier.classify("BIMEH","بیمه نامه شما تمدید شد"));assertEquals(CivicMessageKind.ADLIRAN,CivicSmsClassifier.classify("ADLIRAN","ابلاغیه الکترونیک ثنا"))}
 @Test fun `database and drawer expose civic center and future ready profile`(){val db=File("src/main/java/ir/kharjyar/app/data/db/KharjYarDatabase.kt").readText();val root=File("src/main/java/ir/kharjyar/app/ui/AppRoot.kt").readText();assertTrue(db.contains("version = 17")&&db.contains("MIGRATION_8_9")&&db.contains("UserProfileEntity::class"));assertTrue(root.contains("قبوض شهروندی")&&root.contains("ProfileScreen"))}
}
