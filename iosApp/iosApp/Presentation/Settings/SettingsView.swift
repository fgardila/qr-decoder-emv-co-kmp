import SwiftUI

struct SettingsView: View {

    private var appVersion: String {
        let version = Bundle.main.object(forInfoDictionaryKey: "CFBundleShortVersionString") as? String ?? "—"
        let build = Bundle.main.object(forInfoDictionaryKey: "CFBundleVersion") as? String ?? "—"
        return "\(version) (\(build))"
    }

    var body: some View {
        NavigationStack {
            List {
                Section("Desarrollador") {
                    HStack(spacing: 12) {
                        Image(systemName: "person.crop.circle.fill")
                            .font(.largeTitle)
                            .foregroundStyle(.tint)
                        VStack(alignment: .leading) {
                            Text("Fabian Ardila")
                                .font(.headline)
                            Text("Senior Mobile Developer · Android & iOS")
                                .font(.footnote)
                                .foregroundStyle(.secondary)
                        }
                    }
                    LinkRow(
                        title: "Sitio web — code93.dev",
                        systemImage: "globe",
                        url: "https://www.code93.dev"
                    )
                }

                Section("Enlaces") {
                    LinkRow(
                        title: "Código fuente en GitHub",
                        systemImage: "chevron.left.forwardslash.chevron.right",
                        url: "https://github.com/fgardila/qr-decoder-emv-co-kmp"
                    )
                    LinkRow(
                        title: "Documentación de la API (Dokka)",
                        systemImage: "doc.text",
                        url: "https://fgardila.github.io/qr-decoder-emv-co-kmp/"
                    )
                    LinkRow(
                        title: "Librería emvdecoder en Maven Central",
                        systemImage: "shippingbox",
                        url: "https://central.sonatype.com/artifact/dev.code93/emvdecoder"
                    )
                }

                Section("Acerca de la app") {
                    NavigationLink {
                        LicensesView()
                    } label: {
                        Label("Licencias de software", systemImage: "checkmark.shield")
                    }
                    HStack {
                        Label("Versión", systemImage: "info.circle")
                        Spacer()
                        Text(appVersion)
                            .foregroundStyle(.secondary)
                    }
                }
            }
            .navigationTitle("Ajustes")
        }
    }
}

private struct LinkRow: View {
    let title: String
    let systemImage: String
    let url: String

    var body: some View {
        if let destination = URL(string: url) {
            Link(destination: destination) {
                HStack {
                    Label(title, systemImage: systemImage)
                    Spacer()
                    Image(systemName: "arrow.up.right")
                        .font(.footnote)
                        .foregroundStyle(.tertiary)
                }
            }
            .foregroundStyle(.primary)
        }
    }
}
