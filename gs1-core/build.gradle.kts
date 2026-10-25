import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.dokka)
}

kotlin {
    explicitApi()

    // Android
    android {
        namespace = "io.github.yurimontero.gs1"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    // Desktop (Windows, Linux, macOS) and any other JVM
    jvm()

    // iOS (device + Apple Silicon simulator)
    iosArm64()
    iosSimulatorArm64()

    // Web
    js {
        browser {
            // Browser tests need a local Chrome; common tests already run on Node.js.
            testTask { enabled = false }
        }
        nodejs()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            testTask { enabled = false }
        }
        nodejs()
    }

    sourceSets {
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
    moduleName.set("gs1-core")
    dokkaSourceSets.configureEach {
        includes.from("Module.md")
        sourceLink {
            localDirectory.set(file("src"))
            remoteUrl.set(uri("https://github.com/yurimontero/gs1-kmp/tree/main/gs1-core/src"))
            remoteLineSuffix.set("#L")
        }
    }
}
