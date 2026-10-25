import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.dokka)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    explicitApi()

    // Public API dump in api/: `check` fails if the API changes until `./gradlew updateKotlinAbi` is run
    @OptIn(ExperimentalAbiValidation::class)
    abiValidation()

    // Android
    android {
        namespace = "io.github.yurimontero.gs1.compose"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    // Desktop (Windows, Linux, macOS)
    jvm()

    // iOS (device + Apple Silicon simulator)
    iosArm64()
    iosSimulatorArm64()

    // Web
    js {
        browser {
            testTask { enabled = false }
        }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            testTask { enabled = false }
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":gs1-core"))
            // Compose types (Modifier, Color, Painter…) are part of this module's public API
            api(libs.compose.runtime)
            api(libs.compose.ui)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

// Bytecode for Java 11 on Android and JVM, so the library works on older desktop runtimes.
tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_11)
}
tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = "11"
    targetCompatibility = "11"
}

dokka {
    moduleName.set("gs1-compose")
    dokkaSourceSets.configureEach {
        includes.from("Module.md")
        sourceLink {
            localDirectory.set(file("src"))
            remoteUrl.set(uri("https://github.com/yurimontero/gs1-kmp/tree/main/gs1-compose/src"))
            remoteLineSuffix.set("#L")
        }
    }
}
