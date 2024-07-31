plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("androidx.navigation.safeargs.kotlin")
    kotlin("kapt")
}

android {
    namespace = "com.example.tokomurahinventory"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.tokomurahinventory"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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

    kapt {
        javacOptions {
            // These options are used by the kapt task itself
            option("-source", "17")
            option("-target", "17")
        }
    }

    buildFeatures {
        dataBinding = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    val roomVersion = "2.4.3" // Replace with your actual Room version
    val lifecycleExtensionsVersion = "2.2.0" // Replace with your actual Lifecycle Extensions version
    val coroutineVersion = "1.7.3"

    //hash password
    implementation("org.mindrot:jbcrypt:0.4")

    implementation("androidx.core:core-ktx:1.3.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.2.0")
    //implementation("androidx.activity:activity-compose:1.9.0")
    //implementation(platform("androidx.compose:compose-bom:2023.03.00"))
    //implementation("androidx.compose.ui:ui")
    //implementation("androidx.compose.ui:ui-graphics")
    //implementation("androidx.compose.ui:ui-tooling-preview")
    //implementation("androidx.compose.material3:material3")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    //androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))
    //androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    //debugImplementation("androidx.compose.ui:ui-tooling")
    //debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.5.0")
    implementation("androidx.navigation:navigation-ui-ktx:2.5.0")

    // Room
    implementation("androidx.room:room-runtime:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")

    //Coroutine
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutineVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutineVersion")
    implementation("androidx.room:room-ktx:2.4.0")

    // Legacy support and Lifecycle Extensions
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.lifecycle:lifecycle-extensions:$lifecycleExtensionsVersion")

    // Nav drawer
    implementation("androidx.drawerlayout:drawerlayout:1.1.1")
    implementation("com.google.android.material:material:1.9.0") // Updated version

    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1") // Updated version

    implementation("androidx.work:work-runtime:2.7.1")
}
