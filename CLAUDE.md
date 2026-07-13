# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Kotlin Multiplatform (KMP) library and demo apps for decoding EMV QR codes (Colombian EMVCo spec), using the recommended KMP structure: separate entry-point modules per platform plus a shared library. Toolchain: **Gradle 9.5.1, AGP 9.2.1, Kotlin 2.3.21, JDK 17+**.

- `emvdecoder/` — the KMP library with all decoding logic. Uses the Android-KMP library plugin (`com.android.kotlin.multiplatform.library`); the Android config lives inside `kotlin { android { ... } }` (note: the `androidLibrary {}` block name from the JetBrains migration guide is deprecated in AGP 9.2 — use `android {}`). Targets: Android, `jvm`, `iosArm64`, `iosSimulatorArm64` (static framework named `emvdecoder`). Compiled with `explicitApi()`; the only public entry point is `object EmvQr` (`decode`, `decodeWithDiagnostics`, `isCrcValid`) — `EmvQrCodeDecoder` and `CRCValidator` are internal, and result data classes have internal constructors (`@ConsistentCopyVisibility`).
- `qrscanner-core/` — **local** (unpublished) headless KMP module: `QrImageScanning`/`QrImageScanner` decode a QR image (`ByteArray`, plus an `NSData` overload in `iosMain` for Swift) to raw text by wrapping KScan's `scanImage` (`io.github.ismai117:KScan` — note the capitalized artifactId; ML Kit on Android, Vision on iOS). Targets: Android (KMP-library plugin), `iosArm64`, `iosSimulatorArm64`; no `jvm()` (KScan doesn't publish it), no framework of its own.
- `qrscanner-compose/` — **local** Compose Multiplatform module: `QrScannerScreen(onResult, onClose, showGalleryButton)` = KScan `ScannerView` (custom overlay, `scannerUiOptions = null`) + torch via `ScannerController.setTorch` + gallery via Calf `rememberFilePickerLauncher` → `qrscanner-core`. Camera permission handled internally via `expect/actual` (`rememberCameraPermissionState`). Spanish UI strings live in `Strings.kt` as constants; icons are hand-built `ImageVector`s (do NOT add material-icons — pinned/frozen at 1.7.8). `iosMain` exposes `qrScannerViewController(onResult, onClose)` (`ComposeUIViewController`) and builds the **`QrdKit` umbrella framework** (static) which `export`s `:qrscanner-core` and `:emvdecoder` — `api(projects.emvdecoder)` is declared **only in iosMain** so Android keeps consuming emvdecoder from Maven Central. The framework sets `binaryOption("objcExportSuspendFunctionLaunchThreadRestriction", "none")` so Swift can call the suspend scan off the main thread.
- `androidApp/` — "QR EMV Colombia" portfolio app (`dev.code93.emvqr`): Clean Architecture (`presentation`/`domain`/`data` under `dev/code93/emvqr/`), MVVM with StateFlow, Hilt (KSP — never kapt; no `org.jetbrains.kotlin.android` plugin with AGP 9 built-in Kotlin), typed Compose Navigation. Consumes the library **from Maven Central** (`libs.emvdecoder`), not the local module, but the scanner modules as **project deps**. The camera destination renders the shared `QrScannerScreen`; gallery via Photo Picker (no media permissions) → `KScanQrImageRepository` → `:qrscanner-core`. The EASPBV QR **generator** (TLV + CRC-16/CCITT-FALSE) lives in `domain/generator/` with round-trip tests against the published library (`androidApp/src/test/`).
- `iosApp/` — "QR EMV Colombia" portfolio app (`dev.code93.emvqr.ios`): SwiftUI + MVVM, clean layers (Domain protocols / Data repositories / Presentation), `AppContainer` composition root, iOS 16, `ObservableObject` state. pbxproj uses a `PBXFileSystemSynchronizedRootGroup` (objectVersion 70) — new files under `iosApp/iosApp/` are picked up automatically, no pbxproj edits needed. The app embeds **only the `QrdKit` framework** (`./gradlew :qrscanner-compose:embedAndSignAppleFrameworkForXcode` build phase; `import QrdKit` — never re-add a separate emvdecoder embed: two static Kotlin frameworks duplicate the runtime). Camera scanning = `ComposeQrScannerView` (`UIViewControllerRepresentable` over `QrScannerViewControllerKt.qrScannerViewController`); gallery decode = `QrdKitQrImageDecoder` → `QrImageScanner().scan(data:)` (suspend exported as async). QR generation uses CoreImage; the Swift `EaspbvQrBuilder` mirrors the Android one.

## Commands

```bash
./gradlew :emvdecoder:assemble             # AAR → emvdecoder/build/outputs/aar/emvdecoder.aar (single variant, no assembleRelease)
./gradlew :emvdecoder:linkReleaseFrameworkIosArm64           # iOS device framework
./gradlew :emvdecoder:linkDebugFrameworkIosSimulatorArm64    # simulator framework → build/bin/<target>/<type>Framework/
./gradlew :androidApp:assembleDebug        # demo app APK
./gradlew :qrscanner-compose:linkDebugFrameworkIosSimulatorArm64   # QrdKit umbrella framework (the step most likely to break)
./gradlew :qrscanner-core:testAndroidHostTest :qrscanner-core:iosSimulatorArm64Test   # scanner module tests

# Tests
./gradlew :emvdecoder:allTests                  # all targets
./gradlew :emvdecoder:testAndroidHostTest       # Android/JVM host tests only (NOT testDebugUnitTest)
./gradlew :emvdecoder:iosSimulatorArm64Test     # iOS simulator tests
./gradlew :emvdecoder:testAndroidHostTest --tests "dev.code93.kmp.qrd.CRCValidatorTest"   # single class
```

iOS app smoke build (no signing): `xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' ARCHS=arm64 ONLY_ACTIVE_ARCH=YES build CODE_SIGNING_ALLOWED=NO` (x86_64 is unsupported since `iosX64` was dropped).

## Releasing

Three artifacts publish to Maven Central under **one shared version**: `dev.code93:emvdecoder`, `dev.code93:qrscanner-core` and `dev.code93:qrscanner-compose` (vanniktech maven-publish plugin in each module). The version lives in `gradle.properties` (`VERSION_NAME`, plus `GROUP=dev.code93`) — bump it there before tagging; each module's `coordinates(artifactId = ...)` only sets the artifact name. Pushing a `v*` tag triggers `.github/workflows/release.yml` (tests → root `publishAndReleaseToMavenCentral`, which publishes every module with the plugin applied → GitHub Release with the three AARs). Signing/credentials come from repository secrets (`MAVEN_CENTRAL_USERNAME/PASSWORD`, `SIGNING_KEY/PASSWORD`); without a local signing key, `publishToMavenLocal` fails at `sign*Publication` (expected — verify POMs with the `generatePomFileFor*Publication` tasks instead). After a release, bump the `emvdecoder` version in `libs.versions.toml` (the Maven Central version androidApp consumes) to match.

## Architecture

Decoding lives entirely in `emvdecoder/src/commonMain/kotlin/dev/code93/kmp/qrd/`:

- `EmvQr.decode` (backed by the internal `EmvQrCodeDecoder`) parses the QR payload as TLV (2-char tag, 2-char length, value) into a tag→value map, then maps tags into typed data classes, returning `QRCodeEmvCoColombiaData`. Malformed input never throws — parsing stops at the first invalid element; `EmvQr.decodeWithDiagnostics` additionally reports tags parsed / chars consumed.
- Composite tags (e.g. `26`, `62`, `64`, `80`–`86`, `90`, `91`, `99`) contain nested TLV. Per the EASPBV v1.4 spec (PDF in repo root), the transaction ID lives in tag `90` (`TRXID`); the decoder falls back to tag `86` for QRs from older spec versions (`86`–`89` are now reserved for future taxes). Sub-fields are modeled as enums implementing `SubFieldType` (each enum constant carries its `subTag`); `extractSubFields<T>()` reflects over the enum to pull every sub-value. To add a new field: define/extend an enum in `data/` and wire it into the corresponding `get...Data()` method in `EmvQrCodeDecoder`.
- `EmvQr.isCrcValid()` checks the trailing 4-char CRC-16/CCITT-FALSE. Callers (both apps' `MainViewModel`s) validate CRC and then decode — the decoder itself does not validate.
- Data classes live in `data/` (one file per QR section: merchant info, transaction detail, taxes, etc.).

Both demo apps mirror each other: `MainViewModel` validates + decodes, then flattens the result into a list of expandable title/key-value sections for display.

## Tests

`emvdecoder/src/commonTest` runs on every target; `emvdecoder/src/androidHostTest` is the Android host (JVM) source set — the `androidUnitTest` name does not apply under the Android-KMP library plugin, and host tests are opt-in via `withHostTestBuilder {}.configure {}` in the build script. Test QR payloads are built with the TLV/CRC helpers in `commonTest/.../EmvQrTestBuilder.kt` (`tlv()`, `withCrc()`); the CRC helper is pinned by the standard CRC-16/CCITT-FALSE vector `"123456789" → 29B1`.

## Caveats

- The library has a single build variant: `debugImplementation`-style configurations don't exist in `emvdecoder` (they do still work in `androidApp`).
- `material-icons-core` is pinned at 1.7.8 because the icons artifacts were frozen and dropped from recent Compose BOMs.
- The library's Android namespace is `dev.code93.android.qrd` while its Kotlin code uses `dev.code93.kmp.qrd`.
- Code comments and user-facing strings are partly in Spanish; keep that convention where it exists.
- `org.gradle.jvmargs` is 4 GB because linking the QrdKit framework (Compose + Skiko) OOMs at 2 GB.
- Torch cannot be tested in the iOS simulator — camera/torch/gallery scanning need a real device on both platforms.
- If Calf ever breaks against a newer Kotlin (klib ABI), the agreed fallback is FileKit (`io.github.vinceglb:filekit-dialogs-compose`, near-identical picker API).
