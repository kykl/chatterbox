package chat.randomidea.com.chatterbox;

import android.widget.TextView;

/**
 * Basic Handler for json slug
 * Appends to textView
 * Use for debugging purposes
 */

class NativeMessageHandler implements MessageHandler {
    private TextView textView;
    NativeMessageHandler(TextView textView) {
        this.textView = textView;
    }
    @Override
    public void handleMessage(String jsonString) {
        textView.append(jsonString);
    }
}
