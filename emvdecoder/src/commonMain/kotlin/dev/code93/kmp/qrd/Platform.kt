package dev.code93.kmp.qrd

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform