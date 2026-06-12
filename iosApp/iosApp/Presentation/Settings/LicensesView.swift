import SwiftUI

private struct LicenseEntry: Identifiable {
    let id = UUID()
    let name: String
    let license: String
    let url: String
}

private let licenses: [LicenseEntry] = [
    LicenseEntry(
        name: "emvdecoder (dev.code93)",
        license: "MIT License",
        url: "https://github.com/fgardila/qr-decoder-emv-co-kmp/blob/main/LICENSE"
    ),
    LicenseEntry(
        name: "Kotlin Multiplatform",
        license: "Apache License 2.0",
        url: "https://github.com/JetBrains/kotlin"
    )
]

struct LicensesView: View {
    var body: some View {
        List(licenses) { entry in
            if let url = URL(string: entry.url) {
                Link(destination: url) {
                    VStack(alignment: .leading, spacing: 2) {
                        Text(entry.name)
                            .foregroundStyle(.primary)
                        Text(entry.license)
                            .font(.footnote)
                            .foregroundStyle(.secondary)
                    }
                }
            }
        }
        .navigationTitle("Licencias")
        .navigationBarTitleDisplayMode(.inline)
    }
}
