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
        BlockedSenderEntity::class,
        DebtPersonEntity::class, DebtEntity::class, DebtPaymentEntity::class, CheckEntity::class,
        BankBalanceSnapshotEntity::class
    ],
    version = 5,
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
    abstract fun debtDao(): DebtDao
    abstract fun checkDao(): CheckDao
    abstract fun bankBalanceSnapshotDao(): BankBalanceSnapshotDao

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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
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

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS debt_people (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, phone TEXT NOT NULL, note TEXT NOT NULL, createdAt INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS debts (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, personId INTEGER NOT NULL, kind INTEGER NOT NULL, amountRial INTEGER NOT NULL, title TEXT NOT NULL, createdAt INTEGER NOT NULL, dueAt INTEGER, reminderAt INTEGER, settled INTEGER NOT NULL)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_debts_personId ON debts(personId)"); db.execSQL("CREATE INDEX IF NOT EXISTS index_debts_dueAt ON debts(dueAt)")
                db.execSQL("CREATE TABLE IF NOT EXISTS debt_payments (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, debtId INTEGER NOT NULL, amountRial INTEGER NOT NULL, paidAt INTEGER NOT NULL, note TEXT NOT NULL)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_debt_payments_debtId ON debt_payments(debtId)"); db.execSQL("CREATE INDEX IF NOT EXISTS index_debt_payments_paidAt ON debt_payments(paidAt)")
                db.execSQL("CREATE TABLE IF NOT EXISTS checks (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, direction INTEGER NOT NULL, amountRial INTEGER NOT NULL, counterparty TEXT NOT NULL, bankName TEXT NOT NULL, sayadId TEXT NOT NULL, serialNumber TEXT NOT NULL, accountId INTEGER, issuedAt INTEGER NOT NULL, dueAt INTEGER NOT NULL, reminderAt INTEGER, status INTEGER NOT NULL, imagePath TEXT NOT NULL, note TEXT NOT NULL)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_checks_dueAt ON checks(dueAt)"); db.execSQL("CREATE INDEX IF NOT EXISTS index_checks_accountId ON checks(accountId)"); db.execSQL("CREATE INDEX IF NOT EXISTS index_checks_status ON checks(status)")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS bank_balance_snapshots (accountId INTEGER NOT NULL PRIMARY KEY, balanceRial INTEGER NOT NULL, messageAt INTEGER NOT NULL, smsSystemId INTEGER NOT NULL)")
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
