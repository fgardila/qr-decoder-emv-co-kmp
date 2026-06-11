package dev.code93.kmp.qrd.android.camera

sealed class ReadState {
    data class Read(val data: String) : ReadState()
    data object Cancel : ReadState()
    data object Error : ReadState()
}
