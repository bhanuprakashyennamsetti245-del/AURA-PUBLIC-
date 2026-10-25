package com.aura.ai;

public final class ChatMessage {

    public enum Sender {
        USER,
        AURA
    }

    private final String text;
    private final Sender sender;
    private final String language;
    private final long timestamp;

    public ChatMessage(
            String text,
            Sender sender,
            String language) {

        this.text = text == null ? "" : text;
        this.sender = sender == null ? Sender.AURA : sender;

        this.language = "te".equals(language) ? "te" : "en";
        this.timestamp = System.currentTimeMillis();
    }

    public String getText() {
        return text;
    }

    public Sender getSender() {
        return sender;
    }

    public String getLanguage() {
        return language;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isFromUser() {
        return sender == Sender.USER;
    }

    public String getFormattedTime() {
        java.text.SimpleDateFormat formatter =
                new java.text.SimpleDateFormat(
                        "hh:mm a",
                        java.util.Locale.getDefault());

        return formatter.format(new java.util.Date(timestamp));
    }
}