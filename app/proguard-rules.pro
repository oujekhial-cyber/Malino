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
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# ---------- Room ----------
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-keep @androidx.room.Entity class * { *; }
-dontwarn androidx.room.paging.**

# ---------- Glance / App Widget ----------
-keep class ir.kharjyar.app.widget.** { *; }
-keep class * extends android.appwidget.AppWidgetProvider { *; }

# ---------- گیرنده‌ها و اجزای معرفی‌شده در Manifest ----------
-keep class ir.kharjyar.app.receiver.** { *; }
-keep class ir.kharjyar.app.MainActivity { *; }
-keep class ir.kharjyar.app.KharjYarApp { *; }

# ---------- سخت‌سازی ----------
# حذف کامل لاگ‌ها از نسخه نهایی تا اطلاعات مالی در logcat دیده نشود
-assumenosideeffects class android.util.Log {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}
-assumenosideeffects class java.io.PrintStream {
    public void println(...);
    public void print(...);
}

# چند دور بهینه‌سازی + درهم‌ریزی نام‌ها و ترتیب کلاس‌ها
-optimizationpasses 5
-repackageclasses ''
-allowaccessmodification
-overloadaggressively

# نگه‌داشتن شماره خط برای گزارش خطا، ولی مخفی کردن نام فایل اصلی
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
