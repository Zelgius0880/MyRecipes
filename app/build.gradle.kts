import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    kotlin("android")
    id("androidx.navigation.safeargs")
    id("com.google.gms.google-services")
    id("com.google.android.gms.oss-licenses-plugin")
    id("com.google.firebase.crashlytics")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
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
    compileSdk = 34

    defaultConfig {
        applicationId ="zelgius.com.myrecipes"
        minSdk = 26
        targetSdk =34
        versionCode = 2
        versionName = "1.0"
        testInstrumentationRunner ="androidx.test.runner.AndroidJUnitRunner"

        javaCompileOptions {
            annotationProcessorOptions {
                arguments(mutableMapOf("room.schemaLocation" to "$projectDir/schemas"))
            }
        }
    }

    buildFeatures {
        viewBinding = true
        compose = true
        buildConfig = true
    }

    sourceSets {
        getByName("androidTest").assets.srcDirs("$projectDir/schemas")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField(type = "String", name = "EMAIL", value = getProps("app.email"))
        }

        getByName("debug") {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            buildConfigField(type = "String", name = "EMAIL", value = getProps("app.email"))
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
        /*named("debug").configure {
            storeFile = file("keystore")
            storePassword ="keystore"
            keyAlias = "keystore"
            keyPassword="keystore"
        }*/

        register("release").configure {
            storeFile = file("zelgius.com.myrecipes")
            storePassword= "keystore"
            keyAlias = ("key0")
            keyPassword="keystore"
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


val mockitoVersion = "2.16.0"
val composeVersion = "1.7.0-beta03"

dependencies {
    implementation("com.google.firebase:firebase-crashlytics:19.0.2")
    implementation("com.google.firebase:firebase-analytics:22.0.2")
    val pagingVersion = "3.3.0"
    val lifecycleVersion = ("2.8.2")
    val workVersion = "2.9.0"
    val cameraxVersion = "1.3.4"
    val coroutinesVersion = "1.7.3"

    implementation(project(":data"))

    // implementation (fileTree(dir:("libs"), include: ["*.jar"]))
    implementation("com.google.android.material:material:1.12.0")

    // Tests
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test:runner:1.6.0")
    androidTestImplementation("androidx.test:core:1.6.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.0")
    androidTestImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("org.mockito:mockito-core:5.10.0")
    androidTestImplementation("org.mockito:mockito-core:5.10.0")
    androidTestImplementation("androidx.test:rules:1.6.0")
    androidTestImplementation("androidx.test.ext:junit:1.2.0")
    androidTestImplementation("androidx.work:work-testing:$workVersion")

    androidTestImplementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.picasso:picasso:2.71828") // TODO remove that once the migration will be done
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("de.hdodenhof:circleimageview:3.1.0")

    //Hilt
    implementation("com.google.dagger:hilt-android:2.50")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    ksp("com.google.dagger:hilt-android-compiler:2.50")
    implementation("androidx.hilt:hilt-work:1.2.0")
    ksp("androidx.hilt:hilt-compiler:1.2.0")

    //Android X
    implementation("androidx.fragment:fragment:1.8.0")
    implementation("androidx.fragment:fragment-ktx:1.8.0")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
    implementation("androidx.legacy:legacy-support-v13:1.0.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycleVersion")


    //KTX & coroutines
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")

    //Paging Library
    implementation("androidx.paging:paging-runtime-ktx:$pagingVersion")
    implementation("androidx.paging:paging-compose:3.3.0")

    //Other
    implementation("com.h6ah4i.android.widget.advrecyclerview:advrecyclerview:1.0.0")
    implementation("com.github.kenglxn.QRGen:android:2.6.0")
    implementation("com.google.android.gms:play-services-oss-licenses:17.1.0")

    //Worker
    implementation("androidx.work:work-runtime-ktx:$workVersion")

    implementation("com.google.mlkit:barcode-scanning:17.2.0")

    //CameraX
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:${cameraxVersion}")
    implementation("androidx.camera:camera-view:1.3.4")

    //Compose
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.compose.ui:ui-tooling:$composeVersion")
    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation( "androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.compose.foundation:foundation:$composeVersion")
    implementation("androidx.compose.material3:material3:1.2.1")
    implementation("androidx.compose.material3:material3-window-size-class:1.2.1")
    implementation ("androidx.compose.animation:animation:$composeVersion")
    //implementation("androidx.compose.material3:material3-adaptive:1.3.0-beta03")
    implementation("androidx.compose.material3:material3-adaptive-navigation-suite:1.3.0-beta03")

}
