import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKmpLibrary)
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.dokka)
}

dokka {
    moduleName.set("emvdecoder")
    dokkaSourceSets.configureEach {
        sourceLink {
            localDirectory.set(file("src"))
            remoteUrl("https://github.com/fgardila/qr-decoder-emv-co-kmp/tree/main/emvdecoder/src")
            remoteLineSuffix.set("#L")
        }
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates("dev.code93", "emvdecoder", "1.0.0")

    pom {
        name.set("EMV QR Decoder Colombia")
        description.set(
            "Kotlin Multiplatform library to parse EMVCo merchant-presented QR codes " +
                "per the Colombian EASPBV industry standard (Redeban, Credibanco, Bre-B)."
        )
        inceptionYear.set("2024")
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
    android {
        namespace = "dev.code93.android.qrd"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }

        withHostTestBuilder {}.configure {}
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "emvdecoder"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            //put your multiplatform dependencies here
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
