package dev.code93.qrscanner.core

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.posix.memcpy

/**
 * Azúcar para Swift: acepta [NSData] directamente
 * (por ejemplo `image.jpegData(...)`).
 */
public suspend fun QrImageScanner.scan(data: NSData): String? =
    scan(data.toByteArray())

@OptIn(ExperimentalForeignApi::class)
internal fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    if (size == 0) return ByteArray(0)
    return ByteArray(size).apply {
        usePinned { pinned -> memcpy(pinned.addressOf(0), bytes, length) }
    }
}
