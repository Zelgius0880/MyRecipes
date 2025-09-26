buildscript {
    val protobufVersion by extra { "0.9.4" }

    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath(libs.gradle)
        classpath(libs.kotlin.gradle.plugin)
        classpath(libs.google.services)
        classpath("com.google.protobuf:protobuf-gradle-plugin:$protobufVersion")
        classpath(libs.oss.licenses.plugin)
        classpath(libs.firebase.crashlytics.gradle)
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle.kts files
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
    }

}

tasks.register("clean", Delete::class.java) {
    delete(rootProject.buildDir)
}

plugins {
    id("com.google.devtools.ksp") version "2.2.20-2.0.3" apply false
    id("com.google.dagger.hilt.android") version "2.57.2" apply false
    id("org.jetbrains.kotlin.android") version "2.2.20" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.20" apply false
}
