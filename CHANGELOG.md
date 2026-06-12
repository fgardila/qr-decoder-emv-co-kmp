# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- KDoc on the entire public API, including the semantic values defined by the
  EASPBV spec (tax conditions, channels, transaction purposes, tip indicator).
- API reference generated with Dokka and published to GitHub Pages.

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

[Unreleased]: https://github.com/fgardila/qr-decoder-emv-co-kmp/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/fgardila/qr-decoder-emv-co-kmp/releases/tag/v1.0.0
