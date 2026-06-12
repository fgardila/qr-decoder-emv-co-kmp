package dev.code93.emvqr.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import dev.code93.emvqr.presentation.generate.GenerateQrScreen
import dev.code93.emvqr.presentation.scanner.ScannerScreen
import dev.code93.emvqr.presentation.scanner.camera.CameraScannerScreen
import dev.code93.emvqr.presentation.settings.LicensesScreen
import dev.code93.emvqr.presentation.settings.SettingsScreen

const val SCANNED_QR_RESULT_KEY = "scanned_qr"

@Composable
fun QrdNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Route.Scanner,
        modifier = modifier
    ) {
        composable<Route.Scanner> { backStackEntry ->
            ScannerScreen(
                scannedQrFlow = backStackEntry.savedStateHandle
                    .getStateFlow<String?>(SCANNED_QR_RESULT_KEY, null),
                onConsumeScannedQr = {
                    backStackEntry.savedStateHandle[SCANNED_QR_RESULT_KEY] = null
                },
                onOpenCamera = { navController.navigate(Route.CameraScanner) }
            )
        }

        composable<Route.CameraScanner> {
            CameraScannerScreen(
                onQrScanned = { rawText ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(SCANNED_QR_RESULT_KEY, rawText)
                    navController.popBackStack()
                },
                onClose = { navController.popBackStack() }
            )
        }

        composable<Route.Generate> {
            GenerateQrScreen()
        }

        composable<Route.Settings> {
            SettingsScreen(
                onOpenLicenses = { navController.navigate(Route.Licenses) }
            )
        }

        composable<Route.Licenses> {
            LicensesScreen(onBack = { navController.popBackStack() })
        }
    }
}
