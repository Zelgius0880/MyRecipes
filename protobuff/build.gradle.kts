import com.google.protobuf.gradle.*

plugins {
    id("java-library")
    id("com.google.protobuf")
}
val generatedFilesBaseDir = "$projectDir/src"

sourceSets {
    getByName("main").java.srcDirs("$generatedFilesBaseDir/main/javalite")
}

dependencies {
    //implementation (fileTree("libs", include: ["*.jar"]))
    api( "com.google.protobuf:protobuf-lite:3.0.1")
}

protobuf {

    protoc {
        // You still need protoc like in the non-Android case
        artifact = "com.google.protobuf:protoc:3.0.0"
    }
    plugins {
        id("javalite") {
            // The codegen for lite comes as a separate artifact
            artifact = "com.google.protobuf:protoc-gen-javalite:3.0.0"
        }
    }
    generateProtoTasks {
        ofSourceSet("main").forEach { task ->
            task.builtins {
                // In most cases you don"t need the full Java output
                // if you use the lite output.
                remove ("java")
            }
            task.plugins {
                id("javalite") {}
            }
        }
    }
}