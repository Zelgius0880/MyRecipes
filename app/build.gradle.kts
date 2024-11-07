import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.gms.google-services")
    id("com.google.android.gms.oss-licenses-plugin")
    id("com.google.firebase.crashlytics")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    id("kotlin-parcelize")
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0"
}

val getProps: (propName: String) -> String = {
    val propsFile = rootProject.file("local.properties")
    if (propsFile.exists()) {
        val props = Properties()
        props.load(FileInputStream(propsFile))
        props[it] as String
    } else {
        ""
    }
}
android {
    compileSdk = 35

    defaultConfig {
        applicationId = "zelgius.com.myrecipes"
        minSdk = 28
        targetSdk = 35
        versionCode = 8
        versionName = "2.0-beta03"
        testInstrumentationRunner = "zelgius.com.myrecipes.utils.HiltTestRunner"

        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    sourceSets {
        getByName("androidTest").assets.srcDirs("$projectDir/schemas")
    }

    signingConfigs {

        register("release").configure {
            storeFile = file("zelgius.com.myrecipes")
            storePassword = "keystore"
            keyAlias = ("key0")
            keyPassword = "keystore"
        }
    }


    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isDebuggable = false
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            buildConfigField(type = "String", name = "EMAIL", value = getProps("app.email"))
            signingConfig = signingConfigs.getByName("release")
        }

        getByName("debug") {
            isMinifyEnabled = false
            isDebuggable = true
            applicationIdSuffix = ".debug"
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            buildConfigField(type = "String", name = "EMAIL", value = getProps("app.email"))
        }

        create("generationTest") {
            initWith(getByName("debug"))
            applicationIdSuffix = ".debug"
        }

    }


    packaging {
        resources {
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/license.txt"
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/NOTICE.txt"
            excludes += "META-INF/notice.txt"
            excludes += "META-INF/ASL2.0"
            excludes += "META-INF/atomicfu.kotlin_module"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    namespace = "zelgius.com.myrecipes"

}


val composeVersion = "1.7.5"
val pagingVersion = "3.3.2"
val lifecycleVersion = "2.8.7"
val workVersion = "2.10.0"
val cameraxVersion = "1.4.0"
val coroutinesVersion = "1.9.0-RC.2"

dependencies {
    implementation("com.google.firebase:firebase-crashlytics:19.2.1")
    implementation("com.google.firebase:firebase-analytics:22.1.2")

    implementation(project(":data"))

    // implementation (fileTree(dir:("libs"), include: ["*.jar"]))
    implementation("com.google.android.material:material:1.12.0")

    // Tests
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test:core:1.6.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("org.mockito:mockito-core:5.10.0")
    androidTestImplementation("org.mockito:mockito-core:5.10.0")
    androidTestImplementation("androidx.test:rules:1.6.1")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.work:work-testing:$workVersion")

    kspTest("com.google.dagger:hilt-android-compiler:2.51.1")
    kspAndroidTest("com.google.dagger:hilt-android-compiler:2.51.1")
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.51.1")

    androidTestImplementation("com.google.code.gson:gson:2.10.1")
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("de.hdodenhof:circleimageview:3.1.0")

    //Hilt
    implementation("com.google.dagger:hilt-android:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    ksp("com.google.dagger:hilt-android-compiler:2.51.1")
    implementation("androidx.hilt:hilt-work:1.2.0")
    ksp("androidx.hilt:hilt-compiler:1.2.0")

    //Android X
    implementation("androidx.fragment:fragment:1.8.5")
    implementation("androidx.fragment:fragment-ktx:1.8.5")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
    implementation("androidx.legacy:legacy-support-v13:1.0.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycleVersion")

    //KTX & coroutines
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")

    //Paging Library
    implementation("androidx.paging:paging-runtime-ktx:$pagingVersion")
    implementation("androidx.paging:paging-compose:3.3.2")

    //Other
    implementation("com.h6ah4i.android.widget.advrecyclerview:advrecyclerview:1.0.0")
    implementation("com.google.zxing:core:3.5.3")
    implementation("com.google.android.gms:play-services-oss-licenses:17.1.0")
    implementation("com.github.alexzhirkevich:custom-qr-generator:1.6.2")

    //Worker
    implementation("androidx.work:work-runtime-ktx:$workVersion")
    implementation("com.google.mlkit:barcode-scanning:17.3.0")

    //CameraX
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:${cameraxVersion}")
    implementation("androidx.camera:camera-view:1.4.0")

    //Compose
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.navigation:navigation-compose:2.8.3")
    implementation("androidx.compose.ui:ui-tooling:$composeVersion")
    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.compose.animation:animation:$composeVersion")
    implementation("androidx.compose.foundation:foundation:$composeVersion")
    implementation("androidx.compose.material:material-icons-extended:1.7.5")
    implementation("androidx.compose.material3:material3:1.3.1")
    implementation("androidx.compose.material3:material3-window-size-class:1.3.1")

    implementation("androidx.compose.material3.adaptive:adaptive:1.1.0-alpha06")
    implementation("androidx.compose.material3.adaptive:adaptive-layout:1.1.0-alpha06")
    implementation("androidx.compose.material3.adaptive:adaptive-navigation:1.1.0-alpha06")
    implementation("androidx.compose.material3:material3-adaptive-navigation-suite:1.3.1")


    // Mediapipe
    implementation("com.google.mediapipe:tasks-vision-image-generator:0.10.16")
    //implementation ("com.google.mediapipe:tasks-vision:0.10.16")

    // Fixme: this two first dependencies will need to be removed when the com.google.mediapipe:tasks-vision will not trigger duplicated classes anymore
    compileOnly("com.google.auto.value:auto-value-annotations:1.8.1")
    annotationProcessor("com.google.auto.value:auto-value:1.8.1")
}
