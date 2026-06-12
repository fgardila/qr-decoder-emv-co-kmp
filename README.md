# EMV QR Decoder — Colombia (Kotlin Multiplatform)

[![CI](https://github.com/fgardila/qr-decoder-emv-co-kmp/actions/workflows/ci.yml/badge.svg)](https://github.com/fgardila/qr-decoder-emv-co-kmp/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/dev.code93/emvdecoder)](https://central.sonatype.com/artifact/dev.code93/emvdecoder)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.21-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![API Docs](https://img.shields.io/badge/API%20Docs-Dokka-blue)](https://fgardila.github.io/qr-decoder-emv-co-kmp/)

> 🇪🇸 [Versión en español](README.es.md)

A Kotlin Multiplatform library that parses **EMVCo Merchant-Presented QR codes** following the **Colombian industry standard (EASPBV v1.4-2025)** — the spec behind QR payments on networks like Redeban, Credibanco and the Bre-B instant payment system.

It ships as an **Android AAR** and an **iOS Framework** from a single Kotlin codebase, with native demo apps (Jetpack Compose and SwiftUI) that scan and decode real payment QRs.

## Features

- **TLV parsing** of the full EMVCo payload (tag / 2-digit length / value), including nested templates.
- **Complete EASPBV v1.4 coverage**: immediate-payment keys (Llaves / Bre-B), acquirer network, merchant codes, IVA/INC taxes, channel, transaction ID (tag `90`, with legacy tag `86` fallback), discounts, transfers/collections fields, and alternate-language merchant info.
- **CRC-16/CCITT-FALSE validation** (`CRCValidator`) as a separate, opt-in step.
- **Lenient by design**: malformed input never throws — the parser extracts everything it can and stops at the first invalid element. Standard compliance is the authorizing backend's job, not the client's.
- **No dependencies** in the shared module. Pure Kotlin `commonMain`.

## Usage

### Kotlin (Android / JVM)

```kotlin
val rawText = "00020101021126310014CO.COM.RBM.LLA0409@ocfrf115..." // from your QR scanner

if (CRCValidator.validate(rawText)) {
    val data = EmvQrCodeDecoder(rawText).decode()

    data.transactionDetailData?.transactionValue   // "15000.00"
    data.additionalMerchantInformationData?.transactionId
    data.merchantInformationData?.immediatePaymentKey
        ?.get(ImmediatePaymentKeyType.ALPHANUMERIC_DATA) // "@ocfrf115"
}
```

### Swift (iOS)

```swift
import emvdecoder

let rawText = "00020101021126310014CO.COM.RBM.LLA0409@ocfrf115..." // from your QR scanner

if CRCValidator.Companion().validate(qrCode: rawText) {
    let data = EmvQrCodeDecoder(qrCode: rawText).decode()
    let amount = data.transactionDetailData?.transactionValue
}
```

## Supported fields (EASPBV v1.4-2025)

| Spec section | Tags | Decoded into |
|---|---|---|
| I. Conventions | `00`, `01`, `63`, `91` | `ConventionsQrCodeEmvCoData` |
| II. Merchant information | `26` (subs 00–05), `49`, `50`, `51` | `MerchantInformationData` |
| III. Additional merchant info | `52`, `58`–`61`, `80`–`85`, `90` | `AdditionalMerchantInformationData` |
| IV. Other transactions | `92`–`98`, `99` (subs 00–06) | `OtherTransactionsFieldsData` |
| V. Language template | `64` (subs 00–02) | `MerchantInformationLanguageData` |
| VI. Additional data field | `62` (subs 01–11) | `MerchantAdditionalFieldsData` |
| VII. Transaction detail | `53`–`57` | `TransactionDetailData` |

The full specification PDF (`EASPBV-Campos-QRCode-EMVCo-Industria-v1.4-2025.pdf`) is included in the repository.

📖 **[API documentation (Dokka)](https://fgardila.github.io/qr-decoder-emv-co-kmp/)** — every field documents its tag, sub-tag and the semantic values defined by the spec.

## Installation

The library is published to **Maven Central**:

```kotlin
// Android / JVM (or commonMain of your own KMP project)
dependencies {
    implementation("dev.code93:emvdecoder:1.0.0")
}
```

For iOS you can also build the framework from source:

```bash
./gradlew :emvdecoder:linkReleaseFrameworkIosArm64           # iOS device framework
./gradlew :emvdecoder:linkDebugFrameworkIosSimulatorArm64    # iOS simulator framework → build/bin/
```

## Project structure

Follows the [recommended KMP project structure](https://kotlinlang.org/docs/multiplatform/multiplatform-project-recommended-structure.html) with **Gradle 9.5.1**, **AGP 9.2.1** (`com.android.kotlin.multiplatform.library` plugin) and **Kotlin 2.3.21**. JDK 17+ required.

```
emvdecoder/   Kotlin Multiplatform library (commonMain: all parsing logic)
androidApp/   Demo app — Jetpack Compose + CameraX + ML Kit scanning
iosApp/       Demo app — SwiftUI (links the framework via embedAndSignAppleFrameworkForXcode)
```

## Testing

```bash
./gradlew :emvdecoder:allTests                 # all targets (Android host + iOS simulator)
./gradlew :emvdecoder:testAndroidHostTest      # Android/JVM unit tests only
./gradlew :emvdecoder:iosSimulatorArm64Test    # iOS simulator tests only
```

The suite includes real Redeban QR samples, full-coverage synthetic QRs built with a TLV/CRC helper, malformed-input cases, and the standard CRC-16/CCITT-FALSE test vector (`"123456789" → 0x29B1`).

## Contributing

Issues and PRs are welcome (English or Spanish) — see [CONTRIBUTING.md](CONTRIBUTING.md). Changes are tracked in the [CHANGELOG](CHANGELOG.md).

## License

[MIT](LICENSE). The CRC validation logic is derived from [emv_qrcode](https://github.com/mohamedayed/emv_qrcode) by Mohamed Ayed (MIT).
