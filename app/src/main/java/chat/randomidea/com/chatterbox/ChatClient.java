package chat.randomidea.com.chatterbox;

import android.util.Base64;

import com.google.protobuf.ByteString;

import java.util.concurrent.TimeUnit;
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
    public static final String userId = "18127";
    private static final Logger logger = Logger.getLogger(ChatClient.class.getName());
    private final ManagedChannel channel;
    private final MessagingGrpc.MessagingBlockingStub blockingStub;
    private final MessagingGrpc.MessagingStub asyncStub;
    private final StreamObserver<Message> eventSubscriptionStreamObserver;
    private final MessageHandler handler;

    ChatClient(String host, int port, Metadata metadata, MessageHandler messageHandler) {
        logger.info("Running chat client for " + host + ":" + port);
        channel = ManagedChannelBuilder.forAddress(host, port).build();
        handler = messageHandler;

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

    /**
     * ChatClient constructor for Unity3D Player
     *
     * @param host          name of host
     * @param port          port number - usually 8443
     * @param authorization HMAC string to be used in Metadata
     * @param session       session string to be used in Metadata
     * @param gameObject    class name of GameObject in Unity with attached MonoBehaviour script
     * @param receiveMethod method name for MonoBehaviour script that handles incoming message
     */
    public ChatClient(String host, int port, String authorization, String session, String gameObject, String receiveMethod) {
        this(host, port, createMetadata(authorization, session), new UnityMessageHandler(gameObject, receiveMethod));
    }

    public static Metadata createMetadata(String authorization, String session) {
        Metadata metadata = new Metadata();
        metadata.put(
                Metadata.Key.of("AUTHORIZATION", Metadata.ASCII_STRING_MARSHALLER),
                authorization
        );
        metadata.put(
                Metadata.Key.of("X-AUTHENTICATION", Metadata.ASCII_STRING_MARSHALLER),
                session
        );
        return metadata;
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    private StreamObserver<Message> setupBidirectionalStream() {
        logger.info("Setting up bidirectional stream");
        return asyncStub.channelMessageStream(new StreamObserver<Message>() {
            @Override
            public void onNext(Message message) {
                logger.info("Got message:" + message);
                byte[] bytes = message.getContent().toByteArray();
                String byteString = new String(bytes);
                handler.handleMessage(byteString);
            }

            @Override
            public void onError(Throwable t) {
                Status status = Status.fromThrowable(t);
                logger.warning("Client onError: " + status);
            }

            @Override
            public void onCompleted() {
                logger.info("Client onCompleted");
            }
        });
    }

    public String createChannelGetId() {
        return createChannel().getId();
    }

    public Channel createChannel(){
        return blockingStub.createChannel(Empty.getDefaultInstance());
    }

    public void subscribe(String channelId) {
        blockingStub.subscribeChannel(
                Add.newBuilder()
                        .setChannelId(channelId)
                        .setUserId(userId)
                        .build()
        );
    }

    public void sendMessage(String channelId, String userId, String messageContent) {
        byte[] bytes = messageContent.getBytes();
        ByteString byteStringContent = ByteString.copyFrom(bytes);
        logger.info("Sending message: " + new String(bytes));
        eventSubscriptionStreamObserver.onNext(
                Message.newBuilder()
                        .setChannelId(channelId)
                        .setUserId(userId)
                        .setContent(byteStringContent)
                        .build()
        );
    }
}
