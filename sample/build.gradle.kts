// Desktop demo app (not published). Run with: ./gradlew :sample:run
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

dependencies {
    implementation(project(":gs1-compose"))
    implementation(compose.desktop.currentOs)
    implementation(libs.compose.foundation)
}

compose.desktop {
    application {
        mainClass = "io.github.yurimontero.gs1.sample.MainKt"
    }
}
