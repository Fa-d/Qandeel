import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.time.Duration

// The phone UI, written once for Android and iOS (Compose Multiplatform). The JVM target runs the
// screenshot tests: Compose Desktop draws with Skia, as iOS does, so the goldens show what both see.
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.roborazzi)
    alias(libs.plugins.kover)
    alias(libs.plugins.kotlin.serialization)
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

    // iOS and the JVM both draw through Skia (Skiko): what talks to Skia directly is written once.
    applyDefaultHierarchyTemplate {
        common {
            group("skiko") {
                withJvm()
                group("ios") { withIos() }
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:domain"))
            api(libs.cmp.runtime)
            api(libs.cmp.foundation)
            api(libs.cmp.ui)
            implementation(libs.cmp.components.resources)
            api(libs.cmp.lifecycle.viewmodel.compose)
            implementation(libs.cmp.lifecycle.runtime.compose)
            implementation(libs.cmp.navigation.compose)
            implementation(libs.kotlinx.serialization.json)
        }
        jvmTest.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.cmp.ui.test)
            implementation(libs.junit)
            implementation(libs.roborazzi.compose.desktop)
            implementation(project(":core:testing"))
            implementation(libs.kotlinx.coroutines.swing)
        }
    }
}

compose.resources {
    packageOfResClass = "dev.sadakat.qandeel.shared.resources"
}

roborazzi {
    outputDir.set(file("src/jvmTest/screenshots"))
}

// A desktop UI test that never goes idle blocks on the UI thread, where runTest's own timeout can't
// reach it: fail the task instead of holding CI until the runner's limit. Normally it takes minutes.
tasks.withType<Test>().configureEach {
    timeout.set(Duration.ofMinutes(30))
}
