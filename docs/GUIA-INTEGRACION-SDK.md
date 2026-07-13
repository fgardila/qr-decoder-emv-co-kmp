# Guía de integración del SDK de lectura de QR — code93

Instrucciones de implementación del SDK de **code93** para lectura y decodificación de códigos QR de pago del estándar colombiano **EASPBV v1.4-2025** (Redeban, Credibanco, Bre-B), con dos escenarios de ejemplo:

| App consumidora | Plataformas | Modalidad |
|---|---|---|
| `com.banco.colombia` | 2 apps Android nativas + 1 app iOS | **Con UI del SDK** (pantalla de escaneo lista: cámara + linterna + galería) |
| `com.cooperativa.bogota` | Android / iOS | **Sin UI (headless)**: interfaz gráfica propia, el SDK solo lee y decodifica |

> **Versionado**: los tres artefactos comparten una versión única (un solo proyecto, una sola versión). Disponibles en Maven Central a partir de la **2.1.0**.

---

## 1. Componentes del SDK

El SDK son tres artefactos independientes; cada app integra solo los que necesita:

| Artefacto | Coordenada Maven | Qué hace | Quién lo usa |
|---|---|---|---|
| **emvdecoder** | `dev.code93:emvdecoder:2.1.0` | Decodifica el **raw text** de un QR al modelo tipado del estándar EASPBV v1.4 (TLV, CRC, diagnósticos). No usa cámara ni imágenes. | Ambas apps |
| **qrscanner-core** | `dev.code93:qrscanner-core:2.1.0` | **Headless**: decodifica una **imagen** (bytes PNG/JPEG de galería, screenshot, archivo) a raw text. ML Kit en Android, Vision en iOS. | Cooperativa (y lo usa internamente la UI) |
| **qrscanner-compose** | `dev.code93:qrscanner-compose:2.1.0` | Pantalla completa de escaneo (`QrScannerScreen`): cámara en vivo, **botón de linterna/flash**, **botón de galería**, gestión del permiso de cámara y textos en español. En iOS se entrega dentro del framework **`QrdKit`**. | Banco |

Cadena típica de uso: **imagen o cámara → raw text → `EmvQr` → datos tipados**.

---

## 2. Requisitos mínimos

### Android (ambos escenarios)

| Requisito | Valor |
|---|---|
| `minSdk` | **28** (Android 9.0) |
| `compileSdk` / `targetSdk` recomendado | 36 |
| JDK | 17+ |
| Kotlin | 2.1+ (recomendado 2.3.x) |
| Jetpack Compose | requerido **solo** para `qrscanner-compose` (la app del banco debe ser Compose o alojar un `ComposeView`) |
| Google Play Services | no requerido como app aparte: ML Kit barcode se embebe vía la dependencia (modelo *bundled*) |

Permiso en `AndroidManifest.xml` (lo aporta `qrscanner-compose` por *manifest merge*; declararlo explícito si solo usan `qrscanner-core` con cámara propia):

```xml
<uses-feature android:name="android.hardware.camera" android:required="false" />
<uses-permission android:name="android.permission.CAMERA" />
```

La galería usa el **Photo Picker** del sistema: **no** requiere permisos de almacenamiento/fotos.

### iOS (ambos escenarios)

| Requisito | Valor |
|---|---|
| iOS mínimo | **16.0** |
| Xcode | 16+ |
| Distribución | `QrdKit.xcframework` (incluye `qrscanner-compose` + `qrscanner-core` + `emvdecoder` en un único framework estático — **no** enlazar además un framework `emvdecoder` separado: duplicaría el runtime de Kotlin) |

`Info.plist` (obligatorio para la pantalla con cámara del banco; innecesario si la cooperativa solo decodifica imágenes):

```xml
<key>NSCameraUsageDescription</key>
<string>La cámara se usa únicamente para escanear códigos QR de pago.</string>
```

La galería en iOS usa `PHPicker`/`PhotosPicker` (out-of-process): **no** requiere `NSPhotoLibraryUsageDescription`.

---

## 3. Qué retorna el SDK

### 3.1 Lectura (cámara / imagen) → raw text

| API | Escaneo correcto | Escaneo incorrecto |
|---|---|---|
| `QrScannerScreen` / `qrScannerViewController` (UI) — cámara | Invoca `onResult(rawText)` **una sola vez** con el contenido crudo del QR (`String`) y no vuelve a emitir. | No emite nada: la cámara sigue escaneando hasta detectar un QR o hasta que el usuario cierre (`onClose`). Si el permiso de cámara se deniega, muestra su propia pantalla explicativa con botón para reintentar. |
| `QrScannerScreen` (UI) — botón de galería | Invoca `onResult(rawText)` igual que la cámara. | Muestra dentro de la misma pantalla el mensaje *"No se encontró un código QR en la imagen seleccionada."* durante ~3 s y **no** invoca `onResult`. |
| `QrImageScanner.scan(imageBytes)` (headless, `suspend`) | Retorna el raw text (`String`). | Retorna **`null`** (imagen sin QR, ilegible o bytes que no son una imagen). Nunca lanza excepción. |

> El raw text es el contenido literal del QR (ej. `00020101021126310014CO.COM.RBM.LLA0409@ocfrf115...`). La lectura **no** valida que sea un QR de pago: eso es el paso siguiente.

### 3.2 Decodificación EMV (raw text → datos)

```kotlin
// 1. Validación de integridad (opcional pero recomendada): CRC-16/CCITT-FALSE
EmvQr.isCrcValid(rawText)          // Boolean

// 2. Decodificación al modelo EASPBV v1.4
val data = EmvQr.decode(rawText)   // QRCodeEmvCoColombiaData

// 3. Variante con observabilidad del parseo
val result = EmvQr.decodeWithDiagnostics(rawText)
result.data                        // los mismos datos
result.diagnostics.isFullyParsed   // ¿se consumió todo el payload?
result.diagnostics.parsedTagCount
result.diagnostics.consumedChars / result.diagnostics.totalChars
```

| Situación | Comportamiento |
|---|---|
| QR EASPBV válido | `decode` retorna `QRCodeEmvCoColombiaData` con las secciones tipadas: convenciones, información del comercio (llaves Bre-B, red adquirente), información adicional (canal, ID de transacción — tag `90` con fallback al legado `86`), impuestos IVA/INC, detalle de la transacción (valor, moneda, país), campos de transferencias/recaudos, plantilla de idioma. Los campos ausentes en el QR quedan en `null`. |
| CRC inválido | `isCrcValid` retorna `false`. `decode` **igualmente funciona** (el SDK es tolerante); la app decide si rechaza el QR. Recomendación para pagos: rechazarlo. |
| Payload malformado o no-EMV | **Nunca lanza excepción**: el parser extrae lo que puede y se detiene en el primer elemento inválido. Un texto arbitrario produce un objeto con campos `null` y `diagnostics.isFullyParsed == false`. |
| Diagnóstico | `decodeWithDiagnostics` reporta cuántos tags se parsearon y cuántos caracteres se consumieron — útil para logs y soporte en campo. |

**Regla de oro para ambas apps**: la lectura entrega texto; la app valida CRC y luego decodifica. La conformidad final con el estándar la decide el backend autorizador.

---

## 4. Escenario A — `com.banco.colombia` (con la UI del SDK)

El banco quiere ahorrar desarrollo: usa la pantalla de escaneo completa del SDK (cámara + linterna + galería + permisos + textos en español) en sus 2 apps Android y su app iOS.

### 4.1 Apps Android (×2) — paso a paso

**Paso 1 — Dependencias** (`app/build.gradle.kts` de cada una de las 2 apps):

```kotlin
dependencies {
    implementation("dev.code93:qrscanner-compose:2.1.0") // pantalla de escaneo (trae qrscanner-core transitivo)
    implementation("dev.code93:emvdecoder:2.1.0")        // decodificador EASPBV
}
```

**Paso 2 — Permisos**: nada que hacer. El manifest de `qrscanner-compose` ya declara `CAMERA` y se fusiona con el de la app. El permiso se solicita en runtime dentro de la propia pantalla.

**Paso 3 — Mostrar la pantalla** (destino de navegación, `Activity` propia o `fullscreen dialog`):

```kotlin
import dev.code93.qrscanner.compose.QrScannerScreen

@Composable
fun EscanearPagoScreen(onQrLeido: (String) -> Unit, onCerrar: () -> Unit) {
    QrScannerScreen(
        onResult = onQrLeido,          // raw text, se invoca UNA sola vez
        onClose = onCerrar,
        showGalleryButton = true       // false si la app ya ofrece su propio picker
    )
}
```

La pantalla incluye: marco de escaneo, botón **linterna** (enciende/apaga el flash del dispositivo), botón **galería** (Photo Picker → decodifica la imagen), botón cerrar y pantalla de permiso denegado.

**Paso 4 — Procesar el resultado**:

```kotlin
fun onQrLeido(rawText: String) {
    if (!EmvQr.isCrcValid(rawText)) {
        mostrarError("El código QR no es un QR de pago válido (CRC inválido).")
        return
    }
    val data = EmvQr.decode(rawText)
    val valor = data.transactionDetailData?.transactionValue      // ej. "15000.00"
    val comercio = data.merchantInformationData?.merchantName
    val llaveBreB = data.merchantInformationData
        ?.immediatePaymentKey?.get(ImmediatePaymentKeyType.ALPHANUMERIC_DATA)
    // → flujo de confirmación de pago del banco
}
```

**Paso 5 — Verificación**: probar en **dispositivo físico** (la linterna no existe en el emulador): QR EASPBV real con linterna encendida/apagada, importación desde galería y un QR no-pago (debe fallar el CRC).

### 4.2 App iOS — paso a paso

**Paso 1 — Agregar el framework**: arrastrar `QrdKit.xcframework` al proyecto (o vía Swift Package Manager si se distribuye así) y marcarlo *Embed & Sign* en el target `com.banco.colombia`. No agregar ningún otro framework Kotlin.

**Paso 2 — `Info.plist`**: agregar `NSCameraUsageDescription` (ver §2).

**Paso 3 — Envolver la pantalla para SwiftUI**:

```swift
import SwiftUI
import QrdKit

struct EscanerQrView: UIViewControllerRepresentable {
    let onResult: (String) -> Void
    let onClose: () -> Void

    func makeUIViewController(context: Context) -> UIViewController {
        QrScannerViewControllerKt.qrScannerViewController(
            onResult: onResult,
            onClose: onClose
        )
    }
    func updateUIViewController(_ vc: UIViewController, context: Context) {}
}
```

**Paso 4 — Presentarla y procesar**:

```swift
.fullScreenCover(isPresented: $mostrarEscaner) {
    EscanerQrView(
        onResult: { rawText in
            mostrarEscaner = false
            guard EmvQr.shared.isCrcValid(rawText: rawText) else {
                mostrarError("QR de pago inválido (CRC)")
                return
            }
            let data = EmvQr.shared.decode(rawText: rawText)
            let valor = data.transactionDetailData?.transactionValue
            // → flujo de pago
        },
        onClose: { mostrarEscaner = false }
    )
    .ignoresSafeArea()
}
```

**Paso 5 — Verificación**: dispositivo físico (linterna y cámara no funcionan en el simulador).

> Nota de tamaño: `QrdKit` embebe el runtime de Compose Multiplatform (~15–25 MB adicionales en el binario). Es el costo de la UI lista para usar.

---

## 5. Escenario B — `com.cooperativa.bogota` (sin UI, headless)

La cooperativa tiene su propio diseño de interfaz. El SDK aporta solo la inteligencia: **imagen → raw text** (`qrscanner-core`) y **raw text → datos EASPBV** (`emvdecoder`). La cámara en vivo la implementa la cooperativa con su stack preferido (CameraX/ML Kit en Android, AVFoundation en iOS) y le pasa al SDK el texto o la imagen obtenida.

### 5.1 Android — paso a paso

**Paso 1 — Dependencias** (sin Compose, sin UI del SDK):

```kotlin
dependencies {
    implementation("dev.code93:qrscanner-core:2.1.0")   // imagen → raw text
    implementation("dev.code93:emvdecoder:2.1.0")       // raw text → datos
}
```

**Paso 2 — Permisos**: solo si la cooperativa implementa cámara propia debe declarar y pedir `CAMERA` ella misma. Para decodificar imágenes de galería con Photo Picker no se necesita ningún permiso.

**Paso 3 — Decodificar una imagen (galería, archivo, screenshot)**:

```kotlin
import dev.code93.qrscanner.core.QrImageScanner

val scanner = QrImageScanner()   // inyectable vía la interfaz QrImageScanning

suspend fun leerQrDeImagen(context: Context, uri: Uri): String? =
    withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(uri)
            ?.use { it.readBytes() }
            ?.let { scanner.scan(it) }   // null = la imagen no contiene un QR legible
    }
```

**Paso 4 — Cámara propia**: el analizador de la cooperativa (p. ej. `MlKitAnalyzer` de CameraX) entrega el `rawValue` del QR directamente; ese `String` pasa al paso 5 sin tocar `qrscanner-core`. La linterna se controla con su propio stack (p. ej. `LifecycleCameraController.enableTorch(true)`).

**Paso 5 — Validar y decodificar** (idéntico para cámara y galería):

```kotlin
sealed interface ResultadoQr {
    data class Pago(val data: QRCodeEmvCoColombiaData) : ResultadoQr
    object NoEsQr : ResultadoQr          // scan() == null
    object CrcInvalido : ResultadoQr
}

fun procesar(rawText: String?): ResultadoQr = when {
    rawText == null -> ResultadoQr.NoEsQr
    !EmvQr.isCrcValid(rawText) -> ResultadoQr.CrcInvalido
    else -> ResultadoQr.Pago(EmvQr.decode(rawText))
}
```

### 5.2 iOS — paso a paso

**Paso 1 — Framework**: mismo `QrdKit.xcframework` (*Embed & Sign*). La cooperativa simplemente **no usa** las clases de UI; solo `QrImageScanner` y `EmvQr`.

**Paso 2 — Permisos**: `NSCameraUsageDescription` solo si implementan cámara propia. Nada para galería con `PhotosPicker`.

**Paso 3 — Decodificar una imagen** (el `suspend` de Kotlin se expone como `async` en Swift; acepta `NSData` directamente):

```swift
import QrdKit

func leerQrDeImagen(_ image: UIImage) async -> String? {
    guard let data = image.jpegData(compressionQuality: 0.9) else { return nil }
    return try? await QrImageScanner().scan(data: data)   // nil = sin QR legible
}
```

**Paso 4 — Cámara propia**: `AVCaptureMetadataOutput` con `.qr` entrega el `stringValue`; la linterna con `AVCaptureDevice.torchMode`. Ese `String` va directo al paso 5.

**Paso 5 — Validar y decodificar**:

```swift
guard let rawText else { return .noEsQr }
guard EmvQr.shared.isCrcValid(rawText: rawText) else { return .crcInvalido }
let data = EmvQr.shared.decode(rawText: rawText)
// data.transactionDetailData?.transactionValue, data.merchantInformationData?..., etc.
```

---

## 6. Resumen de contratos y errores

| Paso | Éxito | Fallo | ¿Lanza excepción? |
|---|---|---|---|
| UI cámara (`onResult`) | `String` raw text, una sola vez | No emite; sigue escaneando o el usuario cierra | No |
| UI galería | `String` raw text vía `onResult` | Mensaje en pantalla, sin callback | No |
| `QrImageScanner.scan(...)` | `String` raw text | `null` | No |
| `EmvQr.isCrcValid(...)` | `true` | `false` | No |
| `EmvQr.decode(...)` | `QRCodeEmvCoColombiaData` (campos ausentes = `null`) | Objeto parcial/vacío; usar `decodeWithDiagnostics` para saber cuánto se parseó | No — tolerante por diseño |

**Recomendaciones finales para ambas apps**
- Rechazar el pago si `isCrcValid == false`; registrar `diagnostics` cuando `isFullyParsed == false`.
- Probar siempre en dispositivo físico: linterna, cámara y rendimiento de ML Kit/Vision no son representativos en emulador/simulador.
- Incluir en las pruebas un QR EASPBV largo (>300 caracteres, con tags anidados `26`, `62`, `90`) y un QR no-pago (URL) para verificar los caminos de error.
- Documentación de la API del decodificador: https://fgardila.github.io/qr-decoder-emv-co-kmp/
