plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.playfulwatch.phone"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.playfulwatch.phone"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=org.splitties.compose.oclock.ExperimentalComposeOClockApi",
        )
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":watchfaces"))
    implementation(libs.oclock.core)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.runtime)
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
}
