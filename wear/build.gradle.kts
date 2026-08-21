plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.playfulwatch.wear"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"
    }

    flavorDimensions += "distribution"
    productFlavors {
        create("collection") {
            dimension = "distribution"
            applicationId = "com.playfulwatch.wear.collection"
            resValue("string", "app_name", "Playful Watch")
        }
        create("hourglass") {
            dimension = "distribution"
            applicationId = "com.playfulwatch.wear.hourglass"
            resValue("string", "app_name", "Hourglass")
        }
        create("liquidVials") {
            dimension = "distribution"
            applicationId = "com.playfulwatch.wear.liquidvials"
            resValue("string", "app_name", "Liquid Vials")
        }
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
    implementation(libs.oclock.renderer)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.wear.compose)
    implementation(libs.wear.compose.material)
    implementation(libs.activity.compose)
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
}
