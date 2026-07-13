import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKmpLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    explicitApi()

    android {
        namespace = "dev.code93.qrscanner.compose"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    // Framework umbrella para la app iOS: exporta también el core y emvdecoder
    // (una app iOS solo debe embeber UN framework Kotlin/Native).
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "QrdKit"
            isStatic = true
            export(projects.qrscannerCore)
            export(projects.emvdecoder)
            // Permite invocar suspend desde hilos de fondo en Swift (el scan de
            // galería corre fuera del main thread; Vision es síncrono y pesado).
            binaryOption("objcExportSuspendFunctionLaunchThreadRestriction", "none")
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.qrscannerCore)
            implementation(libs.kscan)
            implementation(libs.calf.file.picker)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
        }
        iosMain.dependencies {
            // Solo iosMain: en Android la app consume emvdecoder desde Maven Central.
            api(projects.emvdecoder)
        }
    }
}
