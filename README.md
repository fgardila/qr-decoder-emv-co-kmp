# EMV Decoder Project

This project is a Kotlin Multiplatform (KMP) application designed to decode EMV QR codes. It provides shared business logic in the `emvdecoder` module and platform-specific implementations in the `androidApp` and `iosApp`.

The `emvdecoder` module contains the core logic for parsing and interpreting EMV QR code data.

The project follows the [recommended KMP project structure](https://kotlinlang.org/docs/multiplatform/multiplatform-project-recommended-structure.html) (separate entry-point modules per platform, shared KMP library) and builds with **Gradle 9.5.1**, **AGP 9.2.1** (`com.android.kotlin.multiplatform.library` plugin) and **Kotlin 2.3.21**. JDK 17+ is required to run the build.

## Building

### Android AAR

To generate the Android AAR library from the `emvdecoder` module, run the following command in the root of the project:

```bash
./gradlew :emvdecoder:assemble
```

The AAR file will be located at `emvdecoder/build/outputs/aar/emvdecoder.aar`.

> Note: the Android-KMP library plugin has a single build variant, so there is no `assembleRelease`/`assembleDebug` distinction for the library anymore.

### iOS Framework

To generate the iOS Framework (`emvdecoder.framework`) from the `emvdecoder` module, run:

```bash
./gradlew :emvdecoder:linkReleaseFrameworkIosArm64          # device
./gradlew :emvdecoder:linkDebugFrameworkIosSimulatorArm64   # Apple Silicon simulator
```

The frameworks are generated under:
* `emvdecoder/build/bin/iosArm64/releaseFramework/emvdecoder.framework`
* `emvdecoder/build/bin/iosSimulatorArm64/debugFramework/emvdecoder.framework`

Supported iOS targets are `iosArm64` and `iosSimulatorArm64` (Intel simulators / `iosX64` are no longer built, matching the current KMP template).

The `iosApp` Xcode project consumes the framework through the `embedAndSignAppleFrameworkForXcode` Gradle task in its build phase.

### Android app

```bash
./gradlew :androidApp:assembleDebug
```

## Testing

```bash
./gradlew :emvdecoder:allTests                 # all targets (Android host + iOS simulator)
./gradlew :emvdecoder:testAndroidHostTest      # Android/JVM unit tests only
./gradlew :emvdecoder:iosSimulatorArm64Test    # iOS simulator tests only
```

Common tests live in `emvdecoder/src/commonTest` and run on every target; Android-specific host tests live in `emvdecoder/src/androidHostTest`.
