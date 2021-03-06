buildscript {
    val kotlinVersion by extra { "1.4.31" }
    val androidPluginVersion by extra {"3.5.0-beta04"}
    val targetSdkVersion by extra { 28 }
    val compileSdkVersion by extra { 29}
    val minSdkVersion by extra { 14}
    val junitVersion by extra { "4.12"}
    val mockitoVersion by extra { "2.16.0"}
    val protobufVersion by extra { "0.8.13"}
    val keystore by extra { "zelgius.com.myrecipes" }

    repositories {
        google()
        mavenCentral()
        maven (
            url = "http://dl.bintray.com/amulyakhare/maven"
        )

    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.0.0-alpha11")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.31")
        classpath("android.arch.navigation:navigation-safe-args-gradle-plugin:1.0.0")

        classpath("com.google.gms:google-services:4.3.5")
        classpath("com.google.protobuf:protobuf-gradle-plugin:$protobufVersion")
        //classpath "androidx.navigation:safe-args-gradle-plugin:1.0.0-alpha01"
        classpath("com.google.android.gms:oss-licenses-plugin:0.10.2")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.5.1")
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle.kts files
    }
}

 allprojects {
    repositories {
        google()
        mavenCentral()
        maven (url = "http://dl.bintray.com/amulyakhare/maven")
        maven (url = "https://jitpack.io")

        @Suppress("JcenterRepositoryObsolete")
        jcenter {
            content {
                includeModule("org.jetbrains.kotlinx", "kotlinx-collections-immutable-jvm")
            }
        }
    }
}

tasks.register("clean", Delete::class.java) {
    delete (rootProject.buildDir)
}
