import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKmpLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.mavenPublish)
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    // group y version vienen de gradle.properties (GROUP / VERSION_NAME)
    coordinates(artifactId = "qrscanner-compose")

    pom {
        name.set("QR Scanner Compose (KMP)")
        description.set(
            "Compose Multiplatform QR scanner screen for Android/iOS: KScan camera " +
                "with torch/flashlight control, gallery import (Calf) decoded to raw text " +
                "and built-in camera permission handling. Ships the QrdKit umbrella iOS framework."
        )
        inceptionYear.set("2026")
        url.set("https://github.com/fgardila/qr-decoder-emv-co-kmp")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/license/mit/")
                distribution.set("repo")
            }
        }
        developers {
            developer {
                id.set("fgardila")
                name.set("Fabian Guillermo Ardila Castro")
                url.set("https://github.com/fgardila")
            }
        }
        scm {
            url.set("https://github.com/fgardila/qr-decoder-emv-co-kmp")
            connection.set("scm:git:git://github.com/fgardila/qr-decoder-emv-co-kmp.git")
            developerConnection.set("scm:git:ssh://git@github.com/fgardila/qr-decoder-emv-co-kmp.git")
        }
    }
}

// Empaqueta README y LICENSE en los jars publicados: exploradores de paquetes
// (socket.dev, etc.) leen el README desde dentro del artefacto, no desde GitHub.
tasks.withType<Jar>().configureEach {
    from(rootProject.file("README.md"), rootProject.file("LICENSE"))
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
