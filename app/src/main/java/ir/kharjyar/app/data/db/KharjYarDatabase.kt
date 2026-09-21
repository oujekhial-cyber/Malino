package ir.kharjyar.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        AccountEntity::class,
        AccountSenderEntity::class,
        SmsCandidateEntity::class,
        SmsTemplateEntity::class,
        TransactionEntity::class,
        TransferGroupEntity::class,
        CategoryEntity::class,
        CategoryRuleEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class KharjYarDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun smsDao(): SmsDao
    abstract fun templateDao(): TemplateDao
    abstract fun transactionDao(): TransactionDao
    abstract fun transferDao(): TransferDao
    abstract fun categoryDao(): CategoryDao

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
                    .addCallback(SeedCallback)
                    .build()
                    .also { instance = it }
            }

        /** دسته‌های اولیه پیش‌فرض. */
        val DEFAULT_CATEGORIES: List<Triple<String, Long, Int>> = listOf(
            Triple("خوراک و سوپرمارکت", 0xFF4CAF50, CategoryKind.EXPENSE),
            Triple("رستوران و کافه", 0xFFFF7043, CategoryKind.EXPENSE),
            Triple("حمل‌ونقل", 0xFF29B6F6, CategoryKind.EXPENSE),
            Triple("مسکن و اجاره", 0xFF8D6E63, CategoryKind.EXPENSE),
            Triple("قبوض", 0xFFFFA726, CategoryKind.EXPENSE),
            Triple("اینترنت و تلفن", 0xFF7E57C2, CategoryKind.EXPENSE),
            Triple("درمان", 0xFFEF5350, CategoryKind.EXPENSE),
            Triple("پوشاک", 0xFFEC407A, CategoryKind.EXPENSE),
            Triple("آموزش", 0xFF26A69A, CategoryKind.EXPENSE),
            Triple("تفریح", 0xFFFFCA28, CategoryKind.EXPENSE),
            Triple("خرید آنلاین", 0xFF5C6BC0, CategoryKind.EXPENSE),
            Triple("هدیه و کمک", 0xFFAB47BC, CategoryKind.BOTH),
            Triple("کارمزد بانکی", 0xFF78909C, CategoryKind.EXPENSE),
            Triple("حقوق", 0xFF66BB6A, CategoryKind.INCOME),
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
