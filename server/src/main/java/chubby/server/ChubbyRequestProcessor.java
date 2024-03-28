package chubby.server;

import chubby.control.handle.*;
import chubby.control.message.*;
import chubby.server.node.ChubbyNode;
import chubby.server.node.ChubbyNodeAttribute;
import chubby.utils.ChubbyUtils;
import chubby.utils.exceptions.ChubbyCannotRemoveHeldNodeException;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

public class ChubbyRequestProcessor {
    private static final Logger logger = LogManager.getLogger();    //logger
    private static final int LOCKDELAY_DEFAULT_VALUE = 60;

    public ChubbyRequestProcessor() {
    }

    //Keep in mind that commands are case-sensitive, but event types aren't (example: 'open' is ok, 'OPEN' won't be
    //recognized. 'file_contents_modified' is ok, 'FILE_CONTENTS_MODIFIED' is ok too)

    /**
     * Processes a chubby request from the client and returns a chubby response message. Executes the client's library
     * methods.
     *
     * @param chubbyNamespace the namespace where the request will be processed
     * @param chubbyRequest   the request to be processed
     * @param client          the client that made the request
     * @return a response to the request
     */
    public ChubbyMessage process(ChubbyNamespace chubbyNamespace, @NotNull ChubbyRequest chubbyRequest, Client client) {
        String requestUsername = chubbyRequest.getUsername();
        Path requestHandleAbsolutePath = Paths.get(chubbyRequest.getHandleAbsolutePath());
        String requestLockId = chubbyRequest.getLockId();
        String requestLeaseId = chubbyRequest.getLeaseId();
        String requestCommand = chubbyRequest.getCommand();
        String[] requestArgs = chubbyRequest.getArgs();
        ChubbyHandleType requestChubbyHandleType = chubbyRequest.getChubbyCurrentHandleType();

        logger.trace("filecontent '{}'", chubbyRequest.getFileContent());
        logger.trace("requested 'chubbyRequest process' with arguments: 'absPath:{}', 'cmd:{}', 'args:{}'", requestHandleAbsolutePath, requestCommand, requestArgs);

        //those are the 'client library' methods that a client may use to make operations into the chubby cell
        return switch (requestCommand) {

            case "echo" -> {
                //arguments are responses to echo command
                String message = String.join(" ", requestArgs);

                logger.trace("detected 'echo' cmd, returning message response: '{}'", message);
                yield new ChubbyResponse(chubbyRequest.getUsername(), message, new ChubbyHandleResponse(chubbyRequest));
            }
            case "open" -> {
                //for reference: open absolute_argumentAbsolutePath handle_type [node_attribute] [lock_delay] [event_sub1 event_sub2 ...]

                if (requestArgs.length < 2) {
                    logger.error("args size < 2, expected at least 2");
                    yield new ChubbyError(chubbyRequest, "expected at least 2 arguments after 'open' command");

                } else {
                    //argument path
                    Path argumentAbsolutePath;

                    argumentAbsolutePath = Path.of(requestArgs[0]);
                    logger.trace("extracted argument 'argumentAbsolutePath:{}'", argumentAbsolutePath);

                    if (requestChubbyHandleType.equals(ChubbyHandleType.WRITE)) {
                        logger.error("detected 'open' cmd while having an exclusive lock on another node, skipping operation...");
                        yield new ChubbyError(chubbyRequest, "cannot execute 'open' cmd while having an exclusive lock on another node");
                    } else if (requestChubbyHandleType.equals(ChubbyHandleType.READ) && !requestHandleAbsolutePath.equals(chubbyNamespace.getRoot())) {
                        logger.error("detected 'open' cmd while having a shared lock on another node that is not root, skipping operation...");
                        yield new ChubbyError(chubbyRequest, "cannot execute 'open' cmd while having a shared lock on another node that is not root");
                    }

                    //handle type
                    ChubbyHandleType argumentChubbyHandleType;
                    try {
                        argumentChubbyHandleType = ChubbyHandleType.valueOf(requestArgs[1].toUpperCase());
                        logger.trace("extracted argument 'argumentChubbyHandleType:{}'", argumentChubbyHandleType);
                    } catch (IllegalArgumentException e) {
                        logger.error("invalid argumentChubbyHandleType '{}'", requestArgs[1]);
                        yield new ChubbyError(chubbyRequest, "invalid argumentChubbyHandleType '" + requestArgs[1] + "'");
                    }

                    //node attribute, lock delay and event subscriptions
                    ChubbyNodeAttribute argumentChubbyNodeAttribute = ChubbyNodeAttribute.PERMANENT;
                    ChubbyLockDelay argumentChubbyLockDelay = new ChubbyLockDelay();
                    String[] argumentEventTypesArray = null;
                    if (requestArgs.length > 2) {

                        try {
                            argumentChubbyNodeAttribute = ChubbyNodeAttribute.valueOf(requestArgs[2].toUpperCase());
                            logger.trace("extracted argument 'argumentNodeAttribute:{}'", argumentChubbyNodeAttribute);

                            if (requestArgs.length > 3) {
                                try {
                                    argumentChubbyLockDelay = new ChubbyLockDelay(requestArgs[3]);

                                    if (requestArgs.length > 4) {
                                        argumentEventTypesArray = Arrays.copyOfRange(requestArgs, 4, requestArgs.length);
                                    }

                                } catch (NumberFormatException e1) {
                                    argumentEventTypesArray = Arrays.copyOfRange(requestArgs, 3, requestArgs.length);
                                }
                            }
                        } catch (IllegalArgumentException e) {
                            try {
                                argumentChubbyLockDelay = new ChubbyLockDelay(requestArgs[2]);

                                if (requestArgs.length > 3) {
                                    argumentEventTypesArray = Arrays.copyOfRange(requestArgs, 3, requestArgs.length);
                                }

                            } catch (NumberFormatException e1) {
                                argumentEventTypesArray = Arrays.copyOfRange(requestArgs, 2, requestArgs.length);
                            }
                        }
                    }

                    ChubbyHandleRequest chubbyHandleRequest = new ChubbyHandleRequest(argumentAbsolutePath, argumentChubbyHandleType, argumentChubbyLockDelay, argumentEventTypesArray);
                    ChubbyHandleResponse chubbyHandleResponse;
                    AtomicReference<ChubbyError> chubbyError = new AtomicReference<>();
                    try {
                        chubbyHandleResponse = chubbyNamespace.createNode(client, argumentAbsolutePath, argumentChubbyNodeAttribute, false).thenCompose(createNodeResponse -> {
                            if (createNodeResponse.wasCreated()) {
                                try {
                                    return chubbyNamespace.inheritACLNames(argumentAbsolutePath, client).thenCompose(inheritResponse -> {
                                        try {
                                            return chubbyNamespace.createHandle(requestUsername, client, chubbyHandleRequest);
                                        } catch (Exception e) {
                                            chubbyError.set(new ChubbyError(chubbyRequest, e.getMessage()));
                                            return null;    //this null won't be used
                                        }
                                    });
                                } catch (Exception e) {
                                    chubbyError.set(new ChubbyError(chubbyRequest, e.getMessage()));
                                    return null;    //this null won't be used
                                }
                            } else {
                                try {
                                    return chubbyNamespace.isClientPermittedAccess(requestUsername, client, chubbyHandleRequest.getChubbyHandleType(), argumentAbsolutePath).thenCompose(permitted -> {
                                        if (!permitted) {
                                            chubbyError.set(new ChubbyError(chubbyRequest, "user '" + requestUsername + "' not permitted to '" + chubbyHandleRequest.getChubbyHandleType().toString().toLowerCase() + "' on node '" + argumentAbsolutePath + "'"));
                                            return CompletableFuture.completedFuture(null);
                                        } else {
                                            try {
                                                return chubbyNamespace.createHandle(requestUsername, client, chubbyHandleRequest);
                                            } catch (Exception e) {
                                                chubbyError.set(new ChubbyError(chubbyRequest, e.getMessage()));
                                                return null;    //this null won't be used
                                            }
                                        }
                                    });
                                } catch (Exception e) {
                                    chubbyError.set(new ChubbyError(chubbyRequest, e.getMessage()));
                                    return null;    //this null won't be used
                                }
                            }
                        }).get();

                        //if permission was not granted, return error message
                        if (chubbyError.get() != null) {
                            yield chubbyError.get();
                        }

                        //if null, the node is already exclusively locked by another client
                        if (chubbyHandleResponse == null) {
                            yield new ChubbyError(chubbyRequest, "specified node is already exclusively locked by another client");
                        }

                        //unlock the old lock
                        try {
                            chubbyNamespace.unlock(chubbyRequest, client, true).get();
                        } catch (Exception e) {
                            yield new ChubbyError(chubbyRequest, e.getMessage());
                        }
                    } catch (Exception e) {
                        logger.error(e);
                        yield new ChubbyError(chubbyRequest, e.getMessage());
                    }

                    yield new ChubbyResponse(requestUsername, "successfully opened node", chubbyHandleResponse);

                }
            }
            case "close" -> {
                logger.trace("detected 'close' cmd, processing it...");

                boolean ephemeralNodeRemoved = false;
                String message;
                try {
                    logger.trace("about to execute method 'tryRemoveIfEphemeral'");

                    //if the node is ephemeral, try to remove and unlock it
                    ephemeralNodeRemoved = chubbyNamespace.tryRemoveIfEphemeral(chubbyRequest, client).get();

                } catch (Exception e) {
                    logger.error("caught exception '{}'", e.getMessage());
                    logger.error("caught exception '{}'", e.getCause().getCause().getMessage());

                    if (e.getCause().getCause() instanceof ChubbyCannotRemoveHeldNodeException) {
                        logger.trace("node is ephemeral, but held by another client, unlocking node for this client");

                        //if the node is ephemeral, but held by another client, the node is not deleted, but this client has to unlock it anyway
//                        try {
//                            logger.trace("about to execute unlock method");
//                            chubbyNamespace.unlock(chubbyRequest, client, false).get();
//
//                        } catch (Exception e2) {
//                            logger.error("caught exception '{}', about to send chubby error message", e2.getMessage());
//                            yield new ChubbyError(chubbyRequest, e2.getMessage());
//                        }
                    } else {
                        logger.trace("about to send chubby error message about previously caught exception");
                        yield new ChubbyError(chubbyRequest, e.getMessage());
                    }
                }

                //unlock the node if it was not removed
                if (!ephemeralNodeRemoved) {
                    try {
                        logger.trace("about to execute unlock method");
                        chubbyNamespace.unlock(chubbyRequest, client, false).get();

                    } catch (Exception e2) {
                        logger.error("caught exception '{}', about to send chubby error message", e2.getMessage());
                        yield new ChubbyError(chubbyRequest, e2.getMessage());
                    }
                }

                //lock (read mode) on root node has to always be guaranteed
                ChubbyHandleResponse chubbyHandleResponse;
                try {
                    logger.trace("about to create handle");
                    chubbyHandleResponse = chubbyNamespace.createHandle(requestUsername, client, new ChubbyHandleRequest(chubbyNamespace.getRoot(), ChubbyHandleType.READ, new ChubbyLockDelay(LOCKDELAY_DEFAULT_VALUE), ChubbyEventType.NONE.toString())).get();

                } catch (Exception e) {
                    logger.error("caught exception '{}', about to send chubby error message", e.getMessage());
                    yield new ChubbyError(requestHandleAbsolutePath, chubbyNamespace.getRoot(), e.getMessage());
                }

                message = "lock released, successfully acquired shared lock on root node";
                logger.trace("successfully execute cmd, about to send chubby response message '{}'", message);
                yield new ChubbyResponse(requestUsername, message, chubbyHandleResponse);
            }
            case "remove" -> {
                logger.trace("detected 'remove' cmd, processing it...");

                try {
                    logger.trace("about to call method 'removeNode'");
                    chubbyNamespace.removeNode(chubbyRequest, client).get();

                } catch (Exception e) {
                    logger.error("caught exception '{}' about to send chubby error message", e.getMessage());
                    yield new ChubbyError(chubbyRequest, e.getMessage());
                }

                ChubbyHandleResponse chubbyHandleResponse;
                try {
                    logger.trace("about to execute 'createHandle' method");
                    chubbyHandleResponse = chubbyNamespace.createHandle(requestUsername, client, new ChubbyHandleRequest(chubbyNamespace.getRoot(), ChubbyHandleType.READ, new ChubbyLockDelay(LOCKDELAY_DEFAULT_VALUE), ChubbyEventType.NONE.toString())).get();

                } catch (Exception e) {
                    logger.error("caught exception '{}', about to send chubby error message", e.getMessage());
                    yield new ChubbyError(requestHandleAbsolutePath, chubbyNamespace.getRoot(), e.getMessage());
                }

                String message = "node removed, successfully acquired shared lock on root node";
                logger.trace("successfully execute cmd, about to send chubby response message '{}'", message);
                yield new ChubbyResponse(requestUsername, message, chubbyHandleResponse);
            }
            case "write" -> {
                logger.debug("detected 'write' cmd, processing it...");

                //for reference:
                //write filecontent [file content]
                //write acl aclType customName --> where aclType can be READ, WRITE, CHANGE_ACL and customName the new name

                ByteSequence resultMessage;
                if (requestArgs.length > 0) {
                    switch (requestArgs[0]) {
                        case "filecontent" -> {
                            logger.debug("detected 'write filecontent' cmd, processing it...");

                            String message = String.join(" ", Arrays.copyOfRange(requestArgs, 1, requestArgs.length));
                            logger.trace("merged request args: '{}'", message);

                            try {
                                logger.debug("about to execute 'write' method");
                                resultMessage = chubbyNamespace.write(chubbyRequest, client, message).get();
                                chubbyRequest.setFileContent(message);

                            } catch (Exception e) {
                                logger.error("caught exception '{}', about to send chubby error message", e.getMessage());
                                yield new ChubbyError(chubbyRequest, e.getMessage());
                            }
                        }
                        case "acl" -> {
                            logger.debug("detected 'write acl' cmd, processing it...");

                            if (requestArgs.length == 3) {
                                if (requestArgs[1].equalsIgnoreCase(ChubbyHandleType.READ.toString())) {
                                    try {
                                        resultMessage = chubbyNamespace.changeACLNames(requestHandleAbsolutePath, client, requestUsername, requestChubbyHandleType, ChubbyHandleType.READ, String.valueOf(requestArgs[2])).get();
                                    } catch (Exception e) {
                                        yield new ChubbyError(chubbyRequest, e.getMessage());
                                    }
                                } else if (requestArgs[1].equalsIgnoreCase(ChubbyHandleType.WRITE.toString())) {
                                    try {
                                        resultMessage = chubbyNamespace.changeACLNames(requestHandleAbsolutePath, client, requestUsername, requestChubbyHandleType, ChubbyHandleType.WRITE, String.valueOf(requestArgs[2])).get();
                                    } catch (Exception e) {
                                        yield new ChubbyError(chubbyRequest, e.getMessage());
                                    }
                                } else if (requestArgs[1].equalsIgnoreCase(ChubbyHandleType.CHANGE_ACL.toString())) {
                                    try {
                                        resultMessage = chubbyNamespace.changeACLNames(requestHandleAbsolutePath, client, requestUsername, requestChubbyHandleType, ChubbyHandleType.CHANGE_ACL, String.valueOf(requestArgs[2])).get();
                                    } catch (Exception e) {
                                        yield new ChubbyError(chubbyRequest, e.getMessage());
                                    }
                                } else {
                                    yield new ChubbyError(chubbyRequest, "acl type '" + requestArgs[1] + "' not recognized, possible types are 'read','write','change_acl'");
                                }
                            } else {
                                yield new ChubbyError(chubbyRequest, "expected exactly 3 arguments for 'write acl' command, syntax is: 'write acl *aclType* *newCustomName*");
                            }
                        }
                        case "add_client" -> {
                            if (requestArgs.length >= 3) {
                                if (requestArgs[1].equalsIgnoreCase(ChubbyHandleType.READ.toString())) {
                                    try {
                                        resultMessage = chubbyNamespace.addACLClient(requestHandleAbsolutePath, client, ChubbyHandleType.READ, requestChubbyHandleType, Arrays.copyOfRange(requestArgs, 2, requestArgs.length)).get();
                                    } catch (Exception e) {
                                        yield new ChubbyError(chubbyRequest, e.getMessage());
                                    }
                                } else if (requestArgs[1].equalsIgnoreCase(ChubbyHandleType.WRITE.toString())) {
                                    try {
                                        resultMessage = chubbyNamespace.addACLClient(requestHandleAbsolutePath, client, ChubbyHandleType.WRITE, requestChubbyHandleType, Arrays.copyOfRange(requestArgs, 2, requestArgs.length)).get();
                                    } catch (Exception e) {
                                        yield new ChubbyError(chubbyRequest, e.getMessage());
                                    }
                                } else if (requestArgs[1].equalsIgnoreCase(ChubbyHandleType.CHANGE_ACL.toString())) {
                                    try {
                                        resultMessage = chubbyNamespace.addACLClient(requestHandleAbsolutePath, client, ChubbyHandleType.CHANGE_ACL, requestChubbyHandleType, Arrays.copyOfRange(requestArgs, 2, requestArgs.length)).get();
                                    } catch (Exception e) {
                                        yield new ChubbyError(chubbyRequest, e.getMessage());
                                    }
                                } else {
                                    yield new ChubbyError(chubbyRequest, "acl type '" + requestArgs[1] + "' not recognized, possible types are 'read','write','change_acl'");
                                }
                            } else {
                                yield new ChubbyError(chubbyRequest, "expected >= 3 arguments for 'write acl' command, syntax is: 'write add_client *aclType* *clientName1 clientName2 ...*");
                            }
                        }
                        default -> {
                            logger.error("argument '{}' not recognized, about to send chubby message error", requestArgs[0]);
                            yield new ChubbyError(chubbyRequest, "wrong argument, expected 'filecontent' or 'acl'");
                        }
                    }
                } else {
                    yield new ChubbyError(chubbyRequest, "missing argument, expected 'filecontent' or 'acl'");
                }

                logger.debug("successfully executed cmd, about to send chubby response message '{}'", resultMessage);
                yield new ChubbyResponse(requestUsername, Objects.requireNonNullElse(resultMessage, "").toString(), new ChubbyHandleResponse(chubbyRequest));
            }
            case "read" -> {
                //read filecontent
                //read acl

                if (requestArgs.length > 0) {
                    if (requestArgs[0].equals("filecontent")) {
                        if (ChubbyUtils.isFile(Path.of(chubbyRequest.getHandleAbsolutePath()))) {
                            yield new ChubbyResponse(requestUsername, chubbyRequest.getFileContent(), new ChubbyHandleResponse(chubbyRequest));
                        } else {
                            yield new ChubbyError(chubbyRequest, "cannot retrieve 'filecontent' from directory node");
                        }
                    } else if (requestArgs[0].equals("acl")) {
                        ChubbyNode responseChubbyNode;
                        try {
                            responseChubbyNode = chubbyNamespace.getNode(client, requestHandleAbsolutePath).get();
                        } catch (InterruptedException | ExecutionException e) {
                            logger.error(e);
                            yield new ChubbyError(chubbyRequest, "something went wrong while executing 'read acl' command'");
                        }
                        yield new ChubbyResponse(requestUsername, responseChubbyNode.getNodeValue().getMetadata().getAclNamesMap().toString(), new ChubbyHandleResponse(chubbyRequest));
                    } else {
                        yield new ChubbyError(chubbyRequest, "wrong argument, possible arguments: 'filecontent', 'acl'");
                    }
                } else {
                    yield new ChubbyError(chubbyRequest, "expecting one argument after 'read' command, possible arguments: 'filecontent', 'acl'");
                }
            }
            case "node" -> {
                if (requestArgs.length > 0) {
                    ChubbyNode responseChubbyNode;
                    try {
                        responseChubbyNode = chubbyNamespace.getNode(client, requestHandleAbsolutePath).get();
                    } catch (InterruptedException | ExecutionException e) {
                        logger.error(e);
                        yield new ChubbyError(chubbyRequest, "something went wrong while executing 'node' command'");
                    }

                    if (requestArgs[0].equals("data")) {
                        yield new ChubbyResponse(requestUsername, responseChubbyNode.getAbsolutePath().toString() + ":" + responseChubbyNode.getNodeValue().toString(), new ChubbyHandleResponse(chubbyRequest));
                    } else if (requestArgs[0].equals("metadata")) {
                        yield new ChubbyResponse(requestUsername, responseChubbyNode.getAbsolutePath().toString() + ":" + responseChubbyNode.getNodeValue().getMetadata().toString(), new ChubbyHandleResponse(chubbyRequest));
                    } else {
                        yield new ChubbyError(chubbyRequest, "wrong argument, possible arguments: 'data', 'metadata'");
                    }
                } else {
                    yield new ChubbyError(chubbyRequest, "expecting one argument after 'node' command, possible arguments: 'data', 'metadata'");
                }
            }
            case "ls" -> {
                logger.trace("detected 'ls' cmd");

                try {
                    int depth = 1;
                    if (requestArgs.length > 0) {
                        try {
                            depth = Integer.parseInt(requestArgs[0]);
                            logger.trace("detected depth '{}'", requestArgs[0]);
                        } catch (NumberFormatException e) {
                            logger.error("invalid depth '{}'", requestArgs[0]);
                            yield new ChubbyError(chubbyRequest, "invalid depth '" + requestArgs[0] + "'");
                        }

                        //if user puts path < 1, set it to default 1 value
                        if (depth < 1) {
                            depth = 1;
                        }
                    }
                    String message = "\n" + String.join("\n", chubbyNamespace.getLs(client, requestHandleAbsolutePath, depth).get());
                    yield new ChubbyResponse(requestUsername, message, new ChubbyHandleResponse(chubbyRequest));

                } catch (InterruptedException | ExecutionException e) {
                    logger.error("something went wrong", e);
                    yield new ChubbyError(chubbyRequest, "something went wrong while executing 'ls' command");
                }
            }
            case "curr_handle" -> {
                String message = "lock type: '" + requestChubbyHandleType + "' on path: '" + requestHandleAbsolutePath + "'";
                yield new ChubbyResponse(requestUsername, message, new ChubbyHandleResponse(chubbyRequest));
            }
            case "list" -> {
                if (requestArgs.length > 0) {
                    switch (requestArgs[0]) {
                        case "event" -> {
                            StringBuilder message = new StringBuilder("\n");
                            for (ChubbyEventType eventType : ChubbyEventType.values()) {
                                message.append("- ").append(eventType.name()).append("\n");
                            }
                            yield new ChubbyResponse(requestUsername, message.toString(), new ChubbyHandleResponse(chubbyRequest));
                        }
                        case "defnode" -> {
                            StringBuilder message = new StringBuilder("\n");
                            for (Path defNodePath : chubbyNamespace.getDefaultNodeList()) {
                                message.append("- ").append(defNodePath).append("\n");
                            }
                            yield new ChubbyResponse(requestUsername, message.toString(), new ChubbyHandleResponse(chubbyRequest));
                        }
                        case "cmd" -> {
                            String message;
                            if (requestChubbyHandleType.equals(ChubbyHandleType.WRITE)) {
                                logger.trace("detected 'list cmd' cmd from a 'WRITE' handle type");

                                message = """
                                        commands that can be used from this handle:
                                        - echo [msg]
                                        - close
                                        - write filecontent [content]
                                        - read filecontent
                                        - read acl
                                        - node data
                                        - node metadata
                                        - ls [depth]
                                        - list event
                                        - list defnode
                                        - list cmd
                                        - help""";

                            } else if (requestChubbyHandleType.equals(ChubbyHandleType.READ)) {
                                logger.trace("detected 'list cmd' cmd from a 'READ' handle type");

                                message = """
                                        commands that can be used from this handle:
                                        - echo [msg]
                                        - *close
                                        - **open absolutePath handle_type [node_attribute] [lock_delay] [event_sub1 event_sub2 ...]
                                        - read filecontent
                                        - read acl
                                        - node data
                                        - node metadata
                                        - ls [depth]
                                        - list event
                                        - list defnode
                                        - list cmd
                                        - help
                                        *only while having a handle not on root node
                                        **only while having a handle on root node""";

                            } else if (requestChubbyHandleType.equals(ChubbyHandleType.CHANGE_ACL)) {
                                logger.trace("detected 'list cmd' cmd from a 'CHANGE_ACL' handle type");

                                message = """
                                        commands that can be used from this handle:
                                        - echo [msg]
                                        - close
                                        - write acl *aclType* *newAclName*
                                        - write add_client *aclType* *client1Name client2Name ...*
                                        - read filecontent
                                        - read acl
                                        - node data
                                        - node metadata
                                        - ls [depth]
                                        - list event
                                        - list defnode
                                        - list cmd
                                        - help""";

                            } else {
                                logger.trace("detected 'list cmd' cmd from 'NONE' handle type");

                                message = """
                                        list of all commands available:
                                        - echo [msg]
                                        - open absolutePath handle_type [node_attribute] [lock_delay] [event_sub1 event_sub2 ...]
                                        - close
                                        - write file_content [content]
                                        - write acl *aclType* *newAclName*
                                        - write add_client *aclType* *client1Name client2Name ...*
                                        - read filecontent
                                        - read acl
                                        - node data
                                        - node metadata
                                        - ls [depth]
                                        - list event
                                        - list defnode
                                        - list cmd
                                        - help""";
                            }
                            yield new ChubbyResponse(requestUsername, message, new ChubbyHandleResponse(chubbyRequest));
                        }
                        default -> {
                            yield new ChubbyError(chubbyRequest, "no matching argument found, possible arguments are 'event', 'defnode', 'cmd'");
                        }
                    }
                } else {
                    yield new ChubbyError(chubbyRequest, "expected at least 1 argument to 'list' cmd, possible arguments are 'event', 'defnode', 'cmd'");
                }
            }
            case "help" -> {
                logger.trace("detected 'help' cmd");

                String message = """
                        list of all commands available:
                        - echo [msg], returns the same arguments passed as input
                        - open absolutePath handle_type [node_attribute] [lock_delay] [event_sub1 event_sub2 ...], opens a handle on 'absolute_argumentPath' creating the node if not already present, user has to define how the handle will be used (write, read, change_acl), an optional 'node_attribute' (permanent, ephemeral) can be defined to make the node permanent or temporary (if not specified a default 'permanent' value will be set), a 'lock_delay' between 0 and 60 seconds can be defined to prevent master from releasing the lock if no keep_alive from client is received for more than the amount of time defined here, values < 0 will be set at '0', values > 60 will be set at '60' (if not specified a default value of 30s will be assigned to it automatically), optionally event subscriptions may be included to receive notifications about the chubbyNodeValue (FILE_CONTENTS_MODIFIED, CHILD_NODE_ADDED, CHILD_NODE_REMOVED, CHILD_NODE_MODIFIED, HANDLE_INVALID, CONFLICTING_LOCK_REQUEST)
                        - close, closes an open handle, releases the lock and tries to remove the node if ephemeral
                        - write file_content [content], overwrites the file content, that follows the command, into this node; an open handle with 'WRITE' lock is needed in order to use this command
                        - write acl *permissionType* *newPermissionName*, changes the acl name of this node's specified acl type; an open handle with 'CHANGE_ACL' lock is needed in order to use this command
                        - node data, returns the node's file content and metadata
                        - node metadata, returns the node's metadata
                        - ls [depth], prints each child node of current path, optional depth can be set (if not set, depth 1 is automatically applied)
                        - list event, prints the full list of possible event subscriptions that can activated through 'open' command
                        - list defnode, prints the full list of default nodes
                        - list cmd, prints the full list of possible commands and arguments""";
                yield new ChubbyResponse(requestUsername, message, new ChubbyHandleResponse(chubbyRequest));
            }
            case "exit" -> {
                logger.trace("detected 'exit' cmd, processing it...");

                try {
                    logger.trace("about to execute unlock method");
                    chubbyNamespace.unlock(chubbyRequest, client, true).get();

                } catch (Exception e2) {
                    logger.error("caught exception '{}', about to send chubby error message", e2.getMessage());
                    yield new ChubbyError(chubbyRequest, e2.getMessage());
                }

                yield new ChubbyNotification(null, null, "goodbye!");
            }
            default -> {
                logger.trace("no matching command found: '{}', returning chubby error", requestCommand);
                yield new ChubbyError(chubbyRequest, "no matching command found: '" + requestCommand + "', for a list of possible commands digit 'list cmd', for a detailed explanation digit 'help'");
            }
        };
    }
}
