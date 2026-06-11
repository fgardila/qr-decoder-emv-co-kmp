# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Kotlin Multiplatform Mobile (KMM) library and demo apps for decoding EMV QR codes (Colombian EMVCo spec). Three modules:

- `emvdecoder/` — the KMP library with all decoding logic (Gradle module, targets Android + iosX64/iosArm64/iosSimulatorArm64 as a static framework named `emvdecoder`)
- `androidApp/` — Compose demo app that scans QR codes with CameraX + ML Kit and displays decoded fields
- `iosApp/` — SwiftUI demo app (Xcode project, not a Gradle module; its build phase runs the `embedAndSignAppleFrameworkForXcode` Gradle task to link the framework)

## Commands

```bash
./gradlew :emvdecoder:assembleRelease      # Android AAR → emvdecoder/build/outputs/aar/
./gradlew :emvdecoder:build                # full build incl. iOS frameworks → emvdecoder/build/bin/<target>/
./gradlew :androidApp:assembleDebug        # demo app APK

# Tests (KMP — pick the target)
./gradlew :emvdecoder:allTests                      # all targets
./gradlew :emvdecoder:testDebugUnitTest             # Android/JVM unit tests only
./gradlew :emvdecoder:iosSimulatorArm64Test         # iOS simulator tests
./gradlew :emvdecoder:testDebugUnitTest --tests "dev.code93.kmp.qrd.CRCValidatorTest"   # single class
```

The iOS app is built/run from `iosApp/iosApp.xcodeproj` in Xcode (or `xcodebuild`).

## Architecture

Decoding lives entirely in `emvdecoder/src/commonMain/kotlin/dev/code93/kmp/qrd/`:

- `EmvQrCodeDecoder` parses the QR payload as TLV (2-char tag, 2-char length, value) into a tag→value map, then maps tags into typed data classes, returning `QRCodeEmvCoColombiaData` from `decode()`.
- Composite tags (e.g. `26`, `62`, `64`, `80`–`86`, `91`, `99`) contain nested TLV. Sub-fields are modeled as enums implementing `SubFieldType` (each enum constant carries its `subTag`); `extractSubFields<T>()` reflects over the enum to pull every sub-value. To add a new field: define/extend an enum in `data/` and wire it into the corresponding `get...Data()` method in `EmvQrCodeDecoder`.
- `CRCValidator.validate()` checks the trailing 4-char CRC-16 CCITT. Callers (both apps' `MainViewModel`s) validate CRC and then decode — the decoder itself does not validate.
- Data classes live in `data/` (one file per QR section: merchant info, transaction detail, taxes, etc.).

Both demo apps mirror each other: `MainViewModel` validates + decodes, then flattens the result into a list of expandable title/key-value sections for display.

## Caveats

- The tests in `emvdecoder/src/commonTest/` were written against a different API: `EmvQrCodeDecoderTest` imports a `dev.code93.kmp.qrd.model` package and calls a static-style `EmvQrCodeDecoder.decode(string)` — neither exists in `commonMain` (the real API is `EmvQrCodeDecoder(qrCode).decode()` with classes in `data/`). Expect the common test compilation to fail until tests are rewritten to match the actual API.
- Package naming is inconsistent in `androidApp`: files under `src/main/java/dev/code93/kmp/qrd/android/` mostly declare `dev.code93.kmp.qrd.android`, but `MainViewModel.kt` declares `dev.code93.android.emvreaderqr`. The library's Android namespace is `dev.code93.android.qrd` while its Kotlin code uses `dev.code93.kmp.qrd`.
- Code comments and user-facing strings are partly in Spanish; keep that convention where it exists.
