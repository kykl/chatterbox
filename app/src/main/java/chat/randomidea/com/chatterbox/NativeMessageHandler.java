package chat.randomidea.com.chatterbox;

import android.util.Log;
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
        Log.i("ChatClient", "handleMessage: " + jsonString);

        textView.append("\n" + jsonString);
    }
}
