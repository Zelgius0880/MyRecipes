buildscript {
    val protobufVersion by extra { "0.9.4"}

    repositories {
        google()
        mavenCentral()

    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.2.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.22")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.7.6")

        classpath("com.google.gms:google-services:4.4.0")
        classpath("com.google.protobuf:protobuf-gradle-plugin:$protobufVersion")
        classpath("com.google.android.gms:oss-licenses-plugin:0.10.6")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.9.9")
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle.kts files
    }
}

 allprojects {
    repositories {
        google()
        mavenCentral()
        maven (url = "https://jitpack.io")
    }
}

tasks.register("clean", Delete::class.java) {
    delete (rootProject.buildDir)
}

plugins {
    id("com.google.devtools.ksp") version "1.9.22-1.0.17" apply false
}
