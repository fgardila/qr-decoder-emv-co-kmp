import SwiftUI

@main
struct QRDecoderApp: App {
    private let container = AppContainer.live()

    var body: some Scene {
        WindowGroup {
            RootTabView(container: container)
        }
    }
}
