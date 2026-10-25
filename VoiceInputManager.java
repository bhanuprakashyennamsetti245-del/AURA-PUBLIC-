package com.aura.ai;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import java.util.ArrayList;
import java.util.Locale;

public final class VoiceInputManager {

    public interface Listener {
        void onResult(String text);
        void onError(String message);
    }

    private VoiceInputManager() {
    }

    public static boolean isAvailable(Activity activity) {
        return SpeechRecognizer.isRecognitionAvailable(activity);
    }

    public static Intent createIntent(String language) {
        Intent intent = new Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                "te".equals(language)
                        ? "te-IN"
                        : Locale.US.toLanguageTag());

        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                "te".equals(language) ? "te-IN" : "en-US");

        intent.putExtra(
                RecognizerIntent.EXTRA_PROMPT,
                "Speak to AURA AI");

        intent.putExtra(
                RecognizerIntent.EXTRA_MAX_RESULTS, 1);

        return intent;
    }

    public static void handleActivityResult(
            int requestCode,
            int expectedRequestCode,
            int resultCode,
            Intent data,
            Listener listener) {

        if (requestCode != expectedRequestCode || listener == null) {
            return;
        }

        if (resultCode != Activity.RESULT_OK || data == null) {
            listener.onError("Voice input was cancelled or returned no result.");
            return;
        }

        ArrayList<String> results = data.getStringArrayListExtra(
                RecognizerIntent.EXTRA_RESULTS);

        if (results == null || results.isEmpty()
                || results.get(0).trim().isEmpty()) {
            listener.onError("No speech was recognised. Please try again.");
            return;
        }

        listener.onResult(results.get(0).trim());
    }
}