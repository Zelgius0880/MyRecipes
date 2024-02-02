import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.devtools.ksp")
    id("androidx.navigation.safeargs")
    id("com.google.gms.google-services")
    id("com.google.android.gms.oss-licenses-plugin")
    id("com.google.firebase.crashlytics")
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
        versionCode = 1
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

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    namespace = "zelgius.com.myrecipes"

}


val mockitoVersion = "2.16.0"
val composeVersion = "1.6.0"

dependencies {
    implementation("com.google.firebase:firebase-crashlytics:18.6.1")
    implementation("com.google.firebase:firebase-analytics:21.5.0")
    val pagingVersion = "2.1.2"
    val lifecycleVersion = ("2.7.0")
    val roomVersion = "2.6.1"
    val navigationVersion = "2.7.6"
    val workVersion = "2.9.0"
    val cameraxVersion = "1.3.1"
    val coroutinesVersion = "1.7.1"


    // implementation (fileTree(dir:("libs"), include: ["*.jar"]))
    implementation("com.google.android.material:material:1.11.0")

    // Tests
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:core:1.5.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("org.mockito:mockito-core:3.12.4")
    androidTestImplementation("androidx.room:room-testing:$roomVersion")
    androidTestImplementation("org.mockito:mockito-core:3.12.4")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.work:work-testing:$workVersion")

    androidTestImplementation("com.google.code.gson:gson:2.10")
    implementation("com.squareup.picasso:picasso:2.71828")
    implementation("de.hdodenhof:circleimageview:3.1.0")

    //Android X
    implementation("androidx.fragment:fragment:1.6.2")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
    implementation("androidx.legacy:legacy-support-v13:1.0.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.room:room-runtime:$roomVersion")

    ksp("androidx.room:room-compiler:$roomVersion")
    // For Kotlin use kapt instead of annotationProcessor
    // optional - Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-ktx:$roomVersion")

    //KTX & coroutines
    implementation("androidx.core:core-ktx:1.12.0")
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
    implementation("com.h6ah4i.android.widget.advrecyclerview:advrecyclerview:1.0.0")
    implementation("com.github.kenglxn.QRGen:android:2.6.0")
    implementation("com.google.android.gms:play-services-oss-licenses:17.0.1")

    //Worker
    implementation("androidx.work:work-runtime-ktx:$workVersion")
    implementation(project(path = (":protobuff")))


    implementation("com.google.mlkit:barcode-scanning:17.2.0")

    //CameraX
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:${cameraxVersion}")
    implementation("androidx.camera:camera-view:1.3.1")

    //Compose
    implementation("androidx.compose.ui:ui-tooling:$composeVersion")
    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.compose.foundation:foundation:$composeVersion")
    implementation("androidx.compose.material:material:$composeVersion")
    implementation("androidx.compose.material:material-icons-core:$composeVersion")
    implementation("androidx.compose.material:material-icons-extended:$composeVersion")

}
