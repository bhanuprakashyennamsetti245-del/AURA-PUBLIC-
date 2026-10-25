
package com.aura.ai;

import android.content.Context;

import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import java.util.concurrent.Executor;

public final class BiometricLock {

    public interface Callback {
        void onAuthenticated();

        void onFailed(String message);

        void onCancelled();
    }

    private BiometricLock() {
    }

    private static int authenticators() {
        // Strong biometrics only for this security-sensitive operation.
        return BiometricManager.Authenticators.BIOMETRIC_STRONG;
    }

    public static boolean isAvailable(Context context) {
        if (context == null) {
            return false;
        }

        BiometricManager manager =
                BiometricManager.from(context);

        return manager.canAuthenticate(authenticators())
                == BiometricManager.BIOMETRIC_SUCCESS;
    }

    public static void authenticate(
            FragmentActivity activity,
            Callback callback) {

        if (activity == null || callback == null) {
            return;
        }

        BiometricManager manager =
                BiometricManager.from(activity);

        int availability =
                manager.canAuthenticate(authenticators());

        if (availability != BiometricManager.BIOMETRIC_SUCCESS) {
            callback.onFailed(getAvailabilityMessage(availability));
            return;
        }

        Executor executor =
                ContextCompat.getMainExecutor(activity);

        BiometricPrompt prompt = new BiometricPrompt(
                activity,
                executor,
                new BiometricPrompt.AuthenticationCallback() {

                    @Override
                    public void onAuthenticationSucceeded(
                            BiometricPrompt.AuthenticationResult result) {

                        super.onAuthenticationSucceeded(result);
                        callback.onAuthenticated();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();

                        callback.onFailed(
                                "Fingerprint not recognised. Try again."
                        );
                    }

                    @Override
                    public void onAuthenticationError(
                            int errorCode,
                            CharSequence errString) {

                        super.onAuthenticationError(
                                errorCode,
                                errString
                        );

                        if (errorCode
                                == BiometricPrompt.ERROR_USER_CANCELED
                                || errorCode
                                == BiometricPrompt.ERROR_NEGATIVE_BUTTON
                                || errorCode
                                == BiometricPrompt.ERROR_CANCELED
                                || errorCode
                                == BiometricPrompt.ERROR_NO_BIOMETRICS) {

                            callback.onCancelled();

                        } else {
                            callback.onFailed(
                                    errString == null
                                            ? "Biometric authentication failed."
                                            : errString.toString()
                            );
                        }
                    }
                });

        BiometricPrompt.PromptInfo promptInfo =
                new BiometricPrompt.PromptInfo.Builder()
                        .setTitle("AURA AI Security")
                        .setSubtitle(
                                "Verify your identity to continue"
                        )
                        .setAllowedAuthenticators(authenticators())
                        .setNegativeButtonText("Cancel")
                        .build();

        prompt.authenticate(promptInfo);
    }

    private static String getAvailabilityMessage(int result) {
        switch (result) {
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                return "This phone has no supported biometric hardware.";

            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                return "Biometric hardware is temporarily unavailable.";

            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                return "Register a fingerprint in your phone settings first.";

            case BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED:
                return "Update your phone security before using biometrics.";

            default:
                return "Strong biometric authentication is unavailable.";
        }
    }
}
