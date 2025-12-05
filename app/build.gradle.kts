plugins {
    // Standard Android/Kotlin Plugins
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    // Kotlin Kapt for Annotation Processing (needed for Room)
    id("org.jetbrains.kotlin.kapt")

    // Jetpack Compose Plugin
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.alcabolt"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.alcabolt"
        minSdk = 24
        targetSdk = 35
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
        // Keeping Java 8 compatibility for broader range
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        // Updated extension version for better compatibility with Room/Kapt and Compose 1.6.x
        kotlinCompilerExtensionVersion = "1.5.11"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// Added Kapt configuration to improve error handling during stub generation.
kapt {
    // This tells Kapt to try to recover when encountering types that are not yet compiled,
    // which can sometimes prevent the NonExistentClass error.
    correctErrorTypes = true
}

dependencies {
    // --- Dependency Version Variables (Using known stable versions) ---
    val compose_version = "1.6.0" // Standard Compose UI version
    val material3_version = "1.2.1" // Standard Material 3 version
    val nav_version = "2.7.7" // Stable Navigation-Compose version
    val koin_version = "3.5.3" // Corrected Koin version to a stable release
    val room_version = "2.6.1" // Room is mandatory for the compiler

    // --- Core Android & Compatibility ---
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.1")
    implementation("androidx.activity:activity-compose:1.9.0")

    // --- Compose UI and Material 3 ---
    implementation("androidx.compose.ui:ui:$compose_version")
    implementation("androidx.compose.ui:ui-graphics:$compose_version")
    implementation("androidx.compose.ui:ui-tooling-preview:$compose_version")
    implementation("androidx.compose.material3:material3:$material3_version")
    implementation("androidx.compose.material3:material3-window-size-class:$material3_version")
    implementation("androidx.compose.material:material-icons-extended:1.6.8")

    // --- Compose Navigation ---
    implementation("androidx.navigation:navigation-compose:$nav_version")

    // --- Dependency Injection (Koin) ---
    implementation("io.insert-koin:koin-android:$koin_version")
    implementation("io.insert-koin:koin-androidx-compose:$koin_version")

    // --- ML Kit Dependencies (Translation) ---
    implementation("com.google.mlkit:language-id:17.0.5")
    implementation("com.google.mlkit:translate:17.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // --- Room Database (Persistence) ---
    // Ensure the versions are consistent and correct (kapt is mandatory for the compiler)
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    kapt("androidx.room:room-compiler:$room_version")

    // --- Testing Dependencies ---
    testImplementation(libs.junit)
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    androidTestImplementation("androidx.compose.ui:ui-test-junit4:$compose_version")
    debugImplementation("androidx.compose.ui:ui-tooling:$compose_version")
    debugImplementation("androidx.compose.ui:ui-test-manifest:$compose_version")
}