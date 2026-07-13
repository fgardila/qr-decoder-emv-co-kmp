import PhotosUI
import SwiftUI

struct ScannerView: View {
    @StateObject var viewModel: ScannerViewModel
    @State private var selectedItem: PhotosPickerItem?

    init(viewModel: ScannerViewModel) {
        _viewModel = StateObject(wrappedValue: viewModel)
    }

    var body: some View {
        NavigationStack {
            content
                .navigationTitle("Escanear código QR")
                .fullScreenCover(isPresented: $viewModel.isCameraPresented) {
                    ComposeQrScannerView(
                        onResult: { rawText in
                            viewModel.isCameraPresented = false
                            viewModel.onQrScanned(rawText)
                        },
                        onClose: { viewModel.isCameraPresented = false }
                    )
                    .ignoresSafeArea()
                }
        }
    }

    @ViewBuilder
    private var content: some View {
        VStack(spacing: 0) {
            actionButtons
                .padding(.horizontal)
                .padding(.vertical, 12)

            switch viewModel.state {
            case .idle:
                Text("Decodifica códigos QR de pago del estándar colombiano EMVCo (EASPBV v1.4)")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
                    .padding(.horizontal)
                EmptyStateView(
                    systemImage: "qrcode.viewfinder",
                    message: "Escanea un QR con la cámara o selecciona una imagen para ver aquí los datos decodificados."
                )
                Spacer()

            case .decoding:
                Spacer()
                ProgressView("Decodificando…")
                Spacer()

            case .error(let noQrFound):
                EmptyStateView(
                    systemImage: "exclamationmark.magnifyingglass",
                    message: noQrFound
                        ? "No se encontró un código QR en la imagen seleccionada."
                        : "No fue posible procesar el código QR."
                )
                Spacer()

            case .success(let decoded):
                ResultSectionsView(decoded: decoded, onScanAgain: viewModel.onReset)
            }
        }
    }

    private var actionButtons: some View {
        HStack(spacing: 12) {
            Button {
                viewModel.isCameraPresented = true
            } label: {
                Label("Abrir cámara", systemImage: "camera")
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent)

            PhotosPicker(selection: $selectedItem, matching: .images) {
                Label("Galería", systemImage: "photo")
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.bordered)
            .onChange(of: selectedItem) { newItem in
                viewModel.onImageSelected(newItem)
                selectedItem = nil
            }
        }
    }
}
