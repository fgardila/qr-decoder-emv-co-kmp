import SwiftUI

struct GenerateQrView: View {
    @StateObject var viewModel: GenerateQrViewModel

    init(viewModel: GenerateQrViewModel) {
        _viewModel = StateObject(wrappedValue: viewModel)
    }

    var body: some View {
        NavigationStack {
            Form {
                Section {
                    Text("Crea un QR estático según el estándar EASPBV v1.4 para pruebas de integración")
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                }

                Section("Red adquirente") {
                    Picker("Red", selection: $viewModel.network) {
                        ForEach(PaymentNetwork.allCases) { network in
                            Text(network.displayName).tag(network)
                        }
                    }
                    .pickerStyle(.segmented)
                }

                Section("Identificación del comercio") {
                    Picker("Tipo de llave", selection: $viewModel.keyType) {
                        ForEach(PaymentKeyType.allCases) { keyType in
                            Text(keyType.displayName).tag(Optional(keyType))
                        }
                        Text("Sin llave").tag(PaymentKeyType?.none)
                    }

                    if viewModel.keyType != nil {
                        TextField("Valor de la llave", text: $viewModel.keyValue)
                            .autocorrectionDisabled()
                            .textInputAutocapitalization(.never)
                    } else {
                        TextField("Código de comercio", text: $viewModel.merchantCode)
                            .keyboardType(.numberPad)
                    }

                    TextField("Nombre del comercio", text: $viewModel.merchantName)
                    TextField("Ciudad", text: $viewModel.merchantCity)
                    TextField("Monto (opcional)", text: $viewModel.amount)
                        .keyboardType(.decimalPad)
                }

                if let message = viewModel.validationMessage {
                    Section {
                        Text(message)
                            .font(.footnote)
                            .foregroundStyle(.red)
                    }
                }

                Section {
                    Button {
                        viewModel.generate()
                    } label: {
                        Label("Generar QR", systemImage: "qrcode")
                            .frame(maxWidth: .infinity)
                    }
                }

                if let image = viewModel.generatedImage {
                    Section("Resultado") {
                        Image(uiImage: image)
                            .resizable()
                            .interpolation(.none)
                            .scaledToFit()
                            .frame(maxWidth: .infinity)
                            .padding(8)
                            .background(Color.white)
                            .clipShape(RoundedRectangle(cornerRadius: 12))

                        Text("Este QR es válido según el estándar: puedes escanearlo desde la pestaña Escáner.")
                            .font(.footnote)
                            .foregroundStyle(.secondary)

                        ShareLink(
                            item: Image(uiImage: image),
                            preview: SharePreview("QR EMV Colombia", image: Image(uiImage: image))
                        ) {
                            Label("Compartir", systemImage: "square.and.arrow.up")
                        }

                        if let rawText = viewModel.generatedRawText {
                            Button {
                                UIPasteboard.general.string = rawText
                            } label: {
                                Label("Copiar texto", systemImage: "doc.on.doc")
                            }
                        }
                    }
                }
            }
            .navigationTitle("Generar QR de prueba")
        }
    }
}
