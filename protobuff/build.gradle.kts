import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.proto

plugins {
    id("com.android.library")
    id("com.google.protobuf")
}


val generatedFilesBaseDir = "$projectDir/src"

android {
    compileSdk = 36

    sourceSets {
        getByName("main").java {
            srcDirs("$generatedFilesBaseDir/main/javalite")
        }
        getByName("main").proto {
            srcDirs("src/main/proto")
        }
    }

    buildTypes {
        create("randomTests") {}
    }

    dependencies {
        //implementation (fileTree("libs", include: ["*.jar"]))
        api ("com.google.protobuf:protobuf-javalite:4.32.1")
    }
    namespace = "zelgius.com.protobuff"

}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.22.3"

    }
    plugins {
        id("javalite") {
            artifact = "com.google.protobuf:protoc-gen:3.0.0:osx-x86_64"
        }
    }
    generateProtoTasks {
        all().forEach  {
            it.builtins {
                id("java") {
                    option ("lite")
                }
            }
        }
    }
}

tasks.withType(ProcessResources::class.java) {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}