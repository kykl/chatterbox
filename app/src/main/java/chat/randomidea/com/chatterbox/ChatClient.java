package chat.randomidea.com.chatterbox;

import com.unity3d.player.UnityPlayer;

/**
 * Created by andy on 8/18/16.
 */
public class ChatClient {

    public void sayHello() {
        UnityPlayer.UnitySendMessage("HyperCube", "sayHello", "hi there");
    }
}
