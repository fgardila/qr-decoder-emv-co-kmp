# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Kotlin Multiplatform (KMP) library and demo apps for decoding EMV QR codes (Colombian EMVCo spec), using the recommended KMP structure: separate entry-point modules per platform plus a shared library. Toolchain: **Gradle 9.5.1, AGP 9.2.1, Kotlin 2.3.21, JDK 17+**.

- `emvdecoder/` — the KMP library with all decoding logic. Uses the Android-KMP library plugin (`com.android.kotlin.multiplatform.library`); the Android config lives inside `kotlin { android { ... } }` (note: the `androidLibrary {}` block name from the JetBrains migration guide is deprecated in AGP 9.2 — use `android {}`). iOS targets: `iosArm64`, `iosSimulatorArm64` (static framework named `emvdecoder`).
- `androidApp/` — Compose demo app (CameraX + ML Kit scanning). AGP 9 built-in Kotlin: no `org.jetbrains.kotlin.android` plugin; `jvmTarget` is set via a top-level `kotlin { compilerOptions {} }` block. Sources in `src/main/kotlin`.
- `iosApp/` — SwiftUI demo app (Xcode project, not a Gradle module; its build phase runs the `embedAndSignAppleFrameworkForXcode` Gradle task to link the framework).

## Commands

```bash
./gradlew :emvdecoder:assemble             # AAR → emvdecoder/build/outputs/aar/emvdecoder.aar (single variant, no assembleRelease)
./gradlew :emvdecoder:linkReleaseFrameworkIosArm64           # iOS device framework
./gradlew :emvdecoder:linkDebugFrameworkIosSimulatorArm64    # simulator framework → build/bin/<target>/<type>Framework/
./gradlew :androidApp:assembleDebug        # demo app APK

# Tests
./gradlew :emvdecoder:allTests                  # all targets
./gradlew :emvdecoder:testAndroidHostTest       # Android/JVM host tests only (NOT testDebugUnitTest)
./gradlew :emvdecoder:iosSimulatorArm64Test     # iOS simulator tests
./gradlew :emvdecoder:testAndroidHostTest --tests "dev.code93.kmp.qrd.CRCValidatorTest"   # single class
```

iOS app smoke build (no signing): `xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' ARCHS=arm64 ONLY_ACTIVE_ARCH=YES build CODE_SIGNING_ALLOWED=NO` (x86_64 is unsupported since `iosX64` was dropped).

## Architecture

Decoding lives entirely in `emvdecoder/src/commonMain/kotlin/dev/code93/kmp/qrd/`:

- `EmvQrCodeDecoder` parses the QR payload as TLV (2-char tag, 2-char length, value) into a tag→value map, then maps tags into typed data classes, returning `QRCodeEmvCoColombiaData` from `decode()`. Malformed input never throws — parsing stops at the first invalid element.
- Composite tags (e.g. `26`, `62`, `64`, `80`–`86`, `91`, `99`) contain nested TLV. Sub-fields are modeled as enums implementing `SubFieldType` (each enum constant carries its `subTag`); `extractSubFields<T>()` reflects over the enum to pull every sub-value. To add a new field: define/extend an enum in `data/` and wire it into the corresponding `get...Data()` method in `EmvQrCodeDecoder`.
- `CRCValidator.validate()` checks the trailing 4-char CRC-16/CCITT-FALSE. Callers (both apps' `MainViewModel`s) validate CRC and then decode — the decoder itself does not validate.
- Data classes live in `data/` (one file per QR section: merchant info, transaction detail, taxes, etc.).

Both demo apps mirror each other: `MainViewModel` validates + decodes, then flattens the result into a list of expandable title/key-value sections for display.

## Tests

`emvdecoder/src/commonTest` runs on every target; `emvdecoder/src/androidHostTest` is the Android host (JVM) source set — the `androidUnitTest` name does not apply under the Android-KMP library plugin, and host tests are opt-in via `withHostTestBuilder {}.configure {}` in the build script. Test QR payloads are built with the TLV/CRC helpers in `commonTest/.../EmvQrTestBuilder.kt` (`tlv()`, `withCrc()`); the CRC helper is pinned by the standard CRC-16/CCITT-FALSE vector `"123456789" → 29B1`.

## Caveats

- The library has a single build variant: `debugImplementation`-style configurations don't exist in `emvdecoder` (they do still work in `androidApp`).
- `material-icons-core` is pinned at 1.7.8 because the icons artifacts were frozen and dropped from recent Compose BOMs.
- Package naming is inconsistent in `androidApp`: files under `src/main/kotlin/dev/code93/kmp/qrd/android/` mostly declare `dev.code93.kmp.qrd.android`, but `MainViewModel.kt` declares `dev.code93.android.emvreaderqr`. The library's Android namespace is `dev.code93.android.qrd` while its Kotlin code uses `dev.code93.kmp.qrd`.
- Code comments and user-facing strings are partly in Spanish; keep that convention where it exists.
