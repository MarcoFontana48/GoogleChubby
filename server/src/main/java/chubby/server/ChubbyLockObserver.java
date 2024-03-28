package chubby.server;

import chubby.control.handle.ChubbyHandleRequest;
import chubby.control.handle.ChubbyHandleType;
import chubby.control.handle.ChubbyEventType;
import chubby.control.message.ChubbyNotification;
import chubby.server.node.ChubbyNodeValue;
import chubby.server.node.ChubbyNodeValueDeserializer;
import chubby.server.node.ChubbyNodeValueSerializer;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.grpc.stub.StreamObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public class ChubbyLockObserver<T> implements StreamObserver<T> {
    private final Logger logger = LogManager.getLogger();
    private final String username;
    private final Path path;
    private final boolean sendHandleInvalid;
    private final ChubbyHandleType chubbyHandleType;
    private final LocalDate localDate;
    private final LocalTime localTime;
    private final KV kvClient;

    public ChubbyLockObserver(String username, @NotNull ChubbyHandleRequest chubbyHandleRequest, @NotNull Client client) {
        this.username = username;
        this.path = Path.of(chubbyHandleRequest.getRequestedAbsolutePath());
        this.sendHandleInvalid = chubbyHandleRequest.getChubbyEventTypeList().contains(ChubbyEventType.HANDLE_INVALID);
        this.chubbyHandleType = chubbyHandleRequest.getChubbyHandleType();
        this.logger.trace("subscription 'handle invalid' on node '{}' set '{}'", this.path, this.sendHandleInvalid);
        this.localDate = LocalDate.now();
        this.localTime = LocalTime.now().truncatedTo(ChronoUnit.SECONDS);
        this.kvClient = client.getKVClient();
    }

    @Override
    public void onNext(T value) {
        this.logger.debug("received keep-alive response over locked node '{}':\n'{}'", this.path, value);
    }

    @Override
    public void onError(@NotNull Throwable t) {
        this.logger.error("error detected '{}', processing handle invalidation event and removing lock from node '{}'", t.getMessage(), this.path);

        //if the handle has become invalid, send a notification to the client
        if (this.sendHandleInvalid) {
            this.logger.trace("detected handle invalid subscription, sending notification...");
            this.sendInvalidHandleNotification();
        } else {
            this.logger.trace("handle invalid subscription not active, skipping notification...");
        }

        this.logger.trace("deleting lock from node...");
        this.deleteLockFromNode();
    }

    @Override
    public void onCompleted() {
        this.logger.debug("observation completed, processing handle invalidation event and removing lock from node '{}'", this.path);

        if (this.sendHandleInvalid) {
            this.logger.trace("detected handle invalid subscription, sending notification...");
            this.sendInvalidHandleNotification();
        } else {
            this.logger.trace("handle invalid subscription not active, skipping notification...");
        }

        this.logger.trace("deleting lock from node...");
        this.deleteLockFromNode();
    }

    /**
     * Send a chubby message notification to the client about the handle invalidation.
     */
    private void sendInvalidHandleNotification() {
        this.logger.debug("detected 'handle invalid' subscription, about to send chubby message notification");

        OutputStream outputStream = System.out;

        ChubbyNotification chubbyNotification = new ChubbyNotification(this.path, ByteSequence.from(this.path.toString().getBytes()), "handle over '" + this.path + "' node (obtained at '" + this.localDate + " " + this.localTime + "') has become invalid");
        try {
            outputStream.write(chubbyNotification.getFormattedMessage().getBytes());
            this.logger.debug("successfully sent notification");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Delete the client from the node's lock map.
     */
    private void deleteLockFromNode() {
        //delete lock from node
        this.kvClient.get(ByteSequence.from(this.path.toString().getBytes())).thenAccept(getResponse -> {
            this.logger.debug("about to delete lock on node '{}'", this.path);

            //if the node still exists, remove the lock
            if (getResponse.getCount() > 0) {
                this.logger.trace("the node still exists");

                ChubbyNodeValue chubbyNodeValue = ChubbyNodeValueDeserializer.deserialize(getResponse.getKvs().getFirst().getValue().toString());
                this.logger.trace("current node value of node '{}' is '{}'", this.path, chubbyNodeValue.toString());

                this.logger.trace("about to remove client lock '{}={}'", this.username, this.chubbyHandleType);
                chubbyNodeValue.getMetadata().removeClientLock(this.username, this.chubbyHandleType);
                this.logger.trace("current node value of node '{}' is '{}'", this.path, chubbyNodeValue.toString());

                this.kvClient.put(ByteSequence.from(this.path.toString().getBytes()), ByteSequence.from(ChubbyNodeValueSerializer.serialize(chubbyNodeValue).getBytes())).thenAccept(putResponse -> {
                    this.logger.debug("successfully removed client lock");
                });
            } else {
                this.logger.debug("the node does not exist anymore, skipping remove lock operation...");
            }
        });
    }
}