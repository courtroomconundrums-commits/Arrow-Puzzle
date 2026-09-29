package com.example.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class SoundManager(private val context: Context) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun playClick(soundEnabled: Boolean, vibrationEnabled: Boolean) {
        if (vibrationEnabled) vibrate(15L)
        if (!soundEnabled) return
        scope.launch {
            playToneSequence(
                listOf(ToneSegment(freqHz = 880.0, durationMs = 38, amplitude = 0.28))
            )
        }
    }

    fun playArrowClearAndCash(soundEnabled: Boolean, vibrationEnabled: Boolean) {
        if (vibrationEnabled) vibrate(25L)
        if (!soundEnabled) return
        scope.launch {
            // Crisp ascending coin/cash chime just like the video
            playToneSequence(
                listOf(
                    ToneSegment(freqHz = 1046.5, durationMs = 55, amplitude = 0.35), // C6
                    ToneSegment(freqHz = 1318.5, durationMs = 55, amplitude = 0.38), // E6
                    ToneSegment(freqHz = 1567.98, durationMs = 65, amplitude = 0.42), // G6
                    ToneSegment(freqHz = 2093.0, durationMs = 140, amplitude = 0.45)  // C7
                )
            )
        }
    }

    fun playBlockedError(soundEnabled: Boolean, vibrationEnabled: Boolean) {
        if (vibrationEnabled) vibrate(120L)
        if (!soundEnabled) return
        scope.launch {
            playToneSequence(
                listOf(
                    ToneSegment(freqHz = 210.0, durationMs = 95, amplitude = 0.45),
                    ToneSegment(freqHz = 155.0, durationMs = 140, amplitude = 0.48)
                )
            )
        }
    }

    fun playLevelWin(soundEnabled: Boolean, vibrationEnabled: Boolean) {
        if (vibrationEnabled) vibrate(60L)
        if (!soundEnabled) return
        scope.launch {
            playToneSequence(
                listOf(
                    ToneSegment(freqHz = 783.99, durationMs = 80, amplitude = 0.38),  // G5
                    ToneSegment(freqHz = 1046.50, durationMs = 80, amplitude = 0.40), // C6
                    ToneSegment(freqHz = 1318.51, durationMs = 85, amplitude = 0.42), // E6
                    ToneSegment(freqHz = 1567.98, durationMs = 95, amplitude = 0.45), // G6
                    ToneSegment(freqHz = 2093.00, durationMs = 220, amplitude = 0.50) // C7
                )
            )
        }
    }

    private data class ToneSegment(
        val freqHz: Double,
        val durationMs: Int,
        val amplitude: Double
    )

    private fun playToneSequence(segments: List<ToneSegment>) {
        try {
            val sampleRate = 22050
            val totalSamples = segments.sumOf { (sampleRate * it.durationMs) / 1000 }
            if (totalSamples <= 0) return

            val pcm = ShortArray(totalSamples)
            var offset = 0
            for (seg in segments) {
                val count = (sampleRate * seg.durationMs) / 1000
                for (i in 0 until count) {
                    val t = i.toDouble() / sampleRate
                    val env = exp(-4.5 * i.toDouble() / count.coerceAtLeast(1))
                    val fundamental = sin(2.0 * PI * seg.freqHz * t)
                    val harmonic = 0.3 * sin(2.0 * PI * (seg.freqHz * 2.0) * t)
                    val sample = ((fundamental + harmonic) * seg.amplitude * env * Short.MAX_VALUE)
                        .toInt()
                        .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                        .toShort()
                    if (offset + i < pcm.size) {
                        pcm[offset + i] = sample
                    }
                }
                offset += count
            }

            val bufferBytes = pcm.size * 2
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferBytes.coerceAtLeast(1024))
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(pcm, 0, pcm.size)
            audioTrack.play()
            val totalMs = segments.sumOf { it.durationMs }.toLong()
            Thread.sleep(totalMs + 60L)
            audioTrack.stop()
            audioTrack.release()
        } catch (_: Throwable) {
            // Ignore audio hardware exceptions in headless/test environments
        }
    }

    private fun vibrate(durationMs: Long) {
        try {
            val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(
                        VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(durationMs)
                }
            }
        } catch (_: Throwable) {
        }
    }
}
