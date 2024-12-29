//
//  QRScannerView.swift
//  emvqrdecoder
//
//  Created by Fabian Ardila Castro on 26/12/24.
//

import SwiftUI
import AVFoundation

struct QRScannerView: UIViewControllerRepresentable {
    @Binding var scannedCode: String
    @Environment(\.presentationMode) var presentationMode
    @State private var showAlert = false
    @State private var alertMessage = ""

    class Coordinator: NSObject, AVCaptureMetadataOutputObjectsDelegate {
        var parent: QRScannerView

        init(parent: QRScannerView) {
            self.parent = parent
        }

        func metadataOutput(_ output: AVCaptureMetadataOutput, didOutput metadataObjects: [AVMetadataObject], from connection: AVCaptureConnection) {
            if let metadataObject = metadataObjects.first as? AVMetadataMachineReadableCodeObject,
               let stringValue = metadataObject.stringValue {
                parent.scannedCode = stringValue
                parent.presentationMode.wrappedValue.dismiss()
            }
        }
    }

    func makeCoordinator() -> Coordinator {
        return Coordinator(parent: self)
    }

    func makeUIViewController(context: Context) -> UIViewController {
        let viewController = UIViewController()

        // Verificar el estado del permiso de la cámara
        switch AVCaptureDevice.authorizationStatus(for: .video) {
        case .authorized:
            setupCamera(viewController, context: context)
        case .notDetermined:
            AVCaptureDevice.requestAccess(for: .video) { granted in
                if granted {
                    DispatchQueue.main.async {
                        setupCamera(viewController, context: context)
                    }
                }
            }
        case .denied:
            DispatchQueue.main.async {
                viewController.view.backgroundColor = .black
                let label = UILabel()
                label.text = "Please enable camera access in Settings"
                label.textColor = .white
                label.textAlignment = .center
                label.frame = viewController.view.bounds
                viewController.view.addSubview(label)
            }
        case .restricted:
            // Manejar el caso de acceso restringido
            break
        @unknown default:
            break
        }

        return viewController
    }

    private func setupCamera(_ viewController: UIViewController, context: Context) {
        let session = AVCaptureSession()

        guard let videoCaptureDevice = AVCaptureDevice.default(for: .video) else {
            return
        }

        do {
            let videoInput = try AVCaptureDeviceInput(device: videoCaptureDevice)

            if session.canAddInput(videoInput) {
                session.addInput(videoInput)
            }

            let metadataOutput = AVCaptureMetadataOutput()

            if session.canAddOutput(metadataOutput) {
                session.addOutput(metadataOutput)
                metadataOutput.setMetadataObjectsDelegate(context.coordinator, queue: DispatchQueue.main)
                metadataOutput.metadataObjectTypes = [.qr]
            }

            DispatchQueue.main.async {
                let previewLayer = AVCaptureVideoPreviewLayer(session: session)
                previewLayer.frame = viewController.view.layer.bounds
                previewLayer.videoGravity = .resizeAspectFill
                viewController.view.layer.addSublayer(previewLayer)

                // Agregar cuadro blanco
                let scanFrame = UIView()
                scanFrame.layer.borderColor = UIColor.white.cgColor
                scanFrame.layer.borderWidth = 2
                scanFrame.layer.cornerRadius = 8
                scanFrame.frame = CGRect(
                    x: (viewController.view.bounds.width - 250) / 2,
                    y: (viewController.view.bounds.height - 250) / 2,
                    width: 250,
                    height: 250
                )
                scanFrame.backgroundColor = UIColor.clear
                viewController.view.addSubview(scanFrame)

                // Botón de cerrar
                let closeButton = UIButton(type: .system)
                closeButton.setTitle("Close", for: .normal)
                closeButton.setTitleColor(.white, for: .normal)
                closeButton.backgroundColor = UIColor.black.withAlphaComponent(0.5)
                closeButton.layer.cornerRadius = 15
                closeButton.frame = CGRect(x: viewController.view.bounds.width - 70, y: 40, width: 60, height: 30)
                closeButton.addTarget(
                    context.coordinator,
                    action: #selector(Coordinator.closeScanner),
                    for: .touchUpInside
                )
                viewController.view.addSubview(closeButton)

                session.startRunning()
            }
        } catch {
            // Manejar el error
            print("Error setting up camera: \(error.localizedDescription)")
        }
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

extension QRScannerView.Coordinator {
    @objc func closeScanner() {
        parent.presentationMode.wrappedValue.dismiss()
    }
}
