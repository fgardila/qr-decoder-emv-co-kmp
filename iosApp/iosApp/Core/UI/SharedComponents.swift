import SwiftUI

/// Fila etiqueta/valor para los campos decodificados, con copiado al portapapeles.
struct KeyValueRow: View {
    let label: String
    let value: String
    var monospace: Bool = false

    @State private var justCopied = false

    var body: some View {
        HStack(alignment: .center, spacing: 8) {
            VStack(alignment: .leading, spacing: 2) {
                Text(label)
                    .font(.caption)
                    .foregroundStyle(.secondary)
                Text(value)
                    .font(monospace ? .body.monospaced() : .body)
            }
            .frame(maxWidth: .infinity, alignment: .leading)

            Button {
                UIPasteboard.general.string = value
                withAnimation { justCopied = true }
                Task {
                    try? await Task.sleep(nanoseconds: 1_500_000_000)
                    withAnimation { justCopied = false }
                }
            } label: {
                Image(systemName: justCopied ? "checkmark" : "doc.on.doc")
                    .font(.footnote)
                    .foregroundStyle(justCopied ? AnyShapeStyle(.green) : AnyShapeStyle(.tertiary))
            }
            .buttonStyle(.borderless)
            .accessibilityLabel("Copiar valor")
        }
    }
}

/// Estado vacío con ícono y mensaje centrados.
struct EmptyStateView: View {
    let systemImage: String
    let message: String

    var body: some View {
        VStack(spacing: 16) {
            Image(systemName: systemImage)
                .font(.system(size: 48))
                .foregroundStyle(.tertiary)
            Text(message)
                .font(.subheadline)
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)
        }
        .frame(maxWidth: .infinity)
        .padding(32)
    }
}
