# EMV Decoder Project

This project is a Kotlin Multiplatform Mobile (KMM) application designed to decode EMV QR codes. It provides shared business logic in the `emvdecoder` module and platform-specific implementations in the `androidApp` and `iosApp`.

The `emvdecoder` module contains the core logic for parsing and interpreting EMV QR code data.

## Building

### Android AAR

To generate the Android AAR library from the `emvdecoder` module, run the following command in the root of the project:

```bash
./gradlew :emvdecoder:assembleRelease
```

The AAR file will be located at `emvdecoder/build/outputs/aar/emvdecoder-release.aar`.

### iOS Framework

To generate the iOS Framework (`emvdecoder.framework`) from the `emvdecoder` module, run the following command in the root of the project:

```bash
./gradlew :emvdecoder:build
```

The framework can be found in the `emvdecoder/build/bin/` directory, inside subdirectories corresponding to the target iOS architecture and build type (e.g., `iosArm64/emvdecoderReleaseFramework/` or `iosSimulatorArm64/emvdecoderDebugFramework/`).

For a release build, you can typically find the universal framework (if configured) or specific architecture frameworks under a path similar to:
* `emvdecoder/build/bin/iosArm64/emvdecoderReleaseFramework/emvdecoder.framework`
* `emvdecoder/build/bin/iosX64/emvdecoderReleaseFramework/emvdecoder.framework` (for older Intel-based simulators or Macs)
* `emvdecoder/build/bin/iosSimulatorArm64/emvdecoderReleaseFramework/emvdecoder.framework` (for Apple Silicon simulators)

The exact path might vary based on your build configuration and the specific target you are building for.
