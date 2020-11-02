buildscript {
    val kotlinVersion by extra { "1.4.0" }
    val androidPluginVersion by extra {"3.5.0-beta04"}
    val targetSdkVersion by extra { 28 }
    val compileSdkVersion by extra { 29}
    val minSdkVersion by extra { 14}
    val junitVersion by extra { "4.12"}
    val mockitoVersion by extra { "2.16.0"}
    val protobufVersion by extra { "0.8.13"}
    val keystore by extra { "keystore" }

    repositories {
        google()
        jcenter()
        maven (
            url = "http://dl.bintray.com/amulyakhare/maven"
        )

    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.2.0-alpha15")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("android.arch.navigation:navigation-safe-args-gradle-plugin:1.0.0")

        classpath("com.google.gms:google-services:4.3.4")
        classpath("com.google.protobuf:protobuf-gradle-plugin:$protobufVersion")
        //classpath "androidx.navigation:safe-args-gradle-plugin:1.0.0-alpha01"

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle.kts files
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        maven (url = "https://jitpack.io")
    }
}

tasks.register("clean", Delete::class.java) {
    delete (rootProject.buildDir)
}
