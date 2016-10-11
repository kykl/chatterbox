package chat.randomidea.com.chatterbox;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.unity3d.player.UnityPlayer;

/**
 * Handler for Unity3D
 * Pass in name of GameObject and the receive method
 */

class UnityMessageHandler extends Handler {
    private String gameObject;
    private String receiveMethod;

    UnityMessageHandler(String gameObject, String receiveMethod) {
        this.gameObject = gameObject;
        this.receiveMethod = receiveMethod;
    }

    @Override
    public void handleMessage(Message message) {
        String stringMessage = (String) message.obj;
        Log.i("ChatClient", "handleMessage: " + stringMessage);
        UnityPlayer.currentActivity.getMainLooper();
        UnityPlayer.UnitySendMessage(gameObject, receiveMethod, stringMessage);
    }
}
