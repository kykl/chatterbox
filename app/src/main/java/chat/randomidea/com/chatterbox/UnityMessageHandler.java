package chat.randomidea.com.chatterbox;

import com.unity3d.player.UnityPlayer;

/**
 * Handler for Unity3D
 * Pass in name of GameObject and the receive method
 */

class UnityMessageHandler implements MessageHandler {
    private String gameObject;
    private String receiveMethod;

    UnityMessageHandler(String gameObject, String receiveMethod) {
        this.gameObject = gameObject;
        this.receiveMethod = receiveMethod;
    }

    @Override
    public void handleMessage(String jsonString) {
        UnityPlayer.UnitySendMessage(gameObject, receiveMethod, jsonString);
    }
}
