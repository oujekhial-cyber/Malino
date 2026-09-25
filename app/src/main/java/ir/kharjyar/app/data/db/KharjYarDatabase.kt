package ir.kharjyar.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ir.kharjyar.app.core.security.DatabaseKey
import net.sqlcipher.database.SupportFactory

@Database(
    entities = [
        AccountEntity::class,
        AccountSenderEntity::class,
        SmsCandidateEntity::class,
        SmsTemplateEntity::class,
        TransactionEntity::class,
        TransferGroupEntity::class,
        CategoryEntity::class,
        CategoryRuleEntity::class,
        BlockedSenderEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class KharjYarDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun smsDao(): SmsDao
    abstract fun templateDao(): TemplateDao
    abstract fun transactionDao(): TransactionDao
    abstract fun transferDao(): TransferDao
    abstract fun categoryDao(): CategoryDao
    abstract fun blockedSenderDao(): BlockedSenderDao

    companion object {
        @Volatile
        private var instance: KharjYarDatabase? = null

        fun get(context: Context): KharjYarDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    KharjYarDatabase::class.java,
                    "kharjyar.db"
                )
                    // دیتابیس روی دیسک با AES-256 رمز می‌شود؛ کلید در Android Keystore است
                    .openHelperFactory(SupportFactory(DatabaseKey.getOrCreate(context)))
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .addCallback(SeedCallback)
                    .build()
                    .also { instance = it }
            }

        /**
         * نسخه ۲: جدول فرستنده‌های تبلیغاتی مسدودشده.
         * مهاجرت واقعی نوشته شده تا داده‌های موجود کاربر از بین نرود.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `blocked_senders` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`sender` TEXT NOT NULL, " +
                        "`createdAt` INTEGER NOT NULL)"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS " +
                        "`index_blocked_senders_sender` ON `blocked_senders` (`sender`)"
                )
            }
        }

        /**
         * نسخه ۳: فیلدهای کارت بانکی روی حساب‌ها.
         * ستون‌ها با مقدار پیش‌فرض خالی اضافه می‌شوند تا داده موجود دست‌نخورده بماند.
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                listOf(
                    "accountNumber", "iban", "cardNumber", "cardExpiry", "cardCvv2"
                ).forEach { col ->
                    db.execSQL(
                        "ALTER TABLE `accounts` ADD COLUMN `$col` TEXT NOT NULL DEFAULT ''"
                    )
                }
            }
        }

        /** دسته‌های اولیه پیش‌فرض. */
        val DEFAULT_CATEGORIES: List<Triple<String, Long, Int>> = listOf(
            Triple("خوراک و سوپرمارکت", 0xFF4CAF50, CategoryKind.EXPENSE),
            Triple("رستوران و کافه", 0xFFFF7043, CategoryKind.EXPENSE),
            Triple("حمل‌ونقل (خودرو)", 0xFF29B6F6, CategoryKind.EXPENSE),
            Triple("تعمیر و نگهداری خودرو", 0xFF26C6DA, CategoryKind.EXPENSE),
            Triple("مسکن و اجاره", 0xFF8D6E63, CategoryKind.EXPENSE),
            Triple("لوازم خانه", 0xFF8D8AC7, CategoryKind.EXPENSE),
            Triple("قبوض", 0xFFFFA726, CategoryKind.EXPENSE),
            Triple("اینترنت و تلفن", 0xFF7E57C2, CategoryKind.EXPENSE),
            Triple("درمان و سلامت", 0xFFEF5350, CategoryKind.EXPENSE),
            Triple("بیمه", 0xFF42A5F5, CategoryKind.EXPENSE),
            Triple("آرایشی و بهداشتی", 0xFFF06292, CategoryKind.EXPENSE),
            Triple("پوشاک", 0xFFEC407A, CategoryKind.EXPENSE),
            Triple("آموزش", 0xFF26A69A, CategoryKind.EXPENSE),
            Triple("تفریح", 0xFFFFCA28, CategoryKind.EXPENSE),
            Triple("سفر و اقامت", 0xFF26A69A, CategoryKind.EXPENSE),
            Triple("اشتراک‌ها", 0xFF7E57C2, CategoryKind.EXPENSE),
            Triple("خرید آنلاین", 0xFF5C6BC0, CategoryKind.EXPENSE),
            Triple("هدیه و کمک", 0xFFAB47BC, CategoryKind.BOTH),
            Triple("کارمزد بانکی", 0xFF78909C, CategoryKind.EXPENSE),
            Triple("حقوق", 0xFF66BB6A, CategoryKind.INCOME),
            Triple("فروش", 0xFF43A047, CategoryKind.INCOME),
            Triple("سود و سرمایه‌گذاری", 0xFF00897B, CategoryKind.INCOME),
            Triple("بازگشت وجه", 0xFF7CB342, CategoryKind.INCOME),
            Triple("سایر درآمدها", 0xFF9CCC65, CategoryKind.INCOME),
            Triple("سایر هزینه‌ها", 0xFFBDBDBD, CategoryKind.EXPENSE)
        )

        private object SeedCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                for ((name, color, kind) in DEFAULT_CATEGORIES) {
                    db.execSQL(
                        "INSERT INTO categories (name, colorArgb, kind, archived, builtin) VALUES (?, ?, ?, 0, 1)",
                        arrayOf(name, color or 0xFF000000, kind)
                    )
                }
            }
        }
    }
}
