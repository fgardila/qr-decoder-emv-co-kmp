import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKmpLibrary)
}

kotlin {
    explicitApi()

    android {
        namespace = "dev.code93.qrscanner.core"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }

        withHostTestBuilder {}.configure {}
    }

    // Sin binaries.framework propio: el framework umbrella QrdKit (:qrscanner-compose)
    // exporta este módulo hacia la app iOS.
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            // api: la firma pública usa suspend
            api(libs.kotlinx.coroutines.core)
            implementation(libs.kscan)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
