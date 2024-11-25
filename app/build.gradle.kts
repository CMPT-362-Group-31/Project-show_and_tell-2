plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    namespace = "com.example.project"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.project"
        minSdk = 26
        targetSdk = 35
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.espresso.core)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    val room_version = "2.6.0"
    val lifecycle_version = "2.6.2"
    implementation("androidx.room:room-ktx:$room_version")
    kapt("androidx.room:room-compiler:$room_version")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx: $lifecycle_version")
    implementation ("com.google.android.gms:play-services-maps:18.0.0") // Use latest version
    implementation ("com.google.android.gms:play-services-location:21.0.1")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")

    // Jetpack Compose dependencies
    // https://mvnrepository.com/artifact/androidx.compose.ui/ui
    runtimeOnly("androidx.compose.ui:ui:1.7.2")
    // https://mvnrepository.com/artifact/androidx.compose.material/material
    runtimeOnly("androidx.compose.material:material:1.7.0")
    // https://mvnrepository.com/artifact/androidx.activity/activity-compose
    runtimeOnly("androidx.activity:activity-compose:1.9.0")
    // https://mvnrepository.com/artifact/androidx.lifecycle/lifecycle-runtime-ktx
    runtimeOnly("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")


    // https://mvnrepository.com/artifact/com.google.android.gms/play-services-auth
    implementation("com.google.android.gms:play-services-auth:21.2.0")


    // Gmail API
    // https://mvnrepository.com/artifact/com.google.api-client/google-api-client-android
    implementation("com.google.api-client:google-api-client-android:1.22.0")
    // https://mvnrepository.com/artifact/com.google.api-client/google-api-client-gson
    implementation("com.google.api-client:google-api-client-gson:1.21.0")
    // https://mvnrepository.com/artifact/com.google.apis/google-api-services-gmail
    implementation("com.google.apis:google-api-services-gmail:v1-rev110-1.25.0")


    // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-android
    runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")


    implementation("androidx.compose.material:material:1.5.3")

}