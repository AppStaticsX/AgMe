plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.appstaticsx.app.agmeapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.appstaticsx.app.agmeapp"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = false
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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.firebase.auth)
    implementation(libs.play.services.auth)
    implementation(libs.firebase.database)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.lottie)
    implementation(libs.fading.text.view)
    implementation(libs.mpandroidchart)
    implementation(libs.taptargetview)
    implementation(libs.circleimageview)
    implementation(libs.blurview)
    implementation(libs.play.services.location)
    implementation(libs.glide)
}