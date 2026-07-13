import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKmpLibrary)
    alias(libs.plugins.mavenPublish)
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    // group y version vienen de gradle.properties (GROUP / VERSION_NAME)
    coordinates(artifactId = "qrscanner-core")

    pom {
        name.set("QR Scanner Core (KMP)")
        description.set(
            "Headless Kotlin Multiplatform module that decodes a QR code image " +
                "(PNG/JPEG bytes) to its raw text via KScan (ML Kit on Android, Vision on iOS)."
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
