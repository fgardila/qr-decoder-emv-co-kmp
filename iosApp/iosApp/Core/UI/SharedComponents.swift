import SwiftUI

/// Fila etiqueta/valor para los campos decodificados.
struct KeyValueRow: View {
    let label: String
    let value: String
    var monospace: Bool = false

    var body: some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(label)
                .font(.caption)
                .foregroundStyle(.secondary)
            Text(value)
                .font(monospace ? .body.monospaced() : .body)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
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
