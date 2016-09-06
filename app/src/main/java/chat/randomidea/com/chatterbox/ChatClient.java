package chat.randomidea.com.chatterbox;

import android.util.Base64;

import com.google.protobuf.ByteString;
import com.unity3d.player.UnityPlayer;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.bigfast.ChatGrpc;
import io.bigfast.ChatOuterClass.Channel;
import io.bigfast.ChatOuterClass.Channel.Message;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;


/**
 * ChatClient
 * Base implementation for bidirectional rpc
 */
public class ChatClient {
    private static final Logger logger = Logger.getLogger(ChatClient.class.getName());
    private final ManagedChannel channel;
    private final ChatGrpc.ChatBlockingStub blockingStub;
    private final ChatGrpc.ChatStub asyncStub;
    private final StreamObserver<Message> eventSubscriptionStreamObserver;

    public ChatClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port).usePlaintext(true));
        logger.info("Running chat client for " + host + ":" + port);
    }

    /**
     * Construct client for accessing RouteGuide server using the existing channel.
     */
    public ChatClient(ManagedChannelBuilder<?> channelBuilder) {
        channel = channelBuilder.build();
        blockingStub = ChatGrpc.newBlockingStub(channel);
        asyncStub = ChatGrpc.newStub(channel);
        eventSubscriptionStreamObserver = setupBidirectionalStream();
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public StreamObserver<Message> setupBidirectionalStream() {
        logger.info("Setting up bidirectional stream");
        return asyncStub.channelMessageStream(new StreamObserver<Message>() {
            @Override
            public void onNext(Message message) {
                logger.info("Got message:" + message);
                byte[] messageB64 = message.getContent().toByteArray();
                String jsonString = new String(Base64.decode(messageB64, Base64.DEFAULT));
                logger.info("Decoding message to:" + jsonString);
                UnityPlayer.UnitySendMessage("HyperCube", "SayHello", jsonString);
            }

            @Override
            public void onError(Throwable t) {
                logger.warning(t.toString());
                Status status = Status.fromThrowable(t);
                logger.log(Level.WARNING, "RouteChat Failed: {0}", status);
            }

            @Override
            public void onCompleted() {
                logger.info("Finished RouteChat");
            }
        });
    }

    public void sayHello() {
        String message = "{text: 'ping'}";
        byte[] b64Msg = Base64.encode(message.getBytes(), Base64.DEFAULT);
        ByteString byteStringContent = ByteString.copyFrom(b64Msg);
        logger.info("Saying hello for the first time!");
        eventSubscriptionStreamObserver.onNext(
                Message.newBuilder()
                        .setChannelId("1")
                        .setUserId("2")
                        .setContent(byteStringContent)
                        .build()
        );

        eventSubscriptionStreamObserver.onNext(
                Message.newBuilder()
                        .setChannelId("1")
                        .setUserId("2")
                        .setContent(byteStringContent)
                        .build()
        );

        eventSubscriptionStreamObserver.onCompleted();

        Channel channel = blockingStub.channelHistory(
                Channel.Get.newBuilder().setChannelId("1").build()
        );

        logger.info("Got a new channel ->");
        logger.info(channel.toString());
    }
}
