
buildscript {
    repositories {
        google()
        mavenCentral()

    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.8.0") // Update AGP version
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.0") // Match Kotlin version
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.7.7")
    }
}

// Root build.gradle.kts
plugins {

    id("com.android.application") version "8.8.0" apply false
    id("com.android.library") version "8.8.0" apply false
    id("org.jetbrains.kotlin.android") version "2.0.0" apply false
    id("androidx.navigation.safeargs.kotlin") version "2.7.7" apply false
}


