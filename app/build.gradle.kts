import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

android {
    namespace = "ir.kharjyar.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "ir.kharjyar.app"
        minSdk = 31
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // فقط معماری گوشی‌های واقعی؛ x86 مخصوص شبیه‌ساز است و حدود ۷ مگابایت
        // کتابخانه بومی بی‌مصرف به APK اضافه می‌کرد.
        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a")
        }
        // فقط منابع زبان‌های مورد استفاده نگه داشته می‌شوند
        resourceConfigurations += listOf("fa", "en")
    }

    /**
     * امضای نسخه انتشار.
     *
     * کلید هرگز داخل مخزن نگهداری نمی‌شود. مسیر و رمزها از این منابع خوانده می‌شوند
     * (به همین ترتیب اولویت):
     *   ۱) فایل local.properties  ← برای ساخت روی کامپیوتر شخصی
     *   ۲) متغیرهای محیطی         ← برای ساخت در CI
     * اگر هیچ‌کدام نبود، امضای انتشار غیرفعال می‌شود و ساخت debug مثل قبل کار می‌کند.
     */
    val keystoreProps = Properties().apply {
        val f = rootProject.file("local.properties")
        if (f.exists()) f.inputStream().use { load(it) }
    }
    fun secret(key: String, env: String): String? =
        (keystoreProps.getProperty(key) ?: System.getenv(env))?.takeIf { it.isNotBlank() }

    val storeFilePath = secret("kharjyar.storeFile", "KEYSTORE_FILE")
    val storePw = secret("kharjyar.storePassword", "KEYSTORE_PASSWORD")
    val keyAliasName = secret("kharjyar.keyAlias", "KEY_ALIAS")
    val keyPw = secret("kharjyar.keyPassword", "KEY_PASSWORD")
    val hasOwnKey = storeFilePath != null && storePw != null &&
        keyAliasName != null && keyPw != null && file(storeFilePath).exists()

    /**
     * کلید پشتیبان داخل مخزن.
     *
     * رمزش عمومی است و راز محسوب نمی‌شود؛ هدفش فقط این است که خروجی انتشار
     * همیشه «امضاشده و قابل نصب» باشد. APK بدون امضا با خطای
     * «App not installed as package appears to be invalid» رد می‌شود.
     *
     * برای انتشار واقعی، کلید اختصاصی را از طریق Secretها بدهید تا جای این یکی
     * را بگیرد (مقدار hasOwnKey آن موقع true می‌شود).
     */
    val fallbackKey = rootProject.file("signing/kharjyar-fallback.p12")
    val useFallback = !hasOwnKey && fallbackKey.exists()

    signingConfigs {
        create("release") {
            if (hasOwnKey) {
                storeFile = file(storeFilePath!!)
                storePassword = storePw
                keyAlias = keyAliasName
                keyPassword = keyPw
            } else if (useFallback) {
                storeFile = fallbackKey
                storePassword = "kharjyar"
                keyAlias = "kharjyar"
                keyPassword = "kharjyar"
            }
            // هر سه طرح امضا: v1 برای سازگاری، v2/v3 برای تأیید سریع‌تر سیستم
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
        }
    }

    val hasSigning = hasOwnKey || useFallback

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            resValue("string", "app_label", "خرج‌یار")
        }
        release {
            if (hasSigning) signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            resValue("string", "app_label", "خرج‌یار")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.fragment.ktx)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.biometric)
    implementation(libs.sqlcipher)
    implementation(libs.androidx.sqlite.ktx)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotlinx.serialization.json)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.espresso)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.room.testing)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
