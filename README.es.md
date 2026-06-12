# EMV QR Decoder — Colombia (Kotlin Multiplatform)

[![CI](https://github.com/fgardila/qr-decoder-emv-co-kmp/actions/workflows/ci.yml/badge.svg)](https://github.com/fgardila/qr-decoder-emv-co-kmp/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/dev.code93/emvdecoder)](https://central.sonatype.com/artifact/dev.code93/emvdecoder)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.21-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![API Docs](https://img.shields.io/badge/API%20Docs-Dokka-blue)](https://fgardila.github.io/qr-decoder-emv-co-kmp/)

> 🇬🇧 [English version](README.md)

Librería Kotlin Multiplatform que parsea **códigos QR EMVCo Merchant-Presented** según el **estándar de la industria colombiana (EASPBV v1.4-2025)** — la especificación detrás de los pagos QR en redes como Redeban, Credibanco y el sistema de pagos inmediatos Bre-B.

Se distribuye como **AAR para Android** y **Framework para iOS** desde una única base de código Kotlin, con apps demo nativas (Jetpack Compose y SwiftUI) que escanean y decodifican QRs de pago reales.

## Características

- **Parsing TLV** del payload EMVCo completo (tag / longitud de 2 dígitos / valor), incluyendo templates anidados.
- **Cobertura completa de EASPBV v1.4**: llaves de pagos inmediatos (Bre-B), red adquirente, códigos de comercio, impuestos IVA/INC, canal, ID de transacción (tag `90`, con fallback legacy al tag `86`), descuentos, campos de transferencias/recaudos e información del comercio en idioma alternativo.
- **Validación CRC-16/CCITT-FALSE** (`CRCValidator`) como paso separado y opcional.
- **Laxa por diseño**: una entrada malformada nunca lanza excepción — el parser extrae todo lo que puede y se detiene en el primer elemento inválido. Verificar el cumplimiento del estándar es responsabilidad del backend autorizador, no del cliente.
- **Sin dependencias** en el módulo compartido. Kotlin puro en `commonMain`.

## Uso

### Kotlin (Android / JVM)

```kotlin
val rawText = "00020101021126310014CO.COM.RBM.LLA0409@ocfrf115..." // desde tu lector de QR

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

let rawText = "00020101021126310014CO.COM.RBM.LLA0409@ocfrf115..." // desde tu lector de QR

if CRCValidator.Companion().validate(qrCode: rawText) {
    let data = EmvQrCodeDecoder(qrCode: rawText).decode()
    let amount = data.transactionDetailData?.transactionValue
}
```

## Campos soportados (EASPBV v1.4-2025)

| Sección de la spec | Tags | Decodificado en |
|---|---|---|
| I. Convenciones | `00`, `01`, `63`, `91` | `ConventionsQrCodeEmvCoData` |
| II. Información del comercio | `26` (subs 00–05), `49`, `50`, `51` | `MerchantInformationData` |
| III. Info adicional del comercio | `52`, `58`–`61`, `80`–`85`, `90` | `AdditionalMerchantInformationData` |
| IV. Otras transacciones | `92`–`98`, `99` (subs 00–06) | `OtherTransactionsFieldsData` |
| V. Template de idioma | `64` (subs 00–02) | `MerchantInformationLanguageData` |
| VI. Campos adicionales | `62` (subs 01–11) | `MerchantAdditionalFieldsData` |
| VII. Detalle de la transacción | `53`–`57` | `TransactionDetailData` |

El PDF completo de la especificación (`EASPBV-Campos-QRCode-EMVCo-Industria-v1.4-2025.pdf`) está incluido en el repositorio.

📖 **[Documentación de la API (Dokka)](https://fgardila.github.io/qr-decoder-emv-co-kmp/)** — cada campo documenta su tag, sub-tag y los valores semánticos que define la spec.

## Instalación

La librería está publicada en **Maven Central**:

```kotlin
// Android / JVM (o el commonMain de tu propio proyecto KMP)
dependencies {
    implementation("dev.code93:emvdecoder:1.0.0")
}
```

Para iOS también puedes construir el framework desde el código fuente:

```bash
./gradlew :emvdecoder:linkReleaseFrameworkIosArm64           # framework iOS dispositivo
./gradlew :emvdecoder:linkDebugFrameworkIosSimulatorArm64    # framework iOS simulador → build/bin/
```

## Estructura del proyecto

Sigue la [estructura recomendada de proyectos KMP](https://kotlinlang.org/docs/multiplatform/multiplatform-project-recommended-structure.html) con **Gradle 9.5.1**, **AGP 9.2.1** (plugin `com.android.kotlin.multiplatform.library`) y **Kotlin 2.3.21**. Requiere JDK 17+.

```
emvdecoder/   Librería Kotlin Multiplatform (commonMain: toda la lógica de parsing)
androidApp/   App demo — Jetpack Compose + CameraX + ML Kit
iosApp/       App demo — SwiftUI (enlaza el framework vía embedAndSignAppleFrameworkForXcode)
```

## Tests

```bash
./gradlew :emvdecoder:allTests                 # todos los targets (Android host + simulador iOS)
./gradlew :emvdecoder:testAndroidHostTest      # solo unit tests Android/JVM
./gradlew :emvdecoder:iosSimulatorArm64Test    # solo tests del simulador iOS
```

La suite incluye QRs reales de Redeban, QRs sintéticos de cobertura completa construidos con un helper TLV/CRC, casos de entrada malformada y el vector estándar CRC-16/CCITT-FALSE (`"123456789" → 0x29B1`).

## Contribuir

Issues y PRs son bienvenidos (en español o inglés) — mira [CONTRIBUTING.md](CONTRIBUTING.md). Los cambios se registran en el [CHANGELOG](CHANGELOG.md).

## Licencia

[MIT](LICENSE). La lógica de validación CRC deriva de [emv_qrcode](https://github.com/mohamedayed/emv_qrcode) de Mohamed Ayed (MIT).
