# EMV QR Decoder — Colombia (Kotlin Multiplatform)

[![CI](https://github.com/fgardila/qr-decoder-emv-co-kmp/actions/workflows/ci.yml/badge.svg)](https://github.com/fgardila/qr-decoder-emv-co-kmp/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/dev.code93/emvdecoder)](https://central.sonatype.com/artifact/dev.code93/emvdecoder)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.21-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![API Docs](https://img.shields.io/badge/API%20Docs-Dokka-blue)](https://fgardila.github.io/qr-decoder-emv-co-kmp/)

> 🇬🇧 [English version](README.md)

Librería Kotlin Multiplatform que parsea **códigos QR EMVCo Merchant-Presented** según el **estándar de la industria colombiana (EASPBV v1.4-2025)** — la especificación detrás de los pagos QR en redes como Redeban, Credibanco y el sistema de pagos inmediatos Bre-B.

Se distribuye como **AAR para Android**, **Framework para iOS** y **jar JVM** (para backends Ktor/Spring) desde una única base de código Kotlin, con apps demo nativas (Jetpack Compose y SwiftUI) que escanean y decodifican QRs de pago reales.

## Características

- **Parsing TLV** del payload EMVCo completo (tag / longitud de 2 dígitos / valor), incluyendo templates anidados.
- **Cobertura completa de EASPBV v1.4**: llaves de pagos inmediatos (Bre-B), red adquirente, códigos de comercio, impuestos IVA/INC, canal, ID de transacción (tag `90`, con fallback legacy al tag `86`), descuentos, campos de transferencias/recaudos e información del comercio en idioma alternativo.
- **Punto de entrada único** (`EmvQr`): decodificación, validación CRC y diagnósticos de parsing tras una superficie de API mínima (`explicitApi` + ABI bloqueada). Los objetos de resultado son legibles pero el consumidor no puede construirlos ni copiarlos.
- **Validación CRC-16/CCITT-FALSE** (`EmvQr.isCrcValid`) como paso separado y opcional.
- **Laxa por diseño, observable a demanda**: una entrada malformada nunca lanza excepción — el parser extrae todo lo que puede y se detiene en el primer elemento inválido. `decodeWithDiagnostics` reporta cuánto del payload se consumió, para logging y soporte en campo. Verificar el cumplimiento del estándar es responsabilidad del backend autorizador, no del cliente.
- **Sin dependencias** en el módulo compartido. Kotlin puro en `commonMain`.

## Uso

### Kotlin (Android / JVM)

```kotlin
val rawText = "00020101021126310014CO.COM.RBM.LLA0409@ocfrf115..." // desde tu lector de QR

if (EmvQr.isCrcValid(rawText)) {
    val data = EmvQr.decode(rawText)

    data.transactionDetailData?.transactionValue   // "15000.00"
    data.additionalMerchantInformationData?.transactionId
    data.merchantInformationData?.immediatePaymentKey
        ?.get(ImmediatePaymentKeyType.ALPHANUMERIC_DATA) // "@ocfrf115"
}

// Observabilidad del parsing laxo (logging / soporte en campo):
val result = EmvQr.decodeWithDiagnostics(rawText)
if (!result.diagnostics.isFullyParsed) {
    log("QR parseado parcialmente: ${result.diagnostics.parsedTagCount} tags, " +
        "detenido en ${result.diagnostics.consumedChars}/${result.diagnostics.totalChars}")
}
```

### Swift (iOS)

```swift
import emvdecoder

let rawText = "00020101021126310014CO.COM.RBM.LLA0409@ocfrf115..." // desde tu lector de QR

if EmvQr.shared.isCrcValid(rawText: rawText) {
    let data = EmvQr.shared.decode(rawText: rawText)
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
    implementation("dev.code93:emvdecoder:2.0.0")
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
emvdecoder/         Librería Kotlin Multiplatform (commonMain: toda la lógica de parsing)
qrscanner-core/     Módulo KMP local: decodifica el QR de una imagen (bytes de galería) a
                    raw text vía KScan (ML Kit en Android, Vision en iOS)
qrscanner-compose/  Módulo Compose Multiplatform local: pantalla de escaneo compartida
                    (cámara KScan + linterna/flash + picker de galería Calf). Genera el
                    framework umbrella QrdKit para iOS (exporta emvdecoder + qrscanner-core)
androidApp/         "QR EMV Colombia" — Clean Architecture + MVVM + Hilt + Compose
                    Navigation; usa la pantalla de escaneo compartida, genera QRs de prueba
                    EASPBV y consume la librería desde Maven Central
iosApp/             "QR EMV Colombia" — SwiftUI + MVVM con capas limpias; embebe el escáner
                    Compose compartido y la decodificación de galería vía el framework QrdKit
                    (embedAndSignAppleFrameworkForXcode), generación con CoreImage
```

Ambas apps demo comparten el mismo producto: 3 pestañas (Escanear / Generar / Ajustes) que decodifican QRs de pago colombianos reales y generan QRs de prueba válidos según la spec (TLV + CRC-16/CCITT-FALSE construidos en la capa de dominio de cada app y verificados por round-trip contra esta librería).

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
