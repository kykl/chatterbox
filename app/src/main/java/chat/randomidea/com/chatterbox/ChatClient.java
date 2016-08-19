package chat.randomidea.com.chatterbox;

import com.unity3d.player.UnityPlayer;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.bigfast.ChatGrpc;
import io.bigfast.ChatOuterClass.Event;
import io.bigfast.ChatOuterClass.EventSubscription;
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
    private final StreamObserver<EventSubscription> eventSubscriptionStreamObserver;

    public ChatClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port).usePlaintext(true));
    }

    /** Construct client for accessing RouteGuide server using the existing channel. */
    public ChatClient(ManagedChannelBuilder<?> channelBuilder) {
        channel = channelBuilder.build();
        blockingStub = ChatGrpc.newBlockingStub(channel);
        asyncStub = ChatGrpc.newStub(channel);
        eventSubscriptionStreamObserver = setupBidirectionalStream();
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public StreamObserver<EventSubscription> setupBidirectionalStream() {
        return asyncStub.subscribeEvents(new StreamObserver<Event>() {
            @Override
            public void onNext(Event value) {
                String eventString = value.toString();
                UnityPlayer.UnitySendMessage("HyperCube", "SayHello", eventString);
            }

            @Override
            public void onError(Throwable t) {
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
        eventSubscriptionStreamObserver.onNext(
                EventSubscription.newBuilder()
                        .setAppId(1L)
                        .setAuth(1L)
                        .setUserId(1L).build()
        );
    }
}
