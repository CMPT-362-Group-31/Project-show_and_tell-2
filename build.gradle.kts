// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    id("com.google.gms.google-services") version "4.4.0"
    id("androidx.navigation.safeargs.kotlin") version "2.7.7" apply false
}
buildscript {
    dependencies {
        classpath("com.google.android.libraries.mapsplatform.secrets-gradle-plugin:secrets-gradle-plugin:2.0.1")
//        classpath("com.google.gms:google-services:4.4.0")
//        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.7.0")
    }
}