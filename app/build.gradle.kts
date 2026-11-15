import java.util.Properties

plugins {
    id("com.android.application")
}

// versionName / versionCode are written by semantic-release (scripts/set-version.sh)
// into version.properties before the release APK is built.
val versionProps = Properties().apply {
    val f = rootProject.file("version.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

// Optional release signing, supplied through the environment (see README).
// Without these the release APK is signed with the debug key so it is still installable.
val keystorePath: String? = System.getenv("ANDROID_KEYSTORE_FILE")

android {
    namespace = "com.hajs.testmodule"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.hajs.testmodule"
        minSdk = 27 // LSPosed requires Android 8.1+
        targetSdk = 35
        versionCode = versionProps.getProperty("versionCode", "1").toInt()
        versionName = versionProps.getProperty("versionName", "0.0.0-dev")
    }

    signingConfigs {
        if (keystorePath != null) {
            create("release") {
                storeFile = file(keystorePath)
                storePassword = System.getenv("ANDROID_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("ANDROID_KEY_ALIAS")
                keyPassword = System.getenv("ANDROID_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            // Keep class names intact: the replacement classes are looked up by name.
            isMinifyEnabled = false
            signingConfig = if (keystorePath != null) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // Provided by the Xposed/LSPosed framework at runtime.
    compileOnly("de.robv.android.xposed:api:82")
}
