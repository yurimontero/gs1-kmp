plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.maven.publish) apply false
    alias(libs.plugins.dokka)
}

// API reference for the published modules: ./gradlew :dokkaGeneratePublicationHtml -> build/dokka/html
dependencies {
    dokka(project(":gs1-core"))
    dokka(project(":gs1-compose"))
}

dokka {
    moduleName.set("gs1-kmp")
}
