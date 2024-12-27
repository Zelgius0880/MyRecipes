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
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
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
        versionCode = 11
        versionName = "2.0-beta05"
        testInstrumentationRunner =  "androidx.test.runner.AndroidJUnitRunner"

        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    sourceSets {
        getByName("androidTest") {
            assets.srcDirs (listOf("src/androidTest/assets/", "$projectDir/schemas"))
        }
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
            isMinifyEnabled = false
            isDebuggable = true
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            buildConfigField(type = "String", name = "EMAIL", value = getProps("app.email"))
            signingConfig = signingConfigs.getByName("release")
        }

        getByName("debug") {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            buildConfigField(type = "String", name = "EMAIL", value = getProps("app.email"))
        }

        create("randomTests") {
            initWith(getByName("debug"))
            applicationIdSuffix = ".debug"
        }

        flavorDimensions += "premium"

        productFlavors {
            create("billing") {
                dimension = "premium"
            }

            create("noBilling") {
                dimension = "premium"
            }
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



dependencies {
    implementation(libs.firebase.analytics)
    implementation(libs.crashlytics)

    implementation(project(":data"))
    implementation(project(":ia"))
    implementation(project(":billing"))
    implementation(libs.material)

    // Tests
    testImplementation(libs.test.junit)
    androidTestImplementation(libs.test.runner)
    androidTestImplementation(libs.test.core)
    androidTestImplementation(libs.test.core.testing)
    androidTestImplementation(libs.test.rules)
    androidTestImplementation(libs.test.junit.ext)
    androidTestImplementation(libs.test.worker)

    kspTest(libs.hilt.android.compiler)
    kspAndroidTest(libs.hilt.android.compiler)
    androidTestImplementation(libs.hilt.android)

    implementation(libs.coil)

    //Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.hilt.worker)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.compiler)

    //Android X
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.viewmodel)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.viewmodel.compose)

    //KTX & coroutines
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    //Paging Library
    implementation(libs.paging.runtime)
    implementation(libs.paging.compose)

    //Other
    implementation(libs.oss.licenses)
    implementation(libs.custom.qr.generator)

    //Worker
    implementation(libs.work.runtime)
    implementation(libs.barcode.scanning)

    //CameraX
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.camera2)
    implementation(libs.camera.view)

    //Compose
    implementation(libs.activity.compose)
    implementation(libs.navigation.compose)
    implementation(libs.compose.ui.tooling)
    implementation(libs.compose.ui)
    implementation(libs.compose.animation)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material.icons)
    implementation(libs.compose.material3)
    implementation(libs.compose.material3.window)
    implementation (libs.androidx.constraintlayout.compose)

    implementation(libs.compose.material3.adaptive)
    implementation(libs.compose.material3.adaptive.layout)
    implementation(libs.compose.material3.adaptive.navigation)
    implementation(libs.compose.material3.adaptive.navigation.suite)

}
