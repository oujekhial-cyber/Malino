# ============================================================
#  خرج‌یار — قوانین کوچک‌سازی و مبهم‌سازی (R8)
#  هدف: سخت‌تر کردن مهندسی معکوس، بدون شکستن Room/Serialization/Glance
# ============================================================

# ---------- kotlinx.serialization ----------
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class ir.kharjyar.app.** {
    *** Companion;
}
-keepclasseswithmembers class ir.kharjyar.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ---------- SQLCipher ----------
-keep class net.sqlcipher.** { *; }
-keep class net.sqlcipher.database.** { *; }
-dontwarn net.sqlcipher.**

# ---------- سخت‌سازی ----------
# حذف کامل لاگ‌ها از نسخه انتشار تا اطلاعات مالی در logcat نشت نکند
-assumenosideeffects class android.util.Log {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}
# مبهم‌سازی نام فایل/خط در stacktrace
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

# ---------- Room ----------
# پیاده‌سازی‌های تولیدشده Room با بازتاب پیدا می‌شوند
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface *
-dontwarn androidx.room.paging.**

# ---------- موجودیت‌ها و مدل‌های داده ----------
# نام فیلدها باید دست‌نخورده بماند تا ستون‌های دیتابیس و JSON بکاپ خراب نشوند
-keep class ir.kharjyar.app.data.db.** { *; }
-keep class ir.kharjyar.app.core.backup.** { *; }
-keep class ir.kharjyar.app.data.prefs.** { *; }

# ---------- Glance / ویجت ----------
-keep class ir.kharjyar.app.widget.** { *; }
-keep class * extends android.appwidget.AppWidgetProvider { *; }

# ---------- enum ----------
# enumها با valueOf از روی نام بازیابی می‌شوند (تنظیمات و بکاپ)
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ---------- Parcelable / Serializable ----------
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}
