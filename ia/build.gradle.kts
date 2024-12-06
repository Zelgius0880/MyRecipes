import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties


plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    id ("org.jetbrains.kotlin.plugin.serialization")
}


android {
    namespace = "com.zelgius.myrecipes.ia"
    compileSdk = 35


    defaultConfig {
        minSdk = 28

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        buildConfigField(name = "CLOUD_FUNCTION_KEY", type = "String", value = "\"${gradleLocalProperties(rootDir, providers).getProperty("cloudFunctionKey")}\"")
        buildConfigField(name = "CLOUD_FUNCTION_URL", type = "String", value = "\"${gradleLocalProperties(rootDir, providers).getProperty("cloudFunctionUrl")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        create("randomTests") {}
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    api(project(path = (":data")))

    testImplementation(libs.test.junit)
    androidTestImplementation(libs.test.junit.ext)
    androidTestImplementation(libs.test.runner)

    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.hilt.worker)
    ksp(libs.hilt.compiler)
    implementation(libs.work.runtime)

    //CameraX
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.camera2)
    implementation(libs.camera.view)

    // Mediapipe & IA
    implementation(libs.tasks.vision.image.generator)
    //implementation (libs.tasks.vision)
    implementation(libs.generativeai)

    compileOnly(libs.auto.value.annotations)
    annotationProcessor(libs.auto.value)

    implementation (libs.kotlinx.serialization.json)

    //FIREBASE
    api(platform(libs.firebase.bom))
    api (libs.firebase.functions.ktx)
}