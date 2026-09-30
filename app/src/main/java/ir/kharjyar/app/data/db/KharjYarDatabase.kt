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
        BankBalanceSnapshotEntity::class, NoteEntity::class, LoanEntity::class, LoanInstallmentEntity::class, AssetEntity::class, AssetTradeEntity::class, StockSmsDraftEntity::class, TransactionAttachmentEntity::class, ReminderEntity::class, UserProfileEntity::class, CoveredPersonEntity::class, VehicleEntity::class, VehicleOilServiceEntity::class, CivicMessageEntity::class
    ],
    version = 16,
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
    abstract fun noteDao(): NoteDao
    abstract fun loanDao(): LoanDao
    abstract fun assetDao(): AssetDao
    abstract fun stockDao(): StockDao
    abstract fun transactionAttachmentDao(): TransactionAttachmentDao
    abstract fun reminderDao(): ReminderDao
    abstract fun civicDao(): CivicDao

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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16)
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

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS notes (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, body TEXT NOT NULL, pinned INTEGER NOT NULL, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_notes_updatedAt ON notes(updatedAt)")
                db.execSQL("CREATE TABLE IF NOT EXISTS loans (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, party TEXT NOT NULL, kind INTEGER NOT NULL, principalRial INTEGER NOT NULL, installmentAmountRial INTEGER NOT NULL, installmentCount INTEGER NOT NULL, startAt INTEGER NOT NULL, nextDueAt INTEGER NOT NULL, reminderDaysBefore INTEGER NOT NULL, note TEXT NOT NULL, closed INTEGER NOT NULL)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_loans_nextDueAt ON loans(nextDueAt)")
                db.execSQL("CREATE TABLE IF NOT EXISTS loan_installments (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, loanId INTEGER NOT NULL, number INTEGER NOT NULL, amountRial INTEGER NOT NULL, dueAt INTEGER NOT NULL, paid INTEGER NOT NULL, paidAt INTEGER)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_loan_installments_loanId ON loan_installments(loanId)"); db.execSQL("CREATE INDEX IF NOT EXISTS index_loan_installments_dueAt ON loan_installments(dueAt)"); db.execSQL("CREATE INDEX IF NOT EXISTS index_loan_installments_paid ON loan_installments(paid)")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) { override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE TABLE IF NOT EXISTS assets (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, kind INTEGER NOT NULL, title TEXT NOT NULL, quantity REAL NOT NULL, purchasePriceRial INTEGER NOT NULL, currentValueRial INTEGER NOT NULL, purchasedAt INTEGER NOT NULL, note TEXT NOT NULL, active INTEGER NOT NULL)");db.execSQL("CREATE INDEX IF NOT EXISTS index_assets_kind ON assets(kind)");db.execSQL("CREATE INDEX IF NOT EXISTS index_assets_active ON assets(active)")
            db.execSQL("CREATE TABLE IF NOT EXISTS asset_trades (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, assetId INTEGER NOT NULL, isSale INTEGER NOT NULL, quantity REAL NOT NULL, amountRial INTEGER NOT NULL, tradedAt INTEGER NOT NULL)");db.execSQL("CREATE INDEX IF NOT EXISTS index_asset_trades_assetId ON asset_trades(assetId)");db.execSQL("CREATE INDEX IF NOT EXISTS index_asset_trades_tradedAt ON asset_trades(tradedAt)")
            db.execSQL("CREATE TABLE IF NOT EXISTS transaction_attachments (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, transactionId INTEGER NOT NULL, imagePath TEXT NOT NULL, createdAt INTEGER NOT NULL)");db.execSQL("CREATE INDEX IF NOT EXISTS index_transaction_attachments_transactionId ON transaction_attachments(transactionId)")
        } }

        val MIGRATION_7_8 = object : Migration(7, 8) { override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE TABLE IF NOT EXISTS reminders (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, category TEXT NOT NULL, note TEXT NOT NULL, nextAt INTEGER NOT NULL, repeatType INTEGER NOT NULL, repeatInterval INTEGER NOT NULL, enabled INTEGER NOT NULL, createdAt INTEGER NOT NULL, lastNotifiedAt INTEGER)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_reminders_nextAt ON reminders(nextAt)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_reminders_enabled ON reminders(enabled)")
        } }

        val MIGRATION_8_9 = object : Migration(8, 9) { override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE TABLE IF NOT EXISTS user_profiles (id INTEGER NOT NULL PRIMARY KEY, username TEXT NOT NULL, displayName TEXT NOT NULL, imagePath TEXT NOT NULL, remoteId TEXT, syncPending INTEGER NOT NULL, updatedAt INTEGER NOT NULL)")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_user_profiles_username ON user_profiles(username)")
            db.execSQL("CREATE TABLE IF NOT EXISTS covered_people (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, relation TEXT NOT NULL, nationalId TEXT NOT NULL, insuranceProvider TEXT NOT NULL, policyNumber TEXT NOT NULL, policyExpiresAt INTEGER, note TEXT NOT NULL)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_covered_people_name ON covered_people(name)")
            db.execSQL("CREATE TABLE IF NOT EXISTS vehicles (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, ownerId INTEGER, title TEXT NOT NULL, plate TEXT NOT NULL)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_vehicles_ownerId ON vehicles(ownerId)");db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_vehicles_plate ON vehicles(plate)")
            db.execSQL("CREATE TABLE IF NOT EXISTS civic_messages (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, kind INTEGER NOT NULL, sender TEXT NOT NULL, body TEXT NOT NULL, receivedAt INTEGER NOT NULL, fingerprint TEXT NOT NULL, personId INTEGER, vehicleId INTEGER, read INTEGER NOT NULL)")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_civic_messages_fingerprint ON civic_messages(fingerprint)");db.execSQL("CREATE INDEX IF NOT EXISTS index_civic_messages_kind ON civic_messages(kind)");db.execSQL("CREATE INDEX IF NOT EXISTS index_civic_messages_receivedAt ON civic_messages(receivedAt)");db.execSQL("CREATE INDEX IF NOT EXISTS index_civic_messages_personId ON civic_messages(personId)");db.execSQL("CREATE INDEX IF NOT EXISTS index_civic_messages_vehicleId ON civic_messages(vehicleId)")
        } }

        val MIGRATION_9_10 = object : Migration(9, 10) { override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE checks ADD COLUMN issuerName TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE checks ADD COLUMN receiverName TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE checks ADD COLUMN nationalId TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE checks ADD COLUMN chequeAccountNumber TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE checks ADD COLUMN chequeIban TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE checks ADD COLUMN amountInWords TEXT NOT NULL DEFAULT ''")
        } }

        val MIGRATION_10_11 = object : Migration(10, 11) { override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE TABLE IF NOT EXISTS vehicle_oil_services (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, vehicleId INTEGER NOT NULL, servicedAt INTEGER NOT NULL, currentKm INTEGER NOT NULL, nextKm INTEGER NOT NULL, nextDueAt INTEGER NOT NULL, oilType TEXT NOT NULL, note TEXT NOT NULL, reminderId INTEGER)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_vehicle_oil_services_vehicleId ON vehicle_oil_services(vehicleId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_vehicle_oil_services_nextDueAt ON vehicle_oil_services(nextDueAt)")
        } }

        val MIGRATION_11_12 = object : Migration(11, 12) { override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE vehicles ADD COLUMN vehicleType TEXT NOT NULL DEFAULT 'خودرو سواری'")
            db.execSQL("ALTER TABLE vehicles ADD COLUMN ownerName TEXT NOT NULL DEFAULT ''")
        } }

        val MIGRATION_12_13 = object : Migration(12, 13) { override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE vehicle_oil_services ADD COLUMN serviceType TEXT NOT NULL DEFAULT 'تعویض روغن'")
            db.execSQL("ALTER TABLE vehicle_oil_services ADD COLUMN partsStatus TEXT NOT NULL DEFAULT ''")
        } }

        /** صندوق نقدی همان دفتر حساب عادی است؛ فقط اطلاعات بانکی ندارد و مالک/محل دارد. */
        val MIGRATION_13_14 = object : Migration(13, 14) { override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE accounts ADD COLUMN accountType TEXT NOT NULL DEFAULT 'BANK'")
            db.execSQL("ALTER TABLE accounts ADD COLUMN ownerName TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE accounts ADD COLUMN cashLocation TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE accounts ADD COLUMN note TEXT NOT NULL DEFAULT ''")
            // نام قدیمی دسته برای همه کاربران به نام جدید منتقل می‌شود. اگر کاربر قبلاً
            // نام جدید را ساخته باشد، تراکنش‌ها/قوانین روی همان رکورد ادغام می‌شوند.
            db.execSQL("UPDATE transactions SET categoryId=(SELECT MIN(id) FROM categories WHERE name='مواد غذایی و سوپرمارکت') WHERE categoryId IN (SELECT id FROM categories WHERE name='خوراک و سوپرمارکت') AND EXISTS(SELECT 1 FROM categories WHERE name='مواد غذایی و سوپرمارکت')")
            db.execSQL("UPDATE category_rules SET categoryId=(SELECT MIN(id) FROM categories WHERE name='مواد غذایی و سوپرمارکت') WHERE categoryId IN (SELECT id FROM categories WHERE name='خوراک و سوپرمارکت') AND EXISTS(SELECT 1 FROM categories WHERE name='مواد غذایی و سوپرمارکت')")
            db.execSQL("DELETE FROM categories WHERE name='خوراک و سوپرمارکت' AND EXISTS(SELECT 1 FROM categories WHERE name='مواد غذایی و سوپرمارکت')")
            db.execSQL("UPDATE categories SET name='مواد غذایی و سوپرمارکت' WHERE name='خوراک و سوپرمارکت'")
            // پاک‌سازی داده‌های تکراری قدیمی پیش از فعال‌کردن قید یکتا.
            db.execSQL("UPDATE transactions SET categoryId=(SELECT MIN(c2.id) FROM categories c2 WHERE TRIM(c2.name)=TRIM((SELECT c1.name FROM categories c1 WHERE c1.id=transactions.categoryId))) WHERE categoryId IN (SELECT id FROM categories c WHERE id<>(SELECT MIN(c2.id) FROM categories c2 WHERE TRIM(c2.name)=TRIM(c.name)))")
            db.execSQL("UPDATE category_rules SET categoryId=(SELECT MIN(c2.id) FROM categories c2 WHERE TRIM(c2.name)=TRIM((SELECT c1.name FROM categories c1 WHERE c1.id=category_rules.categoryId))) WHERE categoryId IN (SELECT id FROM categories c WHERE id<>(SELECT MIN(c2.id) FROM categories c2 WHERE TRIM(c2.name)=TRIM(c.name)))")
            db.execSQL("DELETE FROM categories WHERE id<>(SELECT MIN(c2.id) FROM categories c2 WHERE TRIM(c2.name)=TRIM(categories.name))")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_categories_name ON categories(name)")
        } }

        val MIGRATION_14_15 = object : Migration(14, 15) { override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE TABLE IF NOT EXISTS stock_sms_drafts (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, fingerprint TEXT NOT NULL, sender TEXT NOT NULL, broker TEXT NOT NULL, symbol TEXT NOT NULL, side INTEGER NOT NULL, quantity INTEGER NOT NULL, unitPriceRial INTEGER NOT NULL, totalRial INTEGER NOT NULL, occurredAt INTEGER NOT NULL, status INTEGER NOT NULL, createdAt INTEGER NOT NULL)")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_stock_sms_drafts_fingerprint ON stock_sms_drafts(fingerprint)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_stock_sms_drafts_status ON stock_sms_drafts(status)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_stock_sms_drafts_occurredAt ON stock_sms_drafts(occurredAt)")
        } }

        val MIGRATION_15_16 = object : Migration(15, 16) { override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE accounts ADD COLUMN bankAccountKind TEXT NOT NULL DEFAULT 'OTHER'")
            db.execSQL("ALTER TABLE accounts ADD COLUMN monthlyInterestBearing INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE accounts ADD COLUMN monthlyInterestRatePercent REAL")
            db.execSQL("ALTER TABLE accounts ADD COLUMN interestDestinationAccountId INTEGER")
        } }

        /** نمونه قوانین متداول ایران؛ کاربر می‌تواند آن‌ها را خاموش، ویرایش یا حذف کند. */
        val DEFAULT_IRAN_RULES: List<Pair<String, String>> = listOf(
            "اسنپ" to "حمل و نقل و خودرو", "تپسی" to "حمل و نقل و خودرو",
            "بنزین" to "حمل و نقل و خودرو", "پارکینگ" to "حمل و نقل و خودرو",
            "افق کوروش" to "مواد غذایی و سوپرمارکت", "جانبو" to "مواد غذایی و سوپرمارکت",
            "رفاه" to "مواد غذایی و سوپرمارکت", "شهروند" to "مواد غذایی و سوپرمارکت",
            "اسنپ‌فود" to "رستوران و کافه", "رستوران" to "رستوران و کافه",
            "دیجی‌کالا" to "خرید آنلاین", "ترب" to "خرید آنلاین",
            "داروخانه" to "درمان و سلامت", "بیمارستان" to "درمان و سلامت",
            "همراه اول" to "اینترنت و تلفن", "ایرانسل" to "اینترنت و تلفن",
            "رایتل" to "اینترنت و تلفن", "مخابرات" to "اینترنت و تلفن",
            "قبض برق" to "قبوض", "قبض آب" to "قبوض", "قبض گاز" to "قبوض",
            "بیمه ایران" to "بیمه", "تأمین اجتماعی" to "بیمه",
            "حقوق" to "حقوق", "کارمزد" to "کارمزد بانکی",
            "سود سپرده" to "سود و سرمایه‌گذاری", "سود ماهانه" to "سود و سرمایه‌گذاری", "سود علی الحساب" to "سود و سرمایه‌گذاری"
        )

        /** دسته‌های اولیه پیش‌فرض. */
        val DEFAULT_CATEGORIES: List<Triple<String, Long, Int>> = listOf(
            Triple("مواد غذایی و سوپرمارکت", 0xFF4CAF50, CategoryKind.EXPENSE),
            Triple("رستوران و کافه", 0xFFFF7043, CategoryKind.EXPENSE),
            Triple("حمل و نقل و خودرو", 0xFF29B6F6, CategoryKind.EXPENSE),
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

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                // Consolidate all former built-in transport/vehicle variants into one active
                // category while preserving IDs referenced by historical transactions.
                val variants = arrayOf("حمل‌ونقل", "حمل و نقل", "حمل‌ونقل (خودرو)", "تعمیر و نگهداری خودرو", "حمل و نقل و خودرو")
                db.execSQL(
                    "UPDATE categories SET archived = 1 WHERE builtin = 1 AND name IN (?, ?, ?, ?, ?)",
                    variants
                )
                db.execSQL(
                    "UPDATE categories SET name = ?, archived = 0 WHERE id = (SELECT MIN(id) FROM categories WHERE builtin = 1 AND name IN (?, ?, ?, ?, ?))",
                    arrayOf("حمل و نقل و خودرو", *variants)
                )
                // Add each recommendation once on fresh and existing installations. Explicit
                // user-created rules retain higher priority (10 versus these suggestions' 0).
                for ((keyword, categoryName) in DEFAULT_IRAN_RULES) {
                    db.execSQL(
                        "INSERT INTO category_rules (keyword, categoryId, enabled, priority, createdByUser, createdAt) " +
                            "SELECT ?, id, 1, 0, 0, ? FROM categories c WHERE c.name = ? AND c.archived = 0 " +
                            "AND NOT EXISTS (SELECT 1 FROM category_rules r WHERE r.keyword = ? AND r.categoryId = c.id)",
                        arrayOf(keyword, 0L, categoryName, keyword)
                    )
                }
            }
        }
    }
}
