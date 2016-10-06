package chat.randomidea.com.chatterbox;

import android.content.Context;
import android.util.Base64;

import com.google.protobuf.ByteString;
import com.unity3d.player.UnityPlayer;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.bigfast.MessagingGrpc;
import io.bigfast.MessagingOuterClass.Channel;
import io.bigfast.MessagingOuterClass.Channel.Message;
import io.bigfast.MessagingOuterClass.Channel.Subscription.Add;
import io.bigfast.MessagingOuterClass.Empty;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;


/**
 * ChatClient
 * Base implementation for bidirectional rpc
 * To be used by Unity3D as a jar downstream
 */
public class ChatClient {
    private static final Logger logger = Logger.getLogger(ChatClient.class.getName());
    private static final String userId = "18125";
    private final ManagedChannel channel;
    private final MessagingGrpc.MessagingBlockingStub blockingStub;
    private final MessagingGrpc.MessagingStub asyncStub;
    private final StreamObserver<Message> eventSubscriptionStreamObserver;

    public ChatClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port));
        logger.info("Running chat client for " + host + ":" + port);
    }

    /**
     * Construct client for accessing RouteGuide server using the existing channel.
     */
    public ChatClient(ManagedChannelBuilder<?> channelBuilder) {
        Context context = UnityPlayer.currentActivity.getBaseContext();
        String authorization = context.getString(R.string.authorization);
        String session = context.getString(R.string.session);
        channel = channelBuilder.build();
        Metadata metadata = new Metadata();
        metadata.put(
                Metadata.Key.of("AUTHORIZATION", Metadata.ASCII_STRING_MARSHALLER),
                authorization
        );
        metadata.put(
                Metadata.Key.of("X-AUTHENTICATION", Metadata.ASCII_STRING_MARSHALLER),
                session
        );
        blockingStub = MetadataUtils.attachHeaders(
                MessagingGrpc.newBlockingStub(channel),
                metadata
        );
        asyncStub = MetadataUtils.attachHeaders(
                MessagingGrpc.newStub(channel),
                metadata
        );

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
                UnityPlayer.UnitySendMessage("HyperCube", "ReceiveMessage", jsonString);
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
        Channel channel = blockingStub.createChannel(Empty.getDefaultInstance());
        blockingStub.subscribeChannel(
                Add.newBuilder()
                        .setChannelId(channel.getId())
                        .setUserId(userId)
                        .build()
        );
        String message = "{'text':'hello!'}";
        logger.info("Saying hello for the first time!");
        sendMessage(channel.getId(), userId, message);

        eventSubscriptionStreamObserver.onCompleted();
    }

    public void sendMessage(String channelId, String userId, String messageContent) {
        byte[] b64Msg = Base64.encode(messageContent.getBytes(), Base64.DEFAULT);
        ByteString byteStringContent = ByteString.copyFrom(b64Msg);
        logger.info("Sending message: " + new String(b64Msg));
        eventSubscriptionStreamObserver.onNext(
                Message.newBuilder()
                        .setChannelId(channelId)
                        .setUserId(userId)
                        .setContent(byteStringContent)
                        .build()
        );
    }
}
