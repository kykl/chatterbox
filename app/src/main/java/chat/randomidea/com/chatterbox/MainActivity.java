package chat.randomidea.com.chatterbox;

import android.content.Context;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.common.base.Charsets;

import io.bigfast.MessagingOuterClass.Channel;
import io.bigfast.Playerstateaction.PlayerStateAction;
import io.bigfast.Playerstateaction.PlayerStateAction.GameState;
import io.grpc.Metadata;

public class MainActivity extends AppCompatActivity {
    ChatClient chatClient;
    Channel channel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TextView textView = (TextView) findViewById(R.id.mainText);
        Context context = getApplicationContext();
        String authorization = context.getString(R.string.authorization);
        String session = context.getString(R.string.session);
        Metadata metadata = ChatClient.createMetadata(authorization, session);
        NativeMessageHandler messageHandler = new NativeMessageHandler(textView, Looper.getMainLooper());

        chatClient = new ChatClient("messaging.rndmi.com", 8443, metadata, messageHandler);
        Log.i(chatClient.toString(), "Saying hello for the first time!");

        channel = chatClient.createChannel();
        chatClient.subscribe(channel.getId());

        PlayerStateAction playerStateAction = PlayerStateAction.newBuilder()
                .setChannelId(channel.getId())
                .setUserId(ChatClient.userId)
                .setPlayId("Play123")
                .setPlayerState(
                        GameState.newBuilder()
                                .setPosition(PlayerStateAction.Position.newBuilder()
                                        .setX(1.0f)
                                        .setY(2.0f)
                                        .setZ(3.0f)
                                        .build()
                                )
                                .build()
                )
                .build();

        chatClient.sendMessage(channel.getId(), ChatClient.userId, encodeData(playerStateAction));
    }

    public void subscribe(View view) {
        channel = chatClient.createChannel();
        chatClient.subscribe(channel.getId());
    }

    public void sayHello(View view) {
        String message;
        try {
            EditText editText = (EditText) findViewById(R.id.chatterBox);
            message = editText.getText().toString();
        } catch (NullPointerException exception) {
            message = "I'm a bigger teapot!";
        }

        chatClient.sendMessage(channel.getId(), ChatClient.userId, message);
    }

    private String encodeData(PlayerStateAction playerStateAction) {
        byte[] bytes = playerStateAction.toByteArray();
        return new String(bytes, Charsets.ISO_8859_1);
    }
}
