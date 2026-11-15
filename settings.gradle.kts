pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Legacy Xposed API (de.robv.android.xposed:api:82) is hosted here.
        maven("https://api.xposed.info/") {
            content { includeGroup("de.robv.android.xposed") }
        }
    }
}

rootProject.name = "TestModule"
include(":app")
