
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("com.google.gms.google-services") version "4.4.2" apply false // Usamos la misma versión que en el plugin
}

buildscript {
    dependencies {
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.8.8")
        classpath("com.google.gms:google-services:4.4.2") // Google Services plugin, versión actualizada para ser consistente
    }
}
