package chat.randomidea.com.chatterbox;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

/**
 * Basic Handler for json slug
 * Appends to textView
 * Use for debugging purposes
 */

class NativeMessageHandler extends Handler {
    private TextView textView;

    NativeMessageHandler(TextView textView, Looper looper) {
        super(looper);
        this.textView = textView;
    }

    @Override
    public void handleMessage(Message message) {
        Log.i("ChatClient", "handleMessage: " + message.obj);

        textView.append("\n" + message.obj);
    }
}
