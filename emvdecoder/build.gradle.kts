import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKmpLibrary)
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kover)
    alias(libs.plugins.detekt)
}

detekt {
    source.setFrom("src/commonMain/kotlin", "src/commonTest/kotlin", "src/androidHostTest/kotlin")
    buildUponDefaultConfig = true
    config.setFrom(rootProject.file("config/detekt/detekt.yml"))
}

kover {
    reports {
        verify {
            rule {
                minBound(80)
            }
        }
    }
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

    // group y version vienen de gradle.properties (GROUP / VERSION_NAME)
    coordinates(artifactId = "emvdecoder")

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

// Empaqueta README y LICENSE en los jars publicados: exploradores de paquetes
// (socket.dev, etc.) leen el README desde dentro del artefacto, no desde GitHub.
tasks.withType<Jar>().configureEach {
    from(rootProject.file("README.md"), rootProject.file("LICENSE"))
}

kotlin {
    explicitApi()

    @OptIn(ExperimentalAbiValidation::class)
    abiValidation {
        enabled.set(true)
    }

    jvm()

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
