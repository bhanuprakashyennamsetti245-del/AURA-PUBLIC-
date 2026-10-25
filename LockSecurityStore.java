package com.aura.ai;

public final class LockSecurityStore {

    private static final long AUTH_VALIDITY_MS = 60_000L;

    private boolean authenticated = false;
    private long authenticatedAt = 0L;

    public synchronized void markAuthenticated() {
        authenticated = true;
        authenticatedAt = System.currentTimeMillis();
    }

    public synchronized void clear() {
        authenticated = false;
        authenticatedAt = 0L;
    }

    public synchronized boolean isAuthenticated() {
        if (!authenticated) {
            return false;
        }

        long elapsed = System.currentTimeMillis() - authenticatedAt;

        if (elapsed < 0 || elapsed > AUTH_VALIDITY_MS) {
            clear();
            return false;
        }

        return true;
    }

    public synchronized long getRemainingValidityMs() {
        if (!isAuthenticated()) {
            return 0L;
        }

        long remaining = AUTH_VALIDITY_MS
                - (System.currentTimeMillis() - authenticatedAt);

        return Math.max(0L, remaining);
    }
}