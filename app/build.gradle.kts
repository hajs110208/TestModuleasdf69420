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

// ---- smali -> dex -------------------------------------------------------------------------
// Replacement classes live as smali in src/main/smali. They are assembled into a single dex
// that is packaged as assets/replacements.dex and loaded at runtime by ClassReplacer.
val smaliTool: Configuration by configurations.creating

val smaliSrcDir = layout.projectDirectory.dir("src/main/smali")
val smaliAssetsDir = layout.buildDirectory.dir("generated/smali-assets")

val assembleSmali by tasks.registering(JavaExec::class) {
    description = "Assembles src/main/smali into assets/replacements.dex"
    inputs.dir(smaliSrcDir)
    outputs.dir(smaliAssetsDir)
    classpath = smaliTool
    mainClass.set("com.android.tools.smali.smali.Main")
    doFirst {
        smaliAssetsDir.get().asFile.mkdirs()
        args(
            "assemble",
            "--api", "27",
            "--output", smaliAssetsDir.get().file("replacements.dex").asFile.absolutePath,
            smaliSrcDir.asFile.absolutePath,
        )
    }
}

android.sourceSets.getByName("main").assets.srcDir(smaliAssetsDir)
tasks.named("preBuild") { dependsOn(assembleSmali) }

dependencies {
    // Provided by the Xposed/LSPosed framework at runtime.
    compileOnly("de.robv.android.xposed:api:82")

    // Build-time only: smali assembler.
    smaliTool("com.android.tools.smali:smali:3.0.9")
}
