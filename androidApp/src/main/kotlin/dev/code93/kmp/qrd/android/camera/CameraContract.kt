package dev.code93.kmp.qrd.android.camera

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

class CameraContract : ActivityResultContract<Unit, ReadState>() {
    override fun createIntent(context: Context, input: Unit): Intent {
        return CameraGoogleReaderActivity.newInstance(context)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): ReadState {
        return intent?.let {
            when (resultCode) {
                RESULT_OK -> ReadState.Read(
                    it.getStringExtra(CameraGoogleReaderActivity.CONTENT).orEmpty()
                )

                RESULT_CANCELED -> ReadState.Cancel
                else -> ReadState.Error

            }
        } ?: ReadState.Error
    }
}