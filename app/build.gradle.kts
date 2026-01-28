// build.gradle.kts (Module :app)

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // Keep this line - it's correctly applying the Compose Compiler plugin for Kotlin 2.0
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
}

android {
    namespace = "com.example.baraclan.mentalchallengemath_namepending"
    // Use compileSdk 34 for now for stability, or 35 if you prefer a newer one that is stable
    compileSdk =36

    defaultConfig {
        applicationId = "com.example.baraclan.mentalchallengemath_namepending"
        minSdk = 24
        targetSdk = 34 // Match compileSdk
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    // REMOVE THIS ENTIRE BLOCK:
    // composeOptions {
    //     kotlinCompilerExtensionVersion = "1.5.9" // Or 1.5.11, etc.
    // }
    // The 'org.jetbrains.kotlin.plugin.compose' handles this for Kotlin 2.0
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)
    // Optionally remove if not using XML layouts:
    // implementation(libs.androidx.constraintlayout)
    // implementation(libs.material)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // === Jetpack Compose Dependencies ===

    // Use a recent Compose BOM, e.g., 2024.02.00 is a good choice.
    // Ensure this BOM is compatible with Kotlin 2.0 (it usually is if it's recent).
    val composeBom = platform("androidx.compose:compose-bom:2024.02.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")

    implementation("androidx.activity:activity-compose") // Should be covered by BOM
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0") // Keep explicit if not covered by BOM

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    implementation("com.google.android.material:material:1.11.0")

}
