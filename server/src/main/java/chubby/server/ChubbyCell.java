package chubby.server;

import chubby.control.handle.ChubbyHandleResponse;
import chubby.control.message.ChubbyMessage;
import chubby.control.message.ChubbyNotification;
import chubby.control.message.ChubbyRequest;
import chubby.control.message.ChubbyResponse;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.Watch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ChubbyCell {
    private static final Logger logger = LogManager.getLogger();
    private static final int BUFFER_SIZE = 1024;
    private static final byte[] buffer = new byte[BUFFER_SIZE];
    private static final String MESSAGE_EXIT = "goodbye!";
    private static ChubbyResponse latestChubbyResponse; //latest chubby response is stored each time to set client's current handle
    private static boolean notifiedInitialLockOnRoot = false;
    private static boolean test = false;
    private static final String[] localCellServers = {
            "http://localhost:10000",
            "http://localhost:10001",
            "http://localhost:10002",
            "http://localhost:10003",
            "http://localhost:10004"
    };
    private static final String[] cell1Servers = {
            "http://localhost:11000",
            "http://localhost:11001",
            "http://localhost:11002",
            "http://localhost:11003",
            "http://localhost:11004"
    };
    private static final String[] cell2Servers = {
            "http://localhost:12000",
            "http://localhost:12001",
            "http://localhost:12002",
            "http://localhost:12003",
            "http://localhost:12004"
    };

    public static void main(String[] args) {
        ChubbyNamespace chubbyNamespace = null;
        String username = args[0];
        int hashedPassword = args[1].hashCode();
        String serverNameToConnectTo = args[2];
        String[] servers = new String[0];

        try {
            switch (serverNameToConnectTo) {
                case "local" -> {
                    chubbyNamespace = new ChubbyNamespace("local");
                    servers = localCellServers;
                }
                case "cell1" -> {
                    chubbyNamespace = new ChubbyNamespace("cell1");
                    servers = cell1Servers;
                }
                case "cell2" -> {
                    chubbyNamespace = new ChubbyNamespace("cell2");
                    servers = cell2Servers;
                }
                default -> {
                    System.out.println("Invalid server name");
                    System.exit(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            //generates a chatroom
            generateChatroom(username, hashedPassword, serverNameToConnectTo + "-" + username, chubbyNamespace, test, servers);
        } catch (IOException e) {
            System.out.println("Cannot use IO");
            System.exit(1);
        } catch (InterruptedException e) {
            System.out.println("ChubbyCell interruption");
            System.exit(1);
        }
    }

    private static void generateChatroom(String username, int hashedPassword, String chatId, ChubbyNamespace chubbyNamespace, boolean isTest, String... servers) throws IOException, InterruptedException {
        try {
            System.out.printf("Contacting host(s) %s...\n", Arrays.toString(servers));
            Client client = Client.builder().endpoints(servers).build();
            System.out.println("Connection established");

            if (!isTest) {
                client.getKVClient().get(ByteSequence.from(username.getBytes())).thenCompose(getResponse -> {
                    if (getResponse.getKvs().isEmpty()) {
                        System.out.println("Invalid username");
                        System.exit(1);
                    } else {
                        String passwordResponseStr = new String(getResponse.getKvs().getFirst().getValue().getBytes());
                        int hashedPasswordResponse = Integer.parseInt(passwordResponseStr);
                        if (hashedPassword == hashedPasswordResponse) {
                            System.out.println("successfully authenticated");
                        } else {
                            System.out.println("Invalid password");
                            System.exit(1);
                        }
                    }
                    return CompletableFuture.completedFuture(null);
                });
            }

            if (isTest) {
                logger.trace("adding initial directories to kv store");
                chubbyNamespace.createDefaultNodes(client).get();
            }

            //gets an initial lock (read mode) to root node with max lock-delay value
            ChubbyHandleResponse initialChubbyHandleResponse = chubbyNamespace.createDefaultHandle(username, client).get();

            chatroomImpl(username, chatId, client, chubbyNamespace, initialChubbyHandleResponse, isTest);

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (InterruptedException | ExecutionException e) {
            // Silently ignores
        }
    }

    private static void chatroomImpl(String username, String chatId, Client client, ChubbyNamespace chubbyNamespace, ChubbyHandleResponse initialChubbyHandleResponse, boolean isTest) throws IOException, ExecutionException, InterruptedException {
        propagateServerToStdout(chatId, client, chubbyNamespace, isTest);
        propagateStdinToServer(username, chatId, client, initialChubbyHandleResponse);
    }

    private static void propagateStdinToServer(String username, @NotNull String chatId, @NotNull Client client, ChubbyHandleResponse initialChubbyHandleResponse) throws IOException, ExecutionException, InterruptedException {
        InputStream inputStream = System.in;

        KV kv = client.getKVClient();

        //chat between specified client and chubby server will be used as key into the kv store
        ByteSequence chatIdKey = ByteSequence.from(chatId.getBytes());

        latestChubbyResponse = new ChubbyResponse(username, null, initialChubbyHandleResponse);

        while (true) {
            int read = inputStream.read(buffer);
            if (read > 0) {
                byte[] requestMsgBodyByte = new byte[read];
                System.arraycopy(buffer, 0, requestMsgBodyByte, 0, read);
                String requestMsgString = new String(requestMsgBodyByte);

                logger.trace("latest chubby response filecontent '{}'", latestChubbyResponse.getChubbyCurrentHandleResponse().getFileContent());

                ChubbyRequest chubbyRequest = new ChubbyRequest(username, latestChubbyResponse, requestMsgString);

                String chubbyRequestJsonString = ChubbyRequestSerializer.serialize(chubbyRequest);
                logger.trace("serialized req '{}'", chubbyRequestJsonString);

                ByteSequence chubbyRequestByteSequence = ByteSequence.from(chubbyRequestJsonString.getBytes());

//                chubbyNamespace.createNode(client, Paths.get("/dir"), "null", ChubbyNodeAttribute.PERMANENT, ChubbyHandleType.WRITE);

                //chat is keyed with key = chatId
                kv.put(chatIdKey, chubbyRequestByteSequence).get();

//                chubbyNamespace.createNode(client, Paths.get("/dir/tmp"), null, ChubbyNodeAttribute.PERMANENT, ChubbyHandleType.READ);

            } else {
                ChubbyRequest chubbyRequest = new ChubbyRequest(username, latestChubbyResponse, "exit");

                String chubbyRequestJsonString = ChubbyRequestSerializer.serialize(chubbyRequest);
                ByteSequence chubbyRequestByteSequence = ByteSequence.from(chubbyRequestJsonString.getBytes());

                kv.put(chatIdKey, chubbyRequestByteSequence).get();
                return;
            }
        }
    }

    private static void propagateServerToStdout(@NotNull String chatId, @NotNull Client client, ChubbyNamespace chubbyNamespace, boolean close) {
        OutputStream outputStream = System.out;
        ChubbyRequestProcessor chubbyRequestProcessor = new ChubbyRequestProcessor();
        Watch.Listener listener = Watch.listener(response -> {
            // offloads the processing to another thread, in order not to block any other operation on the kv store caused by chubbyRequestProcessor
            new Thread(() -> response.getEvents().forEach(event -> {
                ChubbyRequest chubbyRequest = ChubbyRequestDeserializer.deserialize(event);

                try {
                    outputStream.write(chubbyRequest.getFormattedMessage().getBytes());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                ChubbyMessage chubbyMessage = chubbyRequestProcessor.process(chubbyNamespace, chubbyRequest, client);

                if (chubbyMessage instanceof ChubbyResponse) {
                    setLatestChubbyResponse((ChubbyResponse) chubbyMessage);
                }

                try {
                    outputStream.write(chubbyMessage.getFormattedMessage().getBytes());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                if (chubbyMessage instanceof ChubbyNotification && MESSAGE_EXIT.equals(chubbyMessage.getMessage())) {
                    try {
                        outputStream.write("session closed".getBytes());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    if (!close) {
                        System.exit(0);
                    }
                }
            }), "chubby_request_processor").start();
        });

        ByteSequence chatIdKey = ByteSequence.from(chatId.getBytes());

        //define watcher
        Watch watch = client.getWatchClient();
        Watch.Watcher watcher = watch.watch(chatIdKey, listener);
        System.out.println("Listening to new messages on chat \"" + chatIdKey + "\"");

        if (!notifiedInitialLockOnRoot) {
            try {
                outputStream.write(new ChubbyNotification(chubbyNamespace.getRoot(), ByteSequence.from(chubbyNamespace.getRoot().toString().getBytes()), "acquired initial shared lock on root '" + chubbyNamespace.getRoot() + "' node").getFormattedMessage().getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            notifiedInitialLockOnRoot = true;
        }
    }

    private static void setLatestChubbyResponse(ChubbyResponse chubbyResponse) {
        latestChubbyResponse = chubbyResponse;
    }

    protected static void setChubbyCellTestModeTo(boolean testMode) {
        test = testMode;
    }
}