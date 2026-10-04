import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// The phone UI, written once for Android and iOS (Compose Multiplatform). The JVM target runs the
// screenshot tests: Compose Desktop draws with Skia, as iOS does, so the goldens show what both see.
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.roborazzi)
    alias(libs.plugins.kover)
}

kotlin {
    android {
        namespace = "dev.sadakat.qandeel.shared"
        compileSdk = 37
        minSdk = 26
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
        androidResources {
            enable = true
        }
    }
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:domain"))
            api(libs.cmp.runtime)
            api(libs.cmp.foundation)
            api(libs.cmp.ui)
            implementation(libs.cmp.components.resources)
        }
        jvmTest.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.cmp.ui.test)
            implementation(libs.junit)
            implementation(libs.roborazzi.compose.desktop)
        }
    }
}

compose.resources {
    packageOfResClass = "dev.sadakat.qandeel.shared.resources"
}

roborazzi {
    outputDir.set(file("src/jvmTest/screenshots"))
}
