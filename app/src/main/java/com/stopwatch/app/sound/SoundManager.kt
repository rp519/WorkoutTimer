package com.stopwatch.app.sound

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.speech.tts.TextToSpeech
import kotlinx.coroutines.delay
import java.util.Locale

class SoundManager(context: Context) {

    private val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 50)
    private var tts: TextToSpeech? = null
    private var ttsReady = false

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                ttsReady = true
            }
        }
    }

    private fun speak(text: String) {
        if (ttsReady) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, text)
        }
    }

    fun speakGetReady() = speak("Get ready")

    fun speakStart() = speak("Start")

    fun speakStop() = speak("Stop")

    fun speakRoundBreak(round: Int, total: Int) =
        speak("Round $round of $total complete. Take a break.")

    fun speakWorkoutComplete() = speak("Workout complete. Great job!")

    /** Short beep for 3-2-1 countdown */
    fun playCountdownBeep() {
        toneGenerator.startTone(ToneGenerator.TONE_DTMF_1, 150)
    }

    /** Longer tone when workout starts */
    fun playGoSound() {
        toneGenerator.startTone(ToneGenerator.TONE_DTMF_A, 500)
    }

    /** Distinct alert when a round ends */
    fun playRoundEnd() {
        toneGenerator.startTone(ToneGenerator.TONE_DTMF_D, 500)
    }

    /** Celebratory triple-beep when workout is complete */
    suspend fun playWorkoutComplete() {
        repeat(3) {
            toneGenerator.startTone(ToneGenerator.TONE_DTMF_S, 250)
            delay(350)
        }
    }

    /** Warning beeps at 3 seconds before phase ends */
    fun playWarningBeep() {
        toneGenerator.startTone(ToneGenerator.TONE_DTMF_1, 100)
    }

    /** Distinctive prep beep 3 seconds before Start/Stop voice-over */
    fun playPrepBeep() {
        toneGenerator.startTone(ToneGenerator.TONE_DTMF_8, 300)
    }

    fun release() {
        toneGenerator.release()
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
