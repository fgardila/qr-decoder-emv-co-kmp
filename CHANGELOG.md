# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [2.0.0] - 2026-06-12

API redesign release. Migration from 1.x:
`EmvQrCodeDecoder(raw).decode()` → `EmvQr.decode(raw)`,
`CRCValidator.validate(raw)` → `EmvQr.isCrcValid(raw)`
(`EmvQr.shared.decode(rawText:)` / `isCrcValid(rawText:)` from Swift).

### Added

- `EmvQr` — single public entry point: `decode`, `decodeWithDiagnostics`
  and `isCrcValid`.
- **Parse diagnostics** (`decodeWithDiagnostics` → `ParseDiagnostics`):
  observability of the lenient parse (tags parsed, characters consumed,
  `isFullyParsed`) without giving up the never-throws contract.
- **JVM target**: the library is now usable from plain JVM backends
  (Ktor/Spring) in addition to Android and iOS.
- KDoc on the entire public API, including the semantic values defined by the
  EASPBV spec (tax conditions, channels, transaction purposes, tip indicator).
- API reference generated with Dokka and published to GitHub Pages.
- Quality gates in CI: detekt static analysis, Kover coverage verification
  (≥ 80% line coverage; currently ~98%) and the Kotlin built-in ABI validator
  locking the public API surface (`emvdecoder/api/`).

### Changed

- **Breaking:** `EmvQrCodeDecoder` is now `internal` — use `EmvQr.decode`.
- **Breaking:** `CRCValidator` is now an `internal object` — use
  `EmvQr.isCrcValid`.
- **Breaking:** all result data classes have `internal` constructors with
  `@ConsistentCopyVisibility`: consumers can read every field but can no
  longer instantiate or `copy()` decoded results.
- The module is compiled with `explicitApi()`.

### Removed

- **Breaking:** `AdditionalMerchantInformationData.taxes` (was always `null`;
  tags `86`–`89` are reserved by the spec with no defined structure) and the
  unused `TaxesType` enum.

## [1.0.0] - 2026-06-11

First public release on Maven Central: `dev.code93:emvdecoder:1.0.0`.

### Added

- Kotlin Multiplatform EMVCo QR decoder for the Colombian EASPBV v1.4-2025
  standard (Android AAR + iOS frameworks for `iosArm64`/`iosSimulatorArm64`).
- Full TLV parsing of conventions, merchant information (immediate-payment
  keys / Llaves, acquirer network, merchant and aggregator codes), additional
  merchant information (MCC, location, IVA/INC taxes, channel), transaction
  detail (amount, currency, tips), additional data fields (tag 62),
  alternate-language template (tag 64), transfer/collection fields (92–98)
  and discount application (tag 99).
- Transaction ID mapped from tag `90` (`TRXID`) per spec v1.4, with a legacy
  fallback to tag `86` for QRs issued under older spec versions.
- `CRCValidator` — opt-in CRC-16/CCITT-FALSE integrity check.
- Test suite with real Redeban QR samples, synthetic full-coverage QRs,
  malformed-input cases and the standard CRC test vector, running on Android
  host and iOS simulator.
- Demo apps: Jetpack Compose (CameraX + ML Kit) and SwiftUI.
- CI (GitHub Actions) and automated Maven Central releases on `v*` tags.

### Changed

- Project migrated to the recommended KMP structure with Gradle 9.5.1,
  AGP 9.2.1 (`com.android.kotlin.multiplatform.library`) and Kotlin 2.3.21.
- Relicensed from GPL-3.0 to MIT (matching the source file headers).

[Unreleased]: https://github.com/fgardila/qr-decoder-emv-co-kmp/compare/v2.0.0...HEAD
[2.0.0]: https://github.com/fgardila/qr-decoder-emv-co-kmp/compare/v1.0.0...v2.0.0
[1.0.0]: https://github.com/fgardila/qr-decoder-emv-co-kmp/releases/tag/v1.0.0
