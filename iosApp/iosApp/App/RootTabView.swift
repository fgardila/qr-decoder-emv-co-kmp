import SwiftUI

struct RootTabView: View {
    let container: AppContainer

    var body: some View {
        TabView {
            ScannerView(
                viewModel: ScannerViewModel(
                    decoder: container.emvDecoder,
                    imageDecoder: container.qrImageDecoder
                )
            )
            .tabItem {
                Label("Escáner", systemImage: "qrcode.viewfinder")
            }

            GenerateQrView(
                viewModel: GenerateQrViewModel(generator: container.qrGenerator)
            )
            .tabItem {
                Label("Generar", systemImage: "qrcode")
            }

            SettingsView()
                .tabItem {
                    Label("Ajustes", systemImage: "gearshape")
                }
        }
    }
}
