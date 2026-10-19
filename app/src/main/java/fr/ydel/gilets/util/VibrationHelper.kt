package fr.ydel.gilets.util

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class VibrationHelper(private val context: Context) {
    private val vibrator: Vibrator by lazy {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    }

    fun vibrateShort() {
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    50, // 50ms short vibration
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        }
    }

    fun vibrateLong() {
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    200, // 200ms long vibration
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        }
    }

    fun vibrateConforme() {
        vibrateShort()
    }

    fun vibrateNonConforme() {
        vibrateLong()
    }
}
