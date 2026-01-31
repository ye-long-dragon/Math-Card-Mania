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
    // Standard AndroidX Core dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)

    // Test dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // === Jetpack Compose Dependencies ===

    // 1. Declare the latest stable Compose BOM ONCE.
    //    This manages the versions of many Compose libraries for consistency.
    //    2024.12.01 is a very recent and good choice.
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // 2. Declare Compose libraries, letting the BOM manage their versions.
    //    (Only declare these once after the BOM)
    implementation("androidx.compose.material3:material3") // This is crucial for rememberRipple!
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation") // Essential for many UI elements

    // 3. Activity-Compose and Navigation-Compose.
    //    These are typically covered by the Compose BOM, so no explicit version needed.
    implementation("androidx.activity:activity-compose")
    implementation("androidx.navigation:navigation-compose:2.7.7")// Let BOM manage version

    // 4. Lifecycle ViewModel Compose (can specify version if needed, or let BOM handle if covered)
    //    2.7.0 is a reasonable choice, or remove version if BOM covers it.
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    // 5. Debugging tools for Compose
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // --- Optional (remove if not using XML layouts or legacy Material Design for Views) ---
    // If your app is purely Compose, these are generally not needed:
     implementation("androidx.constraintlayout:constraintlayout:2.1.4")
     implementation("com.google.android.material:material:1.2.0")
    implementation ("androidx.compose.material:material-ripple:1.10.2")
    // If you need them for specific reasons (e.g., hybrid app), keep them.
    // Given your app structure, I recommend removing them for a pure Compose setup.
}

