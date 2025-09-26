import java.io.FileInputStream
import java.util.Locale
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
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.20"
}

fun getProps(propName: String, propsFile: File = rootProject.file("local.properties")): String {
    return if (propsFile.exists()) {
        val props = Properties()
        props.load(FileInputStream(propsFile))
        // Ensure a default empty string is returned if property is not found to avoid nulls
        props.getProperty(propName, "")
    } else {
        ""
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

android {
    compileSdk = 36

    val gradlePropFile = rootProject.file("gradle.properties")
    defaultConfig {
        applicationId = "zelgius.com.myrecipes"
        minSdk = 28
        targetSdk = 36
        versionCode = getProps("build.versionCode", gradlePropFile).toIntOrNull() ?: 1
        versionName = getProps("build.versionName", gradlePropFile)
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    sourceSets {
        getByName("androidTest") {
            assets.srcDirs(listOf("src/androidTest/assets/", "$projectDir/schemas"))
        }
    }

    signingConfigs {
        register("release").configure {
            // Read signing configuration from local.properties
            val storeFilePath = getProps("RELEASE_STORE_FILE")
            if (storeFilePath.isNotEmpty()) {
                storeFile =
                    file(storeFilePath) // Assuming storeFilePath is relative to the app module or an absolute path
            }
            storePassword = getProps("RELEASE_STORE_PASSWORD")
            keyAlias = getProps("RELEASE_KEY_ALIAS")
            keyPassword = getProps("RELEASE_KEY_PASSWORD")
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
    implementation(libs.androidx.camera.compose)

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
    implementation(libs.androidx.constraintlayout.compose)

    implementation(libs.compose.material3.adaptive)
    implementation(libs.compose.material3.adaptive.layout)
    implementation(libs.compose.material3.adaptive.navigation)
    implementation(libs.compose.material3.adaptive.navigation.suite)

}


tasks.register<Exec>("releaseBillingBundle") {
    doFirst {
        incrementVersionCode()
        incrementVersionName()
    }
    workingDir = rootProject.projectDir
    runScript("gradlew", ":app:bundleBillingRelease")
}

tasks.register<Exec>("releaseNoBillingBundle") {
    doFirst {
        incrementVersionCode()
    }
    workingDir = rootProject.projectDir
    runScript("gradlew", ":app:bundleNoBillingRelease")
    dependsOn("releaseBillingBundle")
}

tasks.register("createReleases") {
    dependsOn("releaseNoBillingBundle")
}

fun Exec.runScript(vararg args: String) {
    if (System.getProperty("os.name").lowercase(Locale.ROOT).contains("windows")) {
        commandLine( "cmd", "/c", *args)
    } else {
        commandLine ("sh", "-c", *args)
    }
}


fun incrementVersionCode() {
    group = "versioning"
    description =
        "Increments the build.versionCode and updates build.versionName patch number in gradle.properties."
    val propertiesFile = rootProject.file("gradle.properties")
    val properties = Properties()
    propertiesFile.inputStream().use { properties.load(it) }

    val versionCode = properties.getProperty("build.versionCode")?.toIntOrNull() ?: 0
    properties.setProperty("build.versionCode", (versionCode + 1).toString())
    propertiesFile.outputStream().use { properties.store(it, null) }
    println("Incremented versionCode to: ${properties.getProperty("build.versionCode")}")

    // Store properties back to file
    propertiesFile.outputStream().use { properties.store(it, null) }
}

fun incrementVersionName() {
    group = "versioning"
    description =
        "Increments the build.versionCode and updates build.versionName patch number in gradle.properties."
    val propertiesFile = rootProject.file("gradle.properties")
    val properties = Properties()
    propertiesFile.inputStream().use { properties.load(it) }

    // Increment versionName patch
    val versionName = properties.getProperty("build.versionName")
    val versionNameParts = versionName.split(".").toMutableList()
    val lastPartIndex = versionNameParts.size - 1
    val patchVersion = versionNameParts[lastPartIndex].toIntOrNull() ?: 0
    versionNameParts[lastPartIndex] = (patchVersion + 1).toString()
    val newVersionName = versionNameParts.joinToString(".")
    properties.setProperty("build.versionName", newVersionName)
    println("Incremented versionName to: ${properties.getProperty("build.versionName")}")

    // Store properties back to file
    propertiesFile.outputStream().use { properties.store(it, null) }
}
