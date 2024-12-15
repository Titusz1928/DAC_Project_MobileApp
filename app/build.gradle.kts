plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.dac_project"
    compileSdk = 34

    packaging {
        exclude("META-INF/license.txt")
        exclude("META-INF/notice.txt")

    }

    defaultConfig {
        applicationId = "com.example.dac_project"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.play.services.maps)
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.squareup.picasso:picasso:2.8")
    implementation("com.google.android.libraries.places:places:3.1.0")
    implementation("org.springframework:spring-web:5.3.11")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}