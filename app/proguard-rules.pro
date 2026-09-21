# Keep kotlinx.serialization generated serializers
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class ir.kharjyar.app.** {
    *** Companion;
}
-keepclasseswithmembers class ir.kharjyar.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}
