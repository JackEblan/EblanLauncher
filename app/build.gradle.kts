plugins {
    alias(libs.plugins.com.eblan.launcher.application)
    alias(libs.plugins.com.eblan.launcher.applicationCompose)
    alias(libs.plugins.com.eblan.launcher.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.eblan.launcher"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.eblan.launcher"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
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
}

dependencies {
    implementation(projects.common)
    implementation(projects.data.repository)
    implementation(projects.designSystem)
    implementation(projects.domain.framework)
    implementation(projects.domain.model)
    implementation(projects.domain.useCase)
    implementation(projects.domain.repository)
    implementation(projects.feature.edit)
    implementation(projects.feature.home)
    implementation(projects.feature.pin)
    implementation(projects.feature.settings.appDrawer)
    implementation(projects.feature.settings.folder)
    implementation(projects.feature.settings.general)
    implementation(projects.feature.settings.gestures)
    implementation(projects.feature.settings.home)
    implementation(projects.feature.settings.settings)
    implementation(projects.framework.fileManager)
    implementation(projects.framework.launcherApps)
    implementation(projects.framework.packageManager)
    implementation(projects.framework.wallpaperManager)
    implementation(projects.framework.widgetManager)
    implementation(projects.service)

    implementation(libs.accompanist.permissions)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
}