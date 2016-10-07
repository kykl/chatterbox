package chat.randomidea.com.chatterbox;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import io.bigfast.MessagingOuterClass.Channel;
import io.grpc.Metadata;

public class MainActivity extends AppCompatActivity {
    ChatClient chatClient;
    Channel channel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView textView = (TextView) findViewById(R.id.mainText);
        Context context = getApplicationContext();
        String authorization = context.getString(R.string.authorization);
        String session = context.getString(R.string.session);
        Metadata metadata = ChatClient.createMetadata(authorization, session);
        NativeMessageHandler messageHandler = new NativeMessageHandler(textView);

        chatClient = new ChatClient("messaging.rndmi.com", 8443, metadata, messageHandler);
        Log.i(chatClient.toString(), "Saying hello for the first time!");

//        try {
//            chatClient.shutdown();
//        } catch (InterruptedException e) {
//            Log.e(chatClient.toString(), e.getMessage());
//        }
    }

    public void subscribe(View view) {
        channel = chatClient.createChannel();
        chatClient.subscribe(channel.getId());
    }

    public void sayHello(View view) {
        EditText editText = (EditText) findViewById(R.id.chatterBox);
        String message;
        try {
            message = "{'content':'" + editText.toString() + "'}";
        } catch (NullPointerException exception) {
            message = "{'content':'Hello!'}";
        }

        chatClient.sendMessage(channel.getId(), ChatClient.userId, message);
    }
}
