package com.nova.ai.voice.speechoutput

import android.content.Context
import android.speech.tts.TextToSpeech
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TextToSpeechManager @Inject constructor(
    @ApplicationContext private val context: Context
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val banglaLocale = Locale("bn", "BD")
    private val englishLocale = Locale.US

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // ডিফল্ট হিসেবে বাংলা ভাষা সেট করা
            val result = tts?.setLanguage(banglaLocale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale("bn"))
            }
            tts?.setPitch(1.0f)
            tts?.setSpeechRate(0.95f) // বাংলায় স্বাভাবিক কথা বলার মিষ্টি গতি
            isInitialized = true
        }
    }

    fun speak(text: String) {
        if (isInitialized && text.isNotBlank()) {
            // টেক্সটে বাংলা অক্ষর থাকলে বাংলা ভয়েস সক্রিয় হবে
            val containsBangla = text.any { it in '\u0980'..'\u09FF' }

            if (containsBangla) {
                val langResult = tts?.setLanguage(banglaLocale)
                if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.setLanguage(Locale("bn"))
                }
            } else {
                tts?.setLanguage(englishLocale)
            }

            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "NOVA_UTTERANCE_${System.currentTimeMillis()}")
        }
    }

    fun stop() {
        tts?.stop()
    }
}
