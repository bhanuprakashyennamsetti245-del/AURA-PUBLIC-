package com.aura.ai;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public final class ChatAdapter
        extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {

    private static final int BG_USER = Color.rgb(12, 91, 112);
    private static final int BG_AURA = Color.rgb(20, 32, 53);
    private static final int CYAN = Color.rgb(25, 221, 247);
    private static final int WHITE = Color.WHITE;
    private static final int MUTED = Color.rgb(170, 190, 210);

    private final List<ChatMessage> messages = new ArrayList<>();

    public void addMessage(ChatMessage message) {
        if (message == null) return;

        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    public void setMessages(List<ChatMessage> newMessages) {
        messages.clear();

        if (newMessages != null) {
            messages.addAll(newMessages);
        }

        notifyDataSetChanged();
    }

    public ChatMessage getMessage(int position) {
        if (position < 0 || position >= messages.size()) {
            return null;
        }

        return messages.get(position);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        LinearLayout row = new LinearLayout(parent.getContext());
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(dp(parent, 2), dp(parent, 5),
                dp(parent, 2), dp(parent, 5));

        return new MessageViewHolder(row);
    }

    @Override
    public void onBindViewHolder(
            @NonNull MessageViewHolder holder, int position) {

        ChatMessage message = messages.get(position);
        boolean fromUser = message.isFromUser();

        LinearLayout row = holder.row;
        row.removeAllViews();

        LinearLayout bubble = new LinearLayout(row.getContext());
        bubble.setOrientation(LinearLayout.VERTICAL);
        bubble.setPadding(dp(row, 13), dp(row, 8),
                dp(row, 13), dp(row, 7));

        GradientDrawable background = new GradientDrawable();
        background.setColor(fromUser ? BG_USER : BG_AURA);
        background.setCornerRadius(dp(row, 17));
        bubble.setBackground(background);

        TextView sender = new TextView(row.getContext());
        sender.setText(fromUser ? "YOU" : "AURA AI");
        sender.setTextColor(CYAN);
        sender.setTextSize(11);
        sender.setTypeface(null, Typeface.BOLD);

        TextView body = new TextView(row.getContext());
        body.setText(message.getText());
        body.setTextColor(WHITE);
        body.setTextSize(16);
        body.setTextIsSelectable(true);
        body.setPadding(0, dp(row, 4), 0, dp(row, 5));

        TextView time = new TextView(row.getContext());
        time.setText(message.getFormattedTime());
        time.setTextColor(MUTED);
        time.setTextSize(10);
        time.setGravity(Gravity.END);

        bubble.addView(sender);
        bubble.addView(body);
        bubble.addView(time);

        LinearLayout.LayoutParams bubbleParams =
                new LinearLayout.LayoutParams(
                        dp(row, 290), ViewGroup.LayoutParams.WRAP_CONTENT);

        bubbleParams.gravity = fromUser
                ? Gravity.END : Gravity.START;

        row.addView(bubble, bubbleParams);
    }

    private static int dp(ViewGroup view, float value) {
        return (int) (value
                * view.getResources().getDisplayMetrics().density + 0.5f);
    }

    static final class MessageViewHolder
            extends RecyclerView.ViewHolder {

        final LinearLayout row;

        MessageViewHolder(LinearLayout row) {
            super(row);
            this.row = row;
        }
    }
}