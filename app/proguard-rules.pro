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
