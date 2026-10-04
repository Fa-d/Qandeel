import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Pure Kotlin, multiplatform: the compiler guarantees the domain never touches Android or the data
// layer, and the same models and logic run on Android (through the JVM target) and on iOS.
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kover)
}

kotlin {
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.coroutines.core)
        }
        jvmTest.dependencies {
            implementation(project(":core:testing"))
        }
    }
}
