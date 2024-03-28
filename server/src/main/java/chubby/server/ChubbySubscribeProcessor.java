package chubby.server;

import chubby.utils.ChubbyUtils;
import chubby.control.handle.ChubbyHandleType;
import chubby.control.message.ChubbyError;
import chubby.control.handle.ChubbyEventType;
import chubby.control.message.ChubbyNotification;
import chubby.server.node.ChubbyNodeValue;
import chubby.server.node.ChubbyNodeValueDeserializer;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.watch.WatchEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class ChubbySubscribeProcessor {
    private static final Logger logger = LogManager.getLogger();

    /**
     * Process the subscriptions to the events of the given handleAbsolutePath
     *
     * @param client                   the client to be used to process the subscriptions
     * @param handleAbsolutePath       the absolute path of the handle to be subscribed
     * @param chubbyHandleType         the type of the handle to be subscribed
     * @param chubbyEventTypeArrayList the list of events to be subscribed
     * @return the list of watchers created for the subscriptions
     */
    public static @NotNull List<Watch.Watcher> process(Client client, Path handleAbsolutePath, ChubbyHandleType chubbyHandleType, List<ChubbyEventType> chubbyEventTypeArrayList) {
        logger.trace("starting event processing with arguments: 'handleAbsolutePath:{}', 'chubbyEventTypeArrayList:{}'", handleAbsolutePath, chubbyEventTypeArrayList);

        List<Watch.Watcher> watcherList = new ArrayList<>();
        OutputStream outputStream = System.out;

        new Thread(() -> chubbyEventTypeArrayList.forEach(chubbyEventType -> {
            switch (chubbyEventType) {
                case FILE_CONTENTS_MODIFIED -> {
                    logger.trace("detected 'FILE_CONTENTS_MODIFIED' subscription, activating it...");

                    if (!ChubbyUtils.isFile(handleAbsolutePath)) {
                        ChubbyError chubbyError = new ChubbyError(handleAbsolutePath, handleAbsolutePath, "cannot make subscription 'FILE_CONTENTS_MODIFIED' to directory nodes");
                        try {
                            outputStream.write(chubbyError.getFormattedMessage().getBytes());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
//                        throw new ChubbyEventException("cannot make subscription 'file contents modified' for a directory node");
                        break;
                    }

                    ChubbyNodeValue oldChubbyNodeValue = deserializeChubbyNodeValue(client, handleAbsolutePath);

                    Watch watch = client.getWatchClient();

                    //notify only the first change, if multiple changes occur while the subscription is active, they will be ignored
                    AtomicBoolean eventProcessed = new AtomicBoolean(false);

                    Watch.Listener listener = Watch.listener(watchResponse -> {
                        // offloads the processing to another thread, in order not to block any other operation on the kv store caused by chubbyRequestProcessor
                        new Thread(() -> watchResponse.getEvents().forEach(watchEvent -> {
                            logger.trace("about to check if new event has to be sent, eventProcessed:{}, eventType:{}", eventProcessed, watchEvent.getEventType());

                            if (!eventProcessed.get() && (watchEvent.getEventType() == WatchEvent.EventType.PUT)) {
                                logger.trace("detected new event 'file content modified' of type PUT, processing event...");

                                ChubbyNodeValue newChubbyNodeValue = deserializeChubbyNodeValue(client, handleAbsolutePath);

                                logger.trace("evaluating if oldValue and currentValue have same checksum - oldValue: '{}', currentValue: '{}'", oldChubbyNodeValue.getMetadata().getChecksum(), newChubbyNodeValue.getMetadata().getChecksum());

                                if (oldChubbyNodeValue.getMetadata().getChecksum() != newChubbyNodeValue.getMetadata().getChecksum()) {
                                    logger.trace("detected differences between oldValue and currentValue's checksum, creating and sending notification...");

                                    String eventMessage = "file content just changed, consider reloading it";
                                    sendNotification(outputStream, handleAbsolutePath, eventMessage);

                                    eventProcessed.set(true);

                                } else {
                                    logger.trace("no differences detected between oldValue and currentValue, this notification won't be sent...");
                                }
                            }

                        }), "chubby_subscribe_slave(file_content_e)_processor").start();
                    });
                    Watch.Watcher fileContentWatcher = watch.watch(ByteSequence.from(handleAbsolutePath.toString().getBytes()), listener);
                    watcherList.add(fileContentWatcher);
                }
                case CHILD_NODE_ADDED -> {

                    if (ChubbyUtils.isFile(handleAbsolutePath)) {
                        ChubbyError chubbyError = new ChubbyError(handleAbsolutePath, handleAbsolutePath, "cannot make subscription 'CHILD_NODE_ADDED' to file nodes");
                        try {
                            outputStream.write(chubbyError.getFormattedMessage().getBytes());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
//                        throw new ChubbyEventException("cannot make subscription 'file contents modified' for a directory node");
                        break;
                    }

                    //if children nodes are added to this
                    logger.trace("detected 'CHILD_NODE_ADDED' subscription, activating it...");

                    AtomicReference<ChubbyNodeValue> oldChubbyNodeValue = new AtomicReference<>(deserializeChubbyNodeValue(client, handleAbsolutePath));
                    logger.trace("value stored '{}'", oldChubbyNodeValue);

                    Watch watch = client.getWatchClient();

                    //notify only the first change, if multiple changes occur while the subscription is active, they will be ignored
                    AtomicBoolean eventProcessed = new AtomicBoolean(false);

                    Watch.Listener listener = Watch.listener(watchResponse -> {
                        // offloads the processing to another thread, in order not to block any other operation on the kv store caused by chubbyRequestProcessor
                        new Thread(() -> watchResponse.getEvents().forEach(watchEvent -> {
                            logger.trace("about to check if new event has to be sent, eventProcessed:{}, eventType:{}", eventProcessed, watchEvent.getEventType());

                            if (!eventProcessed.get() && ((watchEvent.getEventType() == WatchEvent.EventType.PUT) || (watchEvent.getEventType() == WatchEvent.EventType.DELETE))) {
                                logger.trace("detected new event 'child node added', processing event...");

                                ChubbyNodeValue newChubbyNodeValue = deserializeChubbyNodeValue(client, handleAbsolutePath);

                                logger.trace("evaluating if oldMetadata and currentMetadata have different child number - oldChildNodeNumber: '{}', currentChildNodeNumber: '{}'", oldChubbyNodeValue.get().getMetadata().getChildNodeNumber(), newChubbyNodeValue.getMetadata().getChildNodeNumber());

                                if ((newChubbyNodeValue.getMetadata().getChildNodeNumber() - oldChubbyNodeValue.get().getMetadata().getChildNodeNumber()) > 0) {
                                    logger.trace("detected increase on child number, creating and sending notification...");

                                    sendNotification(outputStream, handleAbsolutePath, "number of children from currently held node has increased");

                                    eventProcessed.set(true);

                                } else {
                                    logger.trace("no differences detected between oldChildNumber and currentChildNumber, this notification won't be sent...");
                                    logger.trace("updating oldChubbyNodeValue with newChubbyNodeValue...");
                                    oldChubbyNodeValue.set(newChubbyNodeValue);
                                }

                            }
                        }), "chubby_subscribe_slave(child_node_add)_processor").start();
                    });
                    Watch.Watcher childNodeAddedWatcher = watch.watch(ByteSequence.from(handleAbsolutePath.toString().getBytes()), listener);
                    watcherList.add(childNodeAddedWatcher);
                }
                case CHILD_NODE_REMOVED -> {

                    if (ChubbyUtils.isFile(handleAbsolutePath)) {
                        ChubbyError chubbyError = new ChubbyError(handleAbsolutePath, handleAbsolutePath, "cannot make subscription 'CHILD_NODE_REMOVED' to file nodes");
                        try {
                            outputStream.write(chubbyError.getFormattedMessage().getBytes());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
//                        throw new ChubbyEventException("cannot make subscription 'file contents modified' for a directory node");
                        break;
                    }

                    //if children nodes are added to this
                    logger.trace("detected 'CHILD_NODE_REMOVED' subscription, activating it...");

                    AtomicReference<ChubbyNodeValue> oldChubbyNodeValue = new AtomicReference<>(deserializeChubbyNodeValue(client, handleAbsolutePath));
                    logger.trace("value stored '{}'", oldChubbyNodeValue);

                    Watch watch = client.getWatchClient();

                    //notify only the first change, if multiple changes occur while the subscription is active, they will be ignored
                    AtomicBoolean eventProcessed = new AtomicBoolean(false);

                    Watch.Listener listener = Watch.listener(watchResponse -> {
                        // offloads the processing to another thread, in order not to block any other operation on the kv store caused by chubbyRequestProcessor
                        new Thread(() -> watchResponse.getEvents().forEach(watchEvent -> {
                            logger.trace("about to check if new event has to be sent, eventProcessed:{}, eventType:{}", eventProcessed, watchEvent.getEventType());

                            if (!eventProcessed.get() && (watchEvent.getEventType() == WatchEvent.EventType.DELETE) || (watchEvent.getEventType() == WatchEvent.EventType.PUT)) {
                                logger.trace("detected new event 'child node removed', processing event...");

                                ChubbyNodeValue newChubbyNodeValue = deserializeChubbyNodeValue(client, handleAbsolutePath);

                                logger.trace("evaluating if oldMetadata and currentMetadata have different child number - oldChildNodeNumber: '{}', currentChildNodeNumber: '{}'", oldChubbyNodeValue.get().getMetadata().getChildNodeNumber(), newChubbyNodeValue.getMetadata().getChildNodeNumber());

                                if ((newChubbyNodeValue.getMetadata().getChildNodeNumber() - oldChubbyNodeValue.get().getMetadata().getChildNodeNumber()) < 0) {
                                    logger.trace("detected decrease on child number, creating and sending notification...");

                                    sendNotification(outputStream, handleAbsolutePath, "number of children from currently held node has decreased");

                                    eventProcessed.set(true);

                                } else {
                                    logger.trace("no differences detected between oldChildNumber and currentChildNumber, this notification won't be sent...");
                                    logger.trace("updating oldChubbyNodeValue with newChubbyNodeValue...");
                                    oldChubbyNodeValue.set(newChubbyNodeValue);
                                }

                            }
                        }), "chubby_subscribe_slave(child_node_rem)_processor").start();
                    });
                    Watch.Watcher childNodeRemoved = watch.watch(ByteSequence.from(handleAbsolutePath.toString().getBytes()), listener);
                    watcherList.add(childNodeRemoved);

                }
                case CHILD_NODE_MODIFIED -> {   //is both a subscription to child added and removed

                    if (ChubbyUtils.isFile(handleAbsolutePath)) {
                        ChubbyError chubbyError = new ChubbyError(handleAbsolutePath, handleAbsolutePath, "cannot make subscription 'CHILD_NODE_MODIFIED' to file nodes");
                        try {
                            outputStream.write(chubbyError.getFormattedMessage().getBytes());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
//                        throw new ChubbyEventException("cannot make subscription 'child node modified' for a file node");
                        break;
                    }

                    //if children nodes are added to this
                    logger.trace("detected 'CHILD_NODE_MODIFIED' subscription, activating it...");

                    AtomicReference<ChubbyNodeValue> oldChubbyNodeValue = new AtomicReference<>(deserializeChubbyNodeValue(client, handleAbsolutePath));
                    logger.trace("stored value:{}", oldChubbyNodeValue);

                    Watch watch = client.getWatchClient();

                    //notify only the first change, if multiple changes occur while the subscription is active, they will be ignored
                    AtomicBoolean eventProcessed = new AtomicBoolean(false);

                    Watch.Listener listener = Watch.listener(watchResponse -> {
                        // offloads the processing to another thread, in order not to block any other operation on the kv store caused by chubbyRequestProcessor
                        new Thread(() -> watchResponse.getEvents().forEach(watchEvent -> {
                            logger.trace("about to check if new event has to be sent, eventProcessed:{}, eventType:{}", eventProcessed, watchEvent.getEventType());

                            if (!eventProcessed.get() && ((watchEvent.getEventType() == WatchEvent.EventType.PUT) || (watchEvent.getEventType() == WatchEvent.EventType.DELETE))) {
                                logger.trace("detected new event 'child node modified', processing event...");

                                ChubbyNodeValue newChubbyNodeValue = deserializeChubbyNodeValue(client, handleAbsolutePath);

                                logger.trace("evaluating if oldMetadata and currentMetadata have different child number - oldChildNodeNumber: '{}', currentChildNodeNumber: '{}'", oldChubbyNodeValue.get().getMetadata().getChildNodeNumber(), newChubbyNodeValue.getMetadata().getChildNodeNumber());

                                if ((newChubbyNodeValue.getMetadata().getChildNodeNumber() - oldChubbyNodeValue.get().getMetadata().getChildNodeNumber()) < 0) {
                                    logger.trace("detected decrease on child number, creating and sending notification...");

                                    sendNotification(outputStream, handleAbsolutePath, "number of children from currently held node has changed");

                                    eventProcessed.set(true);

                                } else if ((newChubbyNodeValue.getMetadata().getChildNodeNumber() - oldChubbyNodeValue.get().getMetadata().getChildNodeNumber()) > 0) {
                                    logger.trace("detected increase on child number, creating and sending notification...");

                                    sendNotification(outputStream, handleAbsolutePath, "number of children from currently held node has changed");

                                    eventProcessed.set(true);
                                } else {
                                    logger.trace("no differences detected between oldChildNumber and currentChildNumber, this notification won't be sent...");
                                    logger.trace("updating oldChubbyNodeValue with newChubbyNodeValue...");
                                    oldChubbyNodeValue.set(newChubbyNodeValue);
                                }

                            }
                        }), "chubby_subscribe_slave(child_node_mod)_processor").start();
                    });
                    Watch.Watcher childNodeModified = watch.watch(ByteSequence.from(handleAbsolutePath.toString().getBytes()), listener);
                    watcherList.add(childNodeModified);
                }
                case CONFLICTING_LOCK -> {
                    logger.trace("detected 'CONFLICTING_LOCK' subscription, activating it...");

                    if (!(chubbyHandleType.equals(ChubbyHandleType.CHANGE_ACL) || chubbyHandleType.equals(ChubbyHandleType.WRITE))) {
                        ChubbyError chubbyError = new ChubbyError(handleAbsolutePath, handleAbsolutePath, "cannot make subscription 'CONFLICTING_LOCK' to with non-exclusive handle lock type");
                        try {
                            outputStream.write(chubbyError.getFormattedMessage().getBytes());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
//                        throw new ChubbyEventException("cannot make subscription 'file contents modified' for a directory node");
                        break;
                    }

                    ChubbyNodeValue oldChubbyNodeValue = deserializeChubbyNodeValue(client, handleAbsolutePath);

                    Watch watch = client.getWatchClient();

                    logger.trace("about to activate listener for 'CONFLICTING_LOCK' subscription...");
                    Watch.Listener listener = Watch.listener(watchResponse -> {
                        logger.trace("listener activated for 'CONFLICTING_LOCK' subscription...");

                        // offloads the processing to another thread, in order not to block any other operation on the kv store caused by chubbyRequestProcessor
                        new Thread(() -> watchResponse.getEvents().forEach(watchEvent -> {
                            logger.trace("about to check if new event has to be sent, eventType:{}", watchEvent.getEventType());

                            // Remove the condition on the event type
                            logger.trace("detected new event 'conflicting lock', processing event...");

                            ChubbyNodeValue newChubbyNodeValue = deserializeChubbyNodeValue(client, handleAbsolutePath);

                            logger.trace("evaluating if oldValue and currentValue have same value - oldValue: '{}', currentValue: '{}'", oldChubbyNodeValue.getMetadata().getLockRequestNumber(), newChubbyNodeValue.getMetadata().getLockRequestNumber());

                            if (oldChubbyNodeValue.getMetadata().getLockRequestNumber() != newChubbyNodeValue.getMetadata().getLockRequestNumber()) {
                                logger.trace("detected differences between oldValue and currentValue's lock request number, creating and sending notification...");

//                                oldChubbyNodeValue = newChubbyNodeValue;

                                String eventMessage = "another client just tried to exclusively lock this node";
                                sendNotification(outputStream, handleAbsolutePath, eventMessage);

                            } else {
                                logger.trace("no differences detected between oldValue and currentValue, this notification won't be sent...");
                            }
                        }), "chubby_subscribe_slave(conf_lock_req)_processor").start();
                    });
                    Watch.Watcher fileContentWatcher = watch.watch(ByteSequence.from(handleAbsolutePath.toString().getBytes()), listener);
                    watcherList.add(fileContentWatcher);
                }
                default -> {
                }
            }
        }), "chubby_subscribe_master_processor").start();

        return watcherList;
    }

    /**
     * Deserialize the json value of the given handleAbsolutePath
     *
     * @param client            the client to be used to process the deserialization
     * @param handleAbsolutePath the absolute path of the handle to be deserialized
     * @return the deserialized value of the given handleAbsolutePath
     */
    private static @NotNull ChubbyNodeValue deserializeChubbyNodeValue(@NotNull Client client, Path handleAbsolutePath) {

        String oldJsonStringNodeValue;
        try {
            logger.trace("extracting value from key: '{}'...", handleAbsolutePath.toString().getBytes());
            oldJsonStringNodeValue = client.getKVClient().get(ByteSequence.from(handleAbsolutePath.toString().getBytes())).get().getKvs().getFirst().getValue().toString();
            logger.trace("value extracted, returning: {}", oldJsonStringNodeValue);
        } catch (InterruptedException | ExecutionException e) {
            logger.error("something went wrong", e);
            throw new RuntimeException("something went wrong while extracting node value");
        }

        return ChubbyNodeValueDeserializer.deserialize(oldJsonStringNodeValue);
    }

    /**
     * Send a notification to the given outputStream
     *
     * @param outputStream       the output stream to be used to send the notification
     * @param handleAbsolutePath the absolute path of the handle to be used to send the notification
     * @param message            the message to be sent
     */
    private static void sendNotification(@NotNull OutputStream outputStream, Path handleAbsolutePath, String message) {

        ChubbyNotification chubbyNotification = new ChubbyNotification(handleAbsolutePath, ByteSequence.from(handleAbsolutePath.toString().getBytes()), message);
        try {
            outputStream.write(chubbyNotification.getFormattedMessage().getBytes());
        } catch (IOException e) {
            logger.error("error while writing to output stream", e);
            throw new RuntimeException(e);
        }
    }
}
