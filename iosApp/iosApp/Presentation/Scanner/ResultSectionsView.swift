import SwiftUI

/// Resultado decodificado: badges de integridad/diagnósticos + secciones.
struct ResultSectionsView: View {
    let decoded: DecodedQr
    let onScanAgain: () -> Void

    var body: some View {
        List {
            Section {
                HStack(spacing: 8) {
                    Label(
                        decoded.crcValid ? "CRC válido" : "CRC inválido",
                        systemImage: decoded.crcValid ? "checkmark.seal.fill" : "exclamationmark.triangle.fill"
                    )
                    .font(.footnote)
                    .foregroundStyle(decoded.crcValid ? Color.green : Color.orange)

                    Spacer()

                    Text(diagnosticsLabel)
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                }
                Button("Escanear otro", systemImage: "arrow.counterclockwise", action: onScanAgain)
                    .font(.footnote)
            }

            ForEach(decoded.sections) { section in
                Section(section.title) {
                    ForEach(section.fields) { field in
                        KeyValueRow(
                            label: field.label,
                            value: field.value,
                            monospace: field.label == "CRC" || field.label.hasPrefix("GUID")
                        )
                    }
                }
            }
        }
        .listStyle(.insetGrouped)
    }

    private var diagnosticsLabel: String {
        let diagnostics = decoded.diagnostics
        if diagnostics.isFullyParsed {
            return "\(diagnostics.parsedTagCount) tags · parse completo"
        }
        return "\(diagnostics.parsedTagCount) tags · parcial (\(diagnostics.consumedChars)/\(diagnostics.totalChars))"
    }
}
