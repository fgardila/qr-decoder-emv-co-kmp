import SwiftUI
import emvdecoder

struct ContentView: View {
    
    @StateObject private var viewModel = MainViewModel()
    
    @State private var isShowingScanner = false
    @State private var scannedQRCode = ""
    
    var body: some View {
        NavigationView {
            GeometryReader { geometry in
                VStack {
                    // Título principal
                    Text("EMV QR Colombia Reader iOS")
                        .font(.system(size: geometry.size.width > 600 ? 40 : 28, weight: .bold))
                        .frame(maxWidth: .infinity)
                        .multilineTextAlignment(.center)
                        .padding(.top, 48)
                    
                    // Botón estilo iOS
                    Button(action: onReadQrScan) {
                        Text("Open Camera Reader")
                            .font(.headline)
                            .frame(maxWidth: .infinity)
                            .padding()
                            .frame(maxWidth: geometry.size.width > 600 ? 400 : .infinity) // Ancho máximo en iPad
                    }
                    .buttonStyle(.borderedProminent)
                    .padding(.vertical, 24)
                    
                    // Lista de tarjetas expandibles
                    ScrollView {
                        VStack {
                            ForEach(viewModel.expandableList, id: \.id) { item in
                                ExpandableCard(item: item)
                                    .frame(maxWidth: geometry.size.width > 600 ? 500 : .infinity) // Ancho limitado en iPad
                            }
                        }
                        .frame(maxWidth: .infinity)
                    }
                }
                .padding(.horizontal, 16)
                .frame(maxWidth: geometry.size.width > 600 ? 700 : .infinity) // Limitar ancho central en pantallas grandes
                .alert("Hello World!", isPresented: Binding(
                    get: { viewModel.successMessage != nil || viewModel.errorMessage != nil },
                    set: { if !$0 {
                        viewModel.successMessage = nil
                        viewModel.errorMessage = nil
                    }}
                )) {
                    Button("Ok") {
                        viewModel.successMessage = nil
                        viewModel.errorMessage = nil
                    }
                } message: {
                    if let successMessage = viewModel.successMessage {
                        Text(successMessage.text)
                    } else if let errorMessage = viewModel.errorMessage {
                        Text(errorMessage.text)
                    }
                }
                .sheet(isPresented: $isShowingScanner) {
                    QRScannerView(scannedCode: $scannedQRCode)
                        .onDisappear {
                            if !scannedQRCode.isEmpty {
                                viewModel.processQRCode(data: scannedQRCode)
                            }
                        }
                }
            }
        }
        .navigationViewStyle(StackNavigationViewStyle()) // Sin menú lateral en iPad
    }
    
    func onReadQrScan() {
        viewModel.processQRCode(data: Constants.dataQrDummy)
    }
}

struct ExpandableCard: View {
    let item: ExpandableItem
    @State private var isExpanded = false
    
    var body: some View {
        VStack {
            HStack {
                Text(item.title)
                    .font(.headline)
                    .fontWeight(.bold)
                
                Spacer()
                
                Image(systemName: isExpanded ? "chevron.up" : "chevron.down")
                    .onTapGesture {
                        withAnimation {
                            isExpanded.toggle()
                        }
                    }
            }
            .padding()
            .background(Color(UIColor.systemGray5))
            .cornerRadius(8)
            
            if isExpanded {
                VStack(alignment: .leading, spacing: 8) {
                    ForEach(item.items, id: \.0) { label, description in
                        VStack(alignment: .leading) {
                            Text(label)
                                .fontWeight(.bold)
                            if let description = description {
                                Text(description)
                                    .font(.subheadline)
                                    .foregroundColor(.gray)
                            }
                            Divider()
                        }
                    }
                }
                .padding()
            }
        }
        .padding(.vertical, 8)
    }
}

struct ExpandableItem {
    let id = UUID()
    let title: String
    let items: [(String, String?)]
}

struct AlertMessage: Identifiable {
    let id = UUID()
    let text: String
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
