package dev.code93.qrscanner.compose

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * Iconos como ImageVector propios (paths de Material Symbols, Apache 2.0)
 * para no depender de material-icons, congelado en 1.7.8 en este repo.
 */
internal object ScannerIcons {

    val Close: ImageVector by lazy {
        icon(
            name = "Close",
            pathData = "M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 " +
                "6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"
        )
    }

    val FlashOn: ImageVector by lazy {
        icon(name = "FlashOn", pathData = "M7 2v11h3v9l7-12h-4l4-8z")
    }

    val FlashOff: ImageVector by lazy {
        icon(
            name = "FlashOff",
            pathData = "M3.27 3L2 4.27l5 5V13h3v9l3.58-6.14L17.73 20 19 18.73 " +
                "3.27 3zM17 10h-4l4-8H7v2.18l8.46 8.46L17 10z"
        )
    }

    val Gallery: ImageVector by lazy {
        icon(
            name = "Gallery",
            pathData = "M22 16V4c0-1.1-.9-2-2-2H8c-1.1 0-2 .9-2 2v12c0 1.1.9 2 " +
                "2 2h12c1.1 0 2-.9 2-2zm-11-4l2.03 2.71L16 11l4 5H8l3-4zM2 " +
                "6v14c0 1.1.9 2 2 2h14v-2H4V6H2z"
        )
    }

    private fun icon(name: String, pathData: String): ImageVector =
        ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).addPath(
            pathData = PathParser().parsePathString(pathData).toNodes(),
            fill = SolidColor(Color.Black)
        ).build()
}
