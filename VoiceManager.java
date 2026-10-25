package com.aura.ai;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import java.util.Locale;
import java.util.UUID;

public final class VoiceManager {

    public interface Listener {
        void onReady(boolean ready, String message);
        void onSpeechFinished(String utteranceId);
        void onError(String message);
    }

    private final Context context;
    private final Listener listener;

    private TextToSpeech textToSpeech;
    private boolean initialized = false;
    private boolean released = false;

    public VoiceManager(Context context, Listener listener) {
        this.context = context.getApplicationContext();
        this.listener = listener;

        textToSpeech = new TextToSpeech(
                this.context,
                status -> {
                    if (released) return;

                    initialized = status == TextToSpeech.SUCCESS;

                    if (initialized) {
                        textToSpeech.setSpeechRate(1.0f);
                        textToSpeech.setPitch(1.0f);

                        textToSpeech.setOnUtteranceProgressListener(
                                new UtteranceProgressListener() {
                                    @Override
                                    public void onStart(String utteranceId) {
                                    }

                                    @Override
                                    public void onDone(String utteranceId) {
                                        if (VoiceManager.this.listener != null) {
                                            VoiceManager.this.listener
                                                    .onSpeechFinished(utteranceId);
                                        }
                                    }

                                    @Override
                                    public void onError(String utteranceId) {
                                        reportError(
                                                "Speech playback failed.");
                                    }
                                });

                        reportReady(true,
                                "Phone text-to-speech is ready.");
                    } else {
                        reportReady(false,
                                "Phone text-to-speech initialization failed.");
                    }
                });
    }

    public boolean isReady() {
        return initialized && !released && textToSpeech != null;
    }

    public void speak(String text, String language) {
        if (text == null || text.trim().isEmpty()) {
            reportError("There is no text to speak.");
            return;
        }

        if (!isReady()) {
            reportError("Speech engine is not ready.");
            return;
        }

        Locale locale = "te".equals(language)
                ? new Locale("te", "IN")
                : Locale.US;

        int result = textToSpeech.setLanguage(locale);

        if (result == TextToSpeech.LANG_MISSING_DATA
                || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            reportError("The selected speech language is unavailable. "
                    + "Install the required voice data in Android settings.");
            return;
        }

        String utteranceId = UUID.randomUUID().toString();

        int speakResult = textToSpeech.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                utteranceId);

        if (speakResult == TextToSpeech.ERROR) {
            reportError("Could not start speech playback.");
        }
    }

    public void stop() {
        if (textToSpeech != null) {
            textToSpeech.stop();
        }
    }

    public void release() {
        released = true;
        initialized = false;

        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }
    }

    private void reportReady(boolean ready, String message) {
        if (listener != null) {
            listener.onReady(ready, message);
        }
    }

    private void reportError(String message) {
        if (listener != null) {
            listener.onError(message);
        }
    }
}