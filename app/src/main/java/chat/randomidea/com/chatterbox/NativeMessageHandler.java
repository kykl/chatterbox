package chat.randomidea.com.chatterbox;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import com.google.common.base.Charsets;
import com.google.protobuf.InvalidProtocolBufferException;

import io.bigfast.Playerstateaction.PlayerStateAction;

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
        String messageString = (String) message.obj;
        byte[] bytes = messageString.getBytes(Charsets.ISO_8859_1);
        PlayerStateAction playerStateAction;
        try {
            playerStateAction = PlayerStateAction.parseFrom(bytes);

        } catch (InvalidProtocolBufferException invalidProtocolBufferException) {
            playerStateAction = PlayerStateAction.getDefaultInstance();
        }

        textView.append("\n" + playerStateAction.toString());
    }
}
