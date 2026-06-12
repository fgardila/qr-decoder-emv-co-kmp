# Contributing

Thanks for your interest in improving this library! Issues and pull requests
are welcome, in English or Spanish.

## Development setup

- **JDK 17+** (to run Gradle), Android SDK with API 36, and Xcode (only for
  the iOS demo app / simulator tests).
- Useful commands:

```bash
./gradlew :emvdecoder:allTests                 # run the full test suite (Android host + iOS simulator)
./gradlew :emvdecoder:testAndroidHostTest      # Android/JVM tests only (fastest loop)
./gradlew :emvdecoder:assemble                 # build the AAR
./gradlew :androidApp:assembleDebug            # build the Android demo app
```

## Guidelines

- **The decoder is lenient by design**: malformed input must never throw.
  Standard compliance is the authorizing backend's responsibility, not this
  client library's. Please don't add validations that reject QRs.
- **Follow the spec**: field semantics come from the EASPBV standard — the
  PDF (`EASPBV-Campos-QRCode-EMVCo-Industria-v1.4-2025.pdf`) is in the repo
  root. Reference the tag/sub-tag in your PR description.
- **Tests are required** for behavior changes. Build test payloads with the
  TLV/CRC helpers in `emvdecoder/src/commonTest/.../EmvQrTestBuilder.kt`. If
  you have a real-world QR that decodes incorrectly, include it (redact any
  personal data such as phone numbers or emails in the key fields).
- **Public API changes** need a matching entry in `CHANGELOG.md` under
  *Unreleased*, and follow semantic versioning (breaking changes target the
  next major release).
- **Quality gates** run in CI and locally:
  - `./gradlew :emvdecoder:detekt` — static analysis (config in `config/detekt/detekt.yml`).
  - `./gradlew :emvdecoder:koverVerify` — line coverage must stay ≥ 80%.
  - `./gradlew :emvdecoder:checkKotlinAbi` — the public API surface is locked
    in `emvdecoder/api/`. If your change intentionally alters the public API,
    run `./gradlew :emvdecoder:updateKotlinAbi` and commit the updated dump
    (and remember semver: breaking changes target the next major).
- CI must be green (Android + iOS jobs) before review.

## Releasing (maintainers)

Bump the version in the `coordinates(...)` call in
`emvdecoder/build.gradle.kts`, update `CHANGELOG.md`, merge to `main`, and
push a `vX.Y.Z` tag — the release workflow publishes to Maven Central and
creates the GitHub Release.
