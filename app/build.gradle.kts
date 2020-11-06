plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("kotlin-android-extensions")
    id("androidx.navigation.safeargs")
    id("com.google.gms.google-services")
    id("com.google.android.gms.oss-licenses-plugin")
}

val sdkVersion = rootProject.extra["compileSdkVersion"] as Int
val kotlinVersion = rootProject.extra["kotlinVersion"]

android {
    compileSdkVersion(sdkVersion)
    buildToolsVersion("30.0.2")

    defaultConfig {
        applicationId("zelgius.com.myrecipes")
        minSdkVersion(26)
        targetSdkVersion(30)
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner("androidx.test.runner.AndroidJUnitRunner")

        javaCompileOptions {
            annotationProcessorOptions {
                arguments(mutableMapOf("room.schemaLocation" to "$projectDir/schemas"))
            }
        }
    }

    buildFeatures {
        viewBinding = true
    }

    sourceSets {
        getByName("androidTest").assets.srcDirs("$projectDir/schemas")
    }

    lintOptions {
        warning("InvalidPackage")
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            //buildConfigField "Boolean", "DB_IN_MEMORY", false
        }


/*        debug {
            buildConfigField "Boolean", "DB_IN_MEMORY", false
        }

        test {
            buildConfigField "Boolean", "DB_IN_MEMORY", true
        }*/
    }
    signingConfigs {
        named("debug").configure {
            storeFile = file("keystore")
            storePassword("keystore")
            keyAlias = ("keystore")
            keyPassword("keystore")
        }

        register("release").configure {
            storeFile = file("keystore")
            storePassword("keystore")
            keyAlias = ("keystore")
            keyPassword("keystore")
        }
    }

    packagingOptions {
        exclude("META-INF/DEPENDENCIES")
        exclude("META-INF/LICENSE")
        exclude("META-INF/LICENSE.txt")
        exclude("META-INF/license.txt")
        exclude("META-INF/NOTICE")
        exclude("META-INF/NOTICE.txt")
        exclude("META-INF/notice.txt")
        exclude("META-INF/ASL2.0")
        exclude("META-INF/atomicfu.kotlin_module")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}


val mockitoVersion = "2.16.0"

dependencies {
    val pagingVersion = "2.1.2"
    val lifecycleVersion = ("2.2.0")
    val roomVersion = "2.2.5"
    val navigationVersion = "2.3.1"
    val workVersion = "1.0.1"
    val cameraxVersion = "1.0.0-beta11"
    val coroutinesVersion = "1.3.0-M2"


    // implementation (fileTree(dir:("libs"), include: ["*.jar"]))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.4.10")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("com.google.android.material:material:1.3.0-alpha03")

    // Tests
    testImplementation("junit:junit:4.13.1")
    androidTestImplementation("androidx.test:runner:1.3.0")
    androidTestImplementation("androidx.test:core:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
    androidTestImplementation("androidx.arch.core:core-testing:2.1.0")
    testImplementation("org.mockito:mockito-core:3.5.13")
    androidTestImplementation("androidx.room:room-testing:$roomVersion")
    androidTestImplementation("org.mockito:mockito-core:3.5.13")
    androidTestImplementation("androidx.test:rules:1.3.0")
    androidTestImplementation("com.google.code.gson:gson:2.8.6")

    implementation("com.squareup.picasso:picasso:2.71828")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    //debugImplementation("com.amitshekhar.android:debug-db:1.0.6") //adb forward tcp:8080 tcp:8080

    //Android X
    implementation("androidx.fragment:fragment:1.3.0-beta01")
    implementation("androidx.fragment:fragment-ktx:1.3.0-beta01")
    implementation("androidx.core:core-ktx:1.5.0-alpha04")
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")
    implementation("androidx.recyclerview:recyclerview:1.2.0-alpha06")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.lifecycle:lifecycle-extensions:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
    implementation("androidx.legacy:legacy-support-v13:1.0.0")
    implementation("androidx.appcompat:appcompat:1.3.0-alpha02")
    implementation("androidx.room:room-runtime:$roomVersion")

    kapt("androidx.room:room-compiler:$roomVersion")
    // For Kotlin use kapt instead of annotationProcessor
    // optional - Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-ktx:$roomVersion")

    //KTX & coroutines
    implementation("androidx.core:core-ktx:1.5.0-alpha04")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")


    //navigation
    implementation("androidx.navigation:navigation-fragment:$navigationVersion")
    implementation("androidx.navigation:navigation-fragment-ktx:$navigationVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navigationVersion")

    //Paging Library
    implementation("androidx.paging:paging-runtime:$pagingVersion") // use -ktx for Kotlin)
    implementation("androidx.paging:paging-runtime-ktx:$pagingVersion")

    //Other
    implementation("com.amulyakhare:com.amulyakhare.textdrawable:1.0.1")
    implementation("com.h6ah4i.android.widget.advrecyclerview:advrecyclerview:1.0.0")
    implementation("com.github.kenglxn.QRGen:android:2.6.0")
    implementation("com.google.android.gms:play-services-oss-licenses:17.0.0")

    //Worker
    implementation("android.arch.work:work-runtime-ktx:$workVersion")
    implementation(project(path = (":protobuff")))


    implementation("com.google.mlkit:barcode-scanning:16.0.3")



    implementation ("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:${cameraxVersion}")
    implementation("androidx.camera:camera-view:1.0.0-alpha18")

}
