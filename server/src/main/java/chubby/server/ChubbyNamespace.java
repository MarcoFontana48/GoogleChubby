package chubby.server;

import chubby.control.handle.ChubbyHandleRequest;
import chubby.control.handle.ChubbyHandleResponse;
import chubby.control.handle.ChubbyHandleType;
import chubby.control.handle.ChubbyLockDelay;
import chubby.control.message.ChubbyRequest;
import chubby.server.node.*;
import chubby.utils.ChubbyUtils;
import chubby.utils.exceptions.*;
import com.google.gson.reflect.TypeToken;
import io.etcd.jetcd.*;
import io.etcd.jetcd.kv.DeleteResponse;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.kv.PutResponse;
import io.etcd.jetcd.options.GetOption;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class ChubbyNamespace {
    private static final Charset charset = StandardCharsets.UTF_8;
    private static final Logger logger = LogManager.getLogger();
    private static final int MAX_LOCKDELAY_SECONDS = 60;
    private final Path rootPath;
    private final Path aclNodeAbsolutePath;
    private final Path aclWriteFileAbsolutePath;
    private final Path aclReadFileAbsolutePath;
    private final Path aclChangeACLFileAbsolutePath;
    private final Path cellNameAbsolutePath;
    private final List<Path> defaultNodesCompleteList;
    private final List<Path> defaultNodesToCreate;
    private List<Watch.Watcher> watcherResponse;

    /**
     * constructor
     */
    public ChubbyNamespace(String cellName) {
        this.rootPath = Path.of("/");
        this.aclNodeAbsolutePath = this.rootPath.resolve("ls/" + cellName + "/acl");
        this.aclWriteFileAbsolutePath = this.rootPath.resolve("ls/" + cellName + "/acl/" + "write.txt");
        this.aclReadFileAbsolutePath = this.rootPath.resolve("ls/" + cellName + "/acl/" + "read.txt");
        this.aclChangeACLFileAbsolutePath = this.rootPath.resolve("ls/" + cellName + "/acl/" + "change_acl.txt");

        this.cellNameAbsolutePath = this.rootPath.resolve("ls/" + cellName);

        //root has to be the last element of the list to correctly create each node due to the recursive method 'parentCreateNode'
        this.defaultNodesToCreate = List.of(
                this.aclWriteFileAbsolutePath,
                this.aclReadFileAbsolutePath,
                this.aclChangeACLFileAbsolutePath,
                this.rootPath);

        this.defaultNodesCompleteList = List.of(
                this.rootPath,
                this.rootPath.resolve("ls"),
                this.rootPath.resolve("ls/" + cellName),
                this.rootPath.resolve("ls/" + cellName + "/acl"),
                this.aclWriteFileAbsolutePath,
                this.aclReadFileAbsolutePath,
                this.aclChangeACLFileAbsolutePath);
    }

    /**
     * Create a node in the namespace and its parent nodes if they don't exist.
     * Nodes are stored into etcd's key-value store as key-value pairs. The key is the absolute path of the node and the
     * value is a json serialized ChubbyNodeValue.
     *
     * @param client              etcd client
     * @param absolutePath        absolute path of the node to be created
     * @param chubbyNodeAttribute attribute of the node to be created (PERMANENT or EPHEMERAL)
     * @param isSetup             true if it's a setup operation, false otherwise
     * @return a CompletableFuture containing the response
     * @throws ChubbyLockException if a lock cannot be acquired
     * @throws ChubbyNodeException if the node cannot be created nor accessed
     */
    public CompletableFuture<ChubbyCreateNodeResponse> createNode(@NotNull Client client, @NotNull Path absolutePath, ChubbyNodeAttribute chubbyNodeAttribute, boolean isSetup) throws ChubbyLockException, ChubbyNodeException {
        logger.trace("requested node creation: 'path:{}'", absolutePath);

        this.checkCreateNodeOnIllegalPath(absolutePath, isSetup);

        ChubbyNode retChubbyNode = new ChubbyNode(absolutePath, null, chubbyNodeAttribute);

        KV kvClient = client.getKVClient();
        ByteSequence absolutePathByteSequence = ByteSequence.from(retChubbyNode.getAbsolutePath().toString().getBytes());

        logger.trace("about to retrieve key from kv store '{}'", absolutePathByteSequence);
        return kvClient.get(absolutePathByteSequence).thenCompose(getResponse -> {
            //if node is already present into kv store, skip put operation
            if (getResponse.getCount() > 0) {
                logger.trace("node '{}' already present into kv store, skipping put operation...", getResponse.getKvs().getFirst().getKey());
                return CompletableFuture.completedFuture(new ChubbyCreateNodeResponse(retChubbyNode, false));

            } else {
                logger.trace("node '{}' not already present into kv store, adding it...", absolutePathByteSequence);

                ByteSequence chubbyNodeValueByteSequence = ByteSequence.from(ChubbyNodeValueSerializer.serialize(retChubbyNode.getNodeValue()).getBytes());

                logger.trace("key-value about to be put: 'k:{}','v:{}'", absolutePathByteSequence, chubbyNodeValueByteSequence);
                return kvClient.put(absolutePathByteSequence, chubbyNodeValueByteSequence).thenCompose(putResponse -> {
                    logger.trace("added node '{}' to kv store", absolutePathByteSequence.toString());

                    //parent nodes are always permanent
                    return this.createParentNodes(client, retChubbyNode.getAbsolutePath().getParent(), ChubbyNodeAttribute.PERMANENT).thenCompose(v -> CompletableFuture.completedFuture(new ChubbyCreateNodeResponse(retChubbyNode, true)));
                });
            }
        });
    }

    /**
     * Create parent nodes recursively if they don't exist.
     *
     * @param client              etcd client
     * @param parentPath          parent path
     * @param chubbyNodeAttribute attribute of the node to be created (PERMANENT or EPHEMERAL)
     * @return a CompletableFuture 'null' not to be used anywhere, used only to detect when to stop the recursion
     */
    private @Nullable CompletableFuture<Object> createParentNodes(@NotNull Client client, @NotNull Path parentPath, ChubbyNodeAttribute chubbyNodeAttribute) {
        if (parentPath == null) {
            return CompletableFuture.completedFuture(null);
        }

        KV kvClient = client.getKVClient();
        ByteSequence parentPathByteSequence = ByteSequence.from(parentPath.toString().getBytes());

        return kvClient.get(parentPathByteSequence).thenCompose(getResponse -> {
            CompletableFuture<PutResponse> future;
            if (getResponse.getCount() > 0) {
                ChubbyNodeValue parentChubbyNodeValue = ChubbyNodeValueDeserializer.deserialize(getResponse.getKvs().getFirst().getValue().toString());
                future = this.getLs(client, Path.of(getResponse.getKvs().getFirst().getKey().toString()), 1).thenCompose(list -> {

                    //ignores locks
                    List<String> listWithOnlyNodes = list.stream().filter(e -> !e.startsWith("/")).toList();

                    parentChubbyNodeValue.getMetadata().setChildNodeNumber(listWithOnlyNodes.size());
                    String parentChubbyNodeValueJsonStr = ChubbyNodeValueSerializer.serialize(parentChubbyNodeValue);
                    return kvClient.put(parentPathByteSequence, ByteSequence.from(parentChubbyNodeValueJsonStr.getBytes()));
                });
            } else {
                ChubbyNode parentChubbyNode = new ChubbyNode(Path.of(ByteSequence.from(parentPath.toString().getBytes()).toString()), null, chubbyNodeAttribute);
                future = this.getLs(client, parentChubbyNode.getAbsolutePath(), 1).thenCompose(list -> {

                    //ignores locks
                    List<String> listWithOnlyNodes = list.stream().filter(e -> !e.startsWith("/")).toList();

                    parentChubbyNode.getNodeValue().getMetadata().setChildNodeNumber(listWithOnlyNodes.size());
                    String parentChubbyNodeValueJsonStr = ChubbyNodeValueSerializer.serialize(parentChubbyNode.getNodeValue());
                    return kvClient.put(parentPathByteSequence, ByteSequence.from(parentChubbyNodeValueJsonStr.getBytes()));
                });
            }
            return future.thenCompose(putResponse -> {
                if (parentPath.equals(this.rootPath)) {
                    return CompletableFuture.completedFuture(null);
                }
                return this.createParentNodes(client, parentPath.getParent(), chubbyNodeAttribute);
            });
        });
    }

    /**
     * Check if the create node operation is being performed on an illegal path. Illegal paths are the default nodes paths
     *
     * @param absolutePath absolute path of the node to be created
     * @param isSetup     true if it's a setup operation, false otherwise
     * @throws ChubbyNodeException if the node cannot be created nor accessed because it's on an illegal path
     */
    private void checkCreateNodeOnIllegalPath(Path absolutePath, boolean isSetup) throws ChubbyNodeException {
        if (!isSetup && this.isDefaultNode(absolutePath)) {
            logger.error("cannot re-create, nor access, default node '{}'", absolutePath);
            throw new ChubbyNodeException("cannot re-create, nor access, default node " + absolutePath);
        }

        if (!isSetup && (this.isDefaultNode(absolutePath.getParent()) && !absolutePath.getParent().equals(this.cellNameAbsolutePath))) {
            logger.error("cannot create child of a default node except for '{}'", this.cellNameAbsolutePath);
            throw new ChubbyNodeException("cannot create child of a default node except for '" + this.cellNameAbsolutePath + "'");
        }

        if (!isSetup && !absolutePath.startsWith(this.cellNameAbsolutePath)) {
            logger.error("cannot create node here '{}', all nodes of this cell must start with '{}'", absolutePath, this.cellNameAbsolutePath);
            throw new ChubbyNodeException("cannot create node here '" + absolutePath + "', all nodes of this cell must start with '" + this.cellNameAbsolutePath + "'");
        }
    }

    /**
     * Creates a handle object with the lock information, the handle type and the event subscriptions requested by the
     * client on the specified node, with whom the client can interact with the node.
     *
     * @param username            username
     * @param client              etcd client
     * @param chubbyHandleRequest handle request from client
     * @return a CompletableFuture containing the response with the handle information, or null if the node is already
     * locked
     * @throws ChubbyLockException   if a lock cannot be acquired
     * @throws ChubbyHandleException if the handle type is not recognized
     */
    public CompletableFuture<ChubbyHandleResponse> createHandle(String username, Client client, @NotNull ChubbyHandleRequest chubbyHandleRequest) throws ChubbyLockException, ChubbyHandleException {
        logger.trace("requested handle creation on: 'handleAbsolutePath:{}', 'chubbyHandleType:{}', 'chubbyEventTypeArrayList:{}', 'lockdelay:{}'", chubbyHandleRequest.getRequestedAbsolutePath(), chubbyHandleRequest.getChubbyHandleType(), chubbyHandleRequest.getChubbyEventTypeList(), chubbyHandleRequest.getChubbyLockDelay().getValue());

        ByteSequence handleAbsolutePathByteSequence = ByteSequence.from(chubbyHandleRequest.getRequestedAbsolutePath().getBytes(charset));

        /* --- exclusive (write or change_acl) lock --- */
        if (chubbyHandleRequest.getChubbyHandleType().equals(ChubbyHandleType.WRITE) || chubbyHandleRequest.getChubbyHandleType().equals(ChubbyHandleType.CHANGE_ACL)) {

            if (this.isDefaultNode(Path.of(chubbyHandleRequest.getRequestedAbsolutePath()))) {
                logger.error("cannot get exclusive lock on '{}' since it's a default node", chubbyHandleRequest.getRequestedAbsolutePath());
                throw new ChubbyLockException("cannot get exclusive lock on '" + chubbyHandleRequest.getRequestedAbsolutePath() + "' since it's a default node");
            }

            Lease leaseClient = client.getLeaseClient();
            Lock lockClient = client.getLockClient();
            KV kvClient = client.getKVClient();

            return this.getLs(client, handleAbsolutePathByteSequence, 1).thenCompose(lsResponse -> {
                logger.trace(lsResponse);
                //checks if node is already exclusively locked (only lock IDs start with a '/', paths use backslash '\')
                boolean locked = lsResponse.stream().anyMatch(s -> s.startsWith("/"));
                if (locked) {
                    logger.trace("cannot obtain lock on '{}' because it's already exclusively locked", handleAbsolutePathByteSequence);

                    logger.trace("about to retrieve node from kv store '{}'", handleAbsolutePathByteSequence);
                    return kvClient.get(handleAbsolutePathByteSequence).thenCompose(getResponse -> {
                        logger.trace("increasing number of lock requests by 1...");
                        ChubbyNodeValue chubbyNodeValue = ChubbyNodeValueDeserializer.deserialize(getResponse.getKvs().getFirst().getValue().toString());

                        logger.trace("before lock request increase '{}'", chubbyNodeValue.getMetadata().getLockRequestNumber());
                        chubbyNodeValue.getMetadata().increaseLockRequestNumber();
                        logger.trace("after lock request increase '{}'", chubbyNodeValue.getMetadata().getLockRequestNumber());

                        ByteSequence updatedChubbyNodeValueByteSequence = ByteSequence.from(ChubbyNodeValueSerializer.serialize(chubbyNodeValue).getBytes());

                        logger.trace("about to put 'k={}, v={}' into kv store", handleAbsolutePathByteSequence, updatedChubbyNodeValueByteSequence);
                        return kvClient.put(handleAbsolutePathByteSequence, updatedChubbyNodeValueByteSequence).thenCompose(putResponse -> {
//                            throw new RuntimeException(new ChubbyLockException("specified node is already exclusively locked"));
                            logger.error("specified node is already exclusively locked");
                            return CompletableFuture.completedFuture(null);
                        });
                    });
                } else {
                    // if not already locked, get the lock
                    // grant a lease with TTL of lockdelay seconds
                    logger.trace("node '{}' not already exclusively locked, granting lock...", handleAbsolutePathByteSequence);
                    logger.trace("about to grant lease of 'lockDelay={}' seconds...", chubbyHandleRequest.getChubbyLockDelay().getValue());
                    return leaseClient.grant(chubbyHandleRequest.getChubbyLockDelay().getValue()).thenCompose(leaseGrantResponse -> {
                        logger.trace("activating keep alive with 'id={}'", leaseGrantResponse.getID());
                        leaseClient.keepAlive(leaseGrantResponse.getID(), new ChubbyLockObserver<>(username, chubbyHandleRequest, client));

                        logger.trace("granting lock on '{}' node", handleAbsolutePathByteSequence);
                        return lockClient.lock(handleAbsolutePathByteSequence, leaseGrantResponse.getID()).thenCompose(lockResponse -> {

                            //process subscriptions
                            this.watcherResponse = ChubbySubscribeProcessor.process(client, Path.of(chubbyHandleRequest.getRequestedAbsolutePath()), chubbyHandleRequest.getChubbyHandleType(), chubbyHandleRequest.getChubbyEventTypeList());

                            //update lock generation number and client lock map
                            AtomicLong nodeLockGenerationNumber = new AtomicLong();
                            return kvClient.get(handleAbsolutePathByteSequence).thenCompose(getResponse -> {
                                String chubbyNodeValueJsonString = getResponse.getKvs().getFirst().getValue().toString();

                                ChubbyNodeValue chubbyNodeValue = ChubbyNodeValueDeserializer.deserialize(chubbyNodeValueJsonString);

                                logger.trace("updating lock client map with 'k={}, v={}'", username, chubbyHandleRequest.getChubbyHandleType());
                                chubbyNodeValue.getMetadata().addClientLock(username, chubbyHandleRequest.getChubbyHandleType());
                                nodeLockGenerationNumber.set(chubbyNodeValue.getMetadata().getLockGenerationNumber());
                                logger.trace("updated map '{}'", chubbyNodeValue.getMetadata().getLockClientMapSize());

                                String chubbyUpdatedNodeValueJsonString = ChubbyNodeValueSerializer.serialize(chubbyNodeValue);
                                logger.trace("serialized node value: '{}'", chubbyUpdatedNodeValueJsonString);

                                return kvClient.put(handleAbsolutePathByteSequence, ByteSequence.from(chubbyUpdatedNodeValueJsonString.getBytes())).thenCompose(putResponse -> {
                                    ChubbyHandleResponse chubbyHandleResponse = new ChubbyHandleResponse(Path.of(chubbyHandleRequest.getRequestedAbsolutePath()), chubbyHandleRequest.getChubbyHandleType(), lockResponse.getKey().toString(), String.valueOf(leaseGrantResponse.getID()));
                                    logger.trace("exclusive lock successfully acquired");
                                    return CompletableFuture.completedFuture(chubbyHandleResponse);
                                });
                            });
                        });
                    });
                }
            });

            /* --- shared (read) lock (it's not a real lock, it's simply access to the resource with a lease) --- */
        } else if (chubbyHandleRequest.getChubbyHandleType().equals(ChubbyHandleType.READ)) {
            KV kvClient = client.getKVClient();

            return kvClient.get(handleAbsolutePathByteSequence).thenCompose((getResponse) -> {
                Lease leaseClient = client.getLeaseClient();

                logger.trace("about to grant lease of 'lockDelay={}' seconds...", chubbyHandleRequest.getChubbyLockDelay().getValue());
                return leaseClient.grant(chubbyHandleRequest.getChubbyLockDelay().getValue()).thenCompose(leaseGrantResponse -> {
                    logger.trace("activating keep alive with 'id={}'", leaseGrantResponse.getID());
                    leaseClient.keepAlive(leaseGrantResponse.getID(), new ChubbyLockObserver<>(username, chubbyHandleRequest, client));

                    String chubbyNodeValueJsonString = getResponse.getKvs().getFirst().getValue().toString();

                    logger.trace("chubbyNodeValue to deserialize: '{}'\nnode path:'{}'", chubbyNodeValueJsonString, getResponse.getKvs().getFirst().getKey());
                    ChubbyNodeValue chubbyNodeValue = ChubbyNodeValueDeserializer.deserialize(chubbyNodeValueJsonString);

                    chubbyNodeValue.getMetadata().addClientLock(username, chubbyHandleRequest.getChubbyHandleType());

                    String chubbyUpdatedNodeValueJsonString = ChubbyNodeValueSerializer.serialize(chubbyNodeValue);

                    kvClient.put(handleAbsolutePathByteSequence, ByteSequence.from(chubbyUpdatedNodeValueJsonString.getBytes()));

                    logger.trace("acquired shared lock on {}", chubbyHandleRequest.getRequestedAbsolutePath());

                    //add subscriptions
                    this.watcherResponse = ChubbySubscribeProcessor.process(client, Path.of(chubbyHandleRequest.getRequestedAbsolutePath()), chubbyHandleRequest.getChubbyHandleType(), chubbyHandleRequest.getChubbyEventTypeList());

                    ChubbyHandleResponse chubbyHandleResponse = new ChubbyHandleResponse(Path.of(chubbyHandleRequest.getRequestedAbsolutePath()), chubbyHandleRequest.getChubbyHandleType(), null, String.valueOf(leaseGrantResponse.getID()));
                    chubbyHandleResponse.setFileContent(chubbyNodeValue.getFilecontent());

                    return CompletableFuture.completedFuture(chubbyHandleResponse);
                });
            }).exceptionally(throwable -> {
                logger.error("failed to get lock", throwable);
                throw new RuntimeException(new ChubbyLockException("failed to get shared lock"));
            });
        } else {
            logger.trace("no matching handle type detected");
            throw new ChubbyHandleException("no matching handle type detected");
        }
    }

    /**
     * Checks whether this client is permitted access to the specified node with the specified ACL permissions
     *
     * @param username            username
     * @param client              etcd client
     * @param requestedHandleType handle request from client
     * @param absolutePath        absolute path of the node to be checked
     * @return a CompletableFuture containing a boolean 'true' if the client is permitted access, 'false' otherwise
     * @throws ChubbyNodeException if the node cannot be accessed
     */
    public CompletableFuture<Boolean> isClientPermittedAccess(@NotNull String username, @NotNull Client client, @NotNull ChubbyHandleType requestedHandleType, @NotNull Path absolutePath) throws ChubbyNodeException {
        logger.trace("requested isClientPermittedAccess with 'username:{}', 'requestedHandleType:{}', 'absolutePath:{}'", username, requestedHandleType, absolutePath);
        ByteSequence nodePathKey = ByteSequence.from(absolutePath.toString().getBytes());
        KV kvClient = client.getKVClient();

        logger.trace("about to retrieve node from kv store '{}'", nodePathKey);
        return kvClient.get(nodePathKey).thenCompose(getResponse -> {
            if (getResponse.getCount() > 0) {
                logger.trace("node '{}' retrieved from kv store, value='{}'", getResponse.getKvs().getFirst().getKey(), getResponse.getKvs().getFirst().getValue());
                String aclName = ChubbyNodeValueDeserializer.deserialize(getResponse.getKvs().getFirst().getValue().toString()).getMetadata().getAclNamesMap().get(requestedHandleType);
                String aclNameAbsolutePathString = this.aclNameToAbsolutePath(aclName);
                logger.trace("aclNameAbsolutePathString:{}", aclNameAbsolutePathString);

                //if it's a default node, grant access
                if (this.isDefaultNode(Path.of(aclNameAbsolutePathString))) {
                    logger.trace("node '{}' is a default node, granting access", aclNameAbsolutePathString);
                    return CompletableFuture.completedFuture(true);
                }

                logger.trace("node '{}' is not a default node, checking if user '{}' is present into acl file", aclNameAbsolutePathString, username);
                logger.trace("about to retrieve acl file from kv store '{}'", aclNameAbsolutePathString);
                return kvClient.get(ByteSequence.from(aclNameAbsolutePathString.getBytes())).thenCompose(getResponseAclFile -> {
                    if (getResponseAclFile.getCount() > 0) {
                        logger.trace("acl file '{}' retrieved from kv store", getResponseAclFile.getKvs().getFirst().getKey());
                        List<String> fileValueList = ChubbyUtils.gsonBuild().fromJson(getResponseAclFile.getKvs().getFirst().getValue().toString(), new TypeToken<List<String>>() {
                        }.getType());
                        logger.trace("fileValueList:{}", fileValueList);
                        return CompletableFuture.completedFuture(fileValueList.contains(username));
                    } else {
                        logger.trace("acl file '{}' not present into kv store, cannot grant access", aclNameAbsolutePathString);
                        return CompletableFuture.completedFuture(false);
                    }
                });
            } else {
                logger.error("node '{}' not present into kv store, cannot grant access", absolutePath);
                throw new RuntimeException(new ChubbyNodeException("node '" + absolutePath + "' does not exist"));
            }
        });
    }

    /**
     * Initializes the namespace with default nodes' ACLs
     *
     * @param client etcd client
     * @return a CompletableFuture containing a boolean 'true' if the namespace is successfully initialized
     */
    private @NotNull CompletableFuture<Void> initializeACLs(Client client) throws ChubbyACLException {
        logger.trace("requested initializeACLs");

        //this method assumes that root node exists in kv store
        logger.trace("about to initialize root and ls ACLs");
//        return this.initializeLsNodeACLs(client).thenCompose(v -> {
        //assign to default nodes the initial acl names, they inherit them from root acl names
        logger.trace("about to assign to default nodes the initial acl names inherited from root acl names");
        CompletableFuture<Void> futureChain = CompletableFuture.completedFuture(null);
        for (Path path : this.defaultNodesToCreate) {
            futureChain = futureChain.thenCompose(vv -> {
                try {

                    return this.inheritACLNames(path, client).thenCompose(aBoolean -> null);
                } catch (ChubbyACLException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        return futureChain;
//        });
    }


    /**
     * Initializes ls node ACLs
     *
     * @param client etcd client
     * @return a CompletableFuture containing a boolean 'true' if the namespace is successfully initialized
     */
    private @NotNull CompletableFuture<Boolean> initializeLsNodeACLs(@NotNull Client client) {
        logger.trace("requested initializeLsNodeACLs");
        Map<ChubbyHandleType, String> aclNamesMap = new HashMap<>();

        aclNamesMap.put(ChubbyHandleType.READ, "read");
        aclNamesMap.put(ChubbyHandleType.WRITE, "write");
        aclNamesMap.put(ChubbyHandleType.CHANGE_ACL, "change_acl");
        logger.trace("default aclNamesMap:{}", aclNamesMap);

        ByteSequence rootKey = ByteSequence.from(this.getRoot().toString().getBytes());

        return client.getKVClient().get(rootKey).thenCompose(getResponse -> {
            ChubbyNodeValue chubbyNodeValue = ChubbyNodeValueDeserializer.deserialize(getResponse.getKvs().getFirst().getValue().toString());
            logger.trace("root node value deserialized '{}'", chubbyNodeValue.toString());

            chubbyNodeValue.getMetadata().setAclNamesMap(aclNamesMap);
            logger.trace("root node value updated '{}'", chubbyNodeValue.toString());

            return client.getKVClient().put(rootKey, ByteSequence.from(ChubbyNodeValueSerializer.serialize(chubbyNodeValue).getBytes())).thenCompose(putResponse -> {
                ByteSequence lsKey = ByteSequence.from(this.getRoot().resolve("/ls").toString().getBytes());

                return client.getKVClient().get(lsKey).thenCompose(lsResponse -> {
                    ChubbyNodeValue lsNodeValue = ChubbyNodeValueDeserializer.deserialize(lsResponse.getKvs().getFirst().getValue().toString());
                    logger.trace("ls node value deserialized '{}'", lsNodeValue.toString());

                    lsNodeValue.getMetadata().setAclNamesMap(aclNamesMap);
                    logger.trace("ls node value updated '{}'", lsNodeValue.toString());

                    return client.getKVClient().put(lsKey, ByteSequence.from(ChubbyNodeValueSerializer.serialize(lsNodeValue).getBytes())).thenCompose(putResponse2 -> CompletableFuture.completedFuture(true));
                });
            });
        });
    }

    /**
     * Initializes ls node ACLs
     *
     * @param client etcd client
     * @return a CompletableFuture containing a boolean 'true' if the namespace is successfully initialized
     */
    private @NotNull CompletableFuture<Boolean> initializeRootNodeACLs(@NotNull Client client) {
        logger.trace("requested initializeRootNodeACLs");
        Map<ChubbyHandleType, String> aclNamesMap = new HashMap<>();

        aclNamesMap.put(ChubbyHandleType.READ, "read");
        aclNamesMap.put(ChubbyHandleType.WRITE, "write");
        aclNamesMap.put(ChubbyHandleType.CHANGE_ACL, "change_acl");
        logger.trace("default aclNamesMap:{}", aclNamesMap);

        ByteSequence rootKey = ByteSequence.from(this.getRoot().toString().getBytes());

        return client.getKVClient().get(rootKey).thenCompose(getResponse -> {
            ChubbyNodeValue chubbyNodeValue = ChubbyNodeValueDeserializer.deserialize(getResponse.getKvs().getFirst().getValue().toString());
            logger.trace("root node value deserialized '{}'", chubbyNodeValue.toString());

            chubbyNodeValue.getMetadata().setAclNamesMap(aclNamesMap);
            logger.trace("root node value updated '{}'", chubbyNodeValue.toString());

            return client.getKVClient().put(rootKey, ByteSequence.from(ChubbyNodeValueSerializer.serialize(chubbyNodeValue).getBytes())).thenCompose(putResponse -> CompletableFuture.completedFuture(true));
        });
    }

    /**
     * Inherit ACL names from parent nodes to child nodes
     *
     * @param absolutePath absolute path of the node to inherit ACL names
     * @param client       etcd client
     * @return a CompletableFuture containing a boolean 'true' if the ACL names are successfully inherited
     * @throws ChubbyACLException if the ACL names cannot be inherited
     */
    public CompletableFuture<Boolean> inheritACLNames(@NotNull Path absolutePath, @NotNull Client client) throws ChubbyACLException {
        logger.trace("requested inherit ACL names with 'path:{}'", absolutePath);
        KV kvClient = client.getKVClient();

        Map<ChubbyHandleType, String> parentAclNamesMap = new HashMap<>();
        Path currentPath = absolutePath.getRoot();

        CompletableFuture<Void> futureChain = CompletableFuture.completedFuture(null);

        logger.trace("starting for loop with current path '{}'", currentPath);
        for (Path path : absolutePath) {
            currentPath = currentPath.resolve(path);
            Path finalCurrentPath = currentPath;

            logger.trace("starting iteration on current path '{}'", finalCurrentPath);
            logger.trace("about to retrieve node from kv store '{}'", finalCurrentPath);
            futureChain = futureChain.thenCompose(previousResult -> kvClient.get(ByteSequence.from(finalCurrentPath.toString().getBytes())).thenCompose(getResponse -> {
                if (getResponse.getCount() > 0) {
                    logger.trace("node '{}' retrieved from kv store", finalCurrentPath);

                    Map<ChubbyHandleType, String> currMap = ChubbyNodeValueDeserializer.deserialize(getResponse.getKvs().getFirst().getValue().toString()).getMetadata().getAclNamesMap();
                    logger.trace("node '{}' acl names map '{}'", finalCurrentPath, currMap);

                    for (ChubbyHandleType key : currMap.keySet()) {
                        parentAclNamesMap.put(key, currMap.get(key));
                    }

                    logger.trace("parentAclNamesMap updated '{}'", parentAclNamesMap);
                    String nextKey = finalCurrentPath.resolveSibling(path.getFileName().toString()).toString();

                    logger.trace("about to retrieve next node from kv store '{}'", nextKey);
                    return kvClient.get(ByteSequence.from(nextKey.getBytes())).thenCompose(nextResponse -> {
                        if (nextResponse.getCount() > 0) {
                            logger.trace("next node '{}' retrieved from kv store", nextKey);
                            ChubbyNodeValue nextNodeValue = ChubbyNodeValueDeserializer.deserialize(nextResponse.getKvs().getFirst().getValue().toString());

                            logger.trace("next node '{}' acl names map '{}'", nextKey, nextNodeValue.getMetadata().getAclNamesMap());
                            nextNodeValue.getMetadata().setAclNamesMap(parentAclNamesMap);
                            logger.trace("next node '{}' acl names map updated '{}'", nextKey, nextNodeValue.getMetadata().getAclNamesMap());

                            logger.trace("about to put updated next node '{}' back into kv store", nextKey);
                            return kvClient.put(ByteSequence.from(nextKey.getBytes()), ByteSequence.from(ChubbyNodeValueSerializer.serialize(nextNodeValue).getBytes())).thenCompose(putResponse -> CompletableFuture.completedFuture(null));
                        } else {

                            throw new RuntimeException(new ChubbyACLException("node '" + nextKey + "' does not exist in the kv store"));
                        }
                    });
                } else {

                    throw new RuntimeException(new ChubbyACLException("node '" + finalCurrentPath + "' does not exist in the kv store"));
                }
            }));
        }

        return futureChain.thenCompose(v -> CompletableFuture.completedFuture(true));
    }

    /**
     * Changes the ACL names of the specified node
     *
     * @param absolutePath    absolute path of the node to change ACL names
     * @param client          etcd client
     * @param username        username
     * @param heldHandleType  handle type held by the client
     * @param ACLTypeToChange ACL type to change
     * @param newACLName      new ACL name
     * @return a CompletableFuture containing a byte sequence with the result of the operation
     * @throws ChubbyHandleException if the handle type is not recognized
     * @throws ChubbyNodeException   if the node cannot be accessed
     */
    public CompletableFuture<ByteSequence> changeACLNames(@NotNull Path absolutePath, @NotNull Client client, String username, @NotNull ChubbyHandleType heldHandleType, @NotNull ChubbyHandleType ACLTypeToChange, String newACLName) throws ChubbyHandleException, ChubbyNodeException {
        logger.trace("requested change ACL names with 'path:{}', 'ACLTypeToChange:{}', 'newACLName:{}'", absolutePath, ACLTypeToChange, newACLName);
        KV kvClient = client.getKVClient();

        if (!heldHandleType.equals(ChubbyHandleType.CHANGE_ACL)) {
            throw new ChubbyHandleException("it's not possible to change acl names of current node with '" + heldHandleType + "' handle type, acquire '" + ChubbyHandleType.CHANGE_ACL + "' handle type before proceeding");
        }

        ByteSequence nodePathKey = ByteSequence.from(absolutePath.toString().getBytes());
        String newACLNameAbsolutePath = this.aclNameToAbsolutePath(newACLName);

        logger.trace("about to check if specified new ACL name is already assigned to another node's ACL name '{}'", newACLNameAbsolutePath);
        return this.createACLNodeFileIfAbsent(newACLNameAbsolutePath, client, username).thenCompose(created -> {
            if (!created) {
                throw new RuntimeException(new ChubbyACLException("specified new ACL name '" + newACLNameAbsolutePath + "' is already assigned to another node, consider using another name"));
            } else {
                logger.trace("about to retrieve kv pair from store with 'key:{}'", nodePathKey);
                return kvClient.get(nodePathKey).thenCompose(getResponse -> {
                    //if node exists (it should always exist, since in order to call this method or remove this node a user needs a write (exclusive) lock)
                    if (getResponse.getCount() > 0) {
                        ChubbyNodeValue chubbyNodeValue = ChubbyNodeValueDeserializer.deserialize(getResponse.getKvs().getFirst().getValue().toString());
                        logger.trace("old acl names map: '{}'", chubbyNodeValue.getMetadata().getAclNamesMap());

                        //remove old acl permission file
                        String oldACLNodeFileName = chubbyNodeValue.getMetadata().getAclNamesMap().get(ACLTypeToChange);
                        String oldACLNodeAbsolutePath = this.aclNameToAbsolutePath(oldACLNodeFileName);

                        logger.trace("about to remove ACL node file if present: '{}'", oldACLNodeAbsolutePath);
                        return this.removeACLNodeFileIfPresent(oldACLNodeAbsolutePath, client).thenCompose(removed -> {
                            if (removed) {
                                //replaces old name with new one
                                chubbyNodeValue.getMetadata().getAclNamesMap().put(ACLTypeToChange, newACLName);
                                logger.trace("new acl names map: '{}'", chubbyNodeValue.getMetadata().getAclNamesMap());

                                //update acl generation number
                                chubbyNodeValue.getMetadata().increaseAclGenerationNumberOnce();

                                logger.trace("about to put node: '{}'", absolutePath.toString());
                                return kvClient.put(nodePathKey, ByteSequence.from(ChubbyNodeValueSerializer.serialize(chubbyNodeValue).getBytes())).thenCompose(putResponse -> CompletableFuture.completedFuture(ByteSequence.from("node content updated successfully".getBytes())));
                            } else {
                                logger.trace("about to remove ACL node file if present: '{}'", newACLNameAbsolutePath);
                                //whether the new file is present, remove it, then throw an exception.
                                this.removeACLNodeFileIfPresent(newACLNameAbsolutePath, client).thenCompose(removed1 -> {
                                    throw new RuntimeException(new ChubbyACLException("old ACL name '" + oldACLNodeAbsolutePath + "' is not present into kv store"));
                                });
                            }
                            return CompletableFuture.completedFuture(null);
                        });
                    } else {
                        throw new RuntimeException(new ChubbyNodeException("specified node '" + absolutePath + "' does not exist"));
                    }
                });
            }
        });
    }

    /**
     * Creates an ACL node file if it's absent
     *
     * @param newACLNameAbsolutePathString absolute path of the new ACL name
     * @param client                       etcd client
     * @param username                     username
     * @return a CompletableFuture containing a boolean 'true' if the ACL node file was absent and this method
     * successfully created it
     */
    private CompletableFuture<Boolean> createACLNodeFileIfAbsent(@NotNull String newACLNameAbsolutePathString, @NotNull Client client, @NotNull String username) {
        KV kvClient = client.getKVClient();
        ByteSequence newACLNameAbsolutePathByteSequence = ByteSequence.from(newACLNameAbsolutePathString.getBytes());

        return kvClient.get(newACLNameAbsolutePathByteSequence).thenCompose(getResponse -> {
            if (getResponse.getCount() > 0) {
                return CompletableFuture.completedFuture(false);
            } else {
                List<String> usernamesList = new ArrayList<>();
                usernamesList.add(username);

                logger.trace("about to put new ACL node: k='{}', v='{}'", newACLNameAbsolutePathByteSequence, ByteSequence.from(ChubbyUtils.gsonBuild().toJson(usernamesList).getBytes()));
                return kvClient.put(newACLNameAbsolutePathByteSequence, ByteSequence.from(ChubbyUtils.gsonBuild().toJson(usernamesList).getBytes())).thenCompose(putResponse -> CompletableFuture.completedFuture(true));
            }
        });
    }

    /**
     * Removes an ACL node file if it's present
     *
     * @param aclNameAbsolutePathString absolute path of the ACL name
     * @param client                    etcd client
     * @return a CompletableFuture containing a boolean 'true' if the ACL node file was present and this method
     * successfully removed it, 'false' otherwise
     */
    private CompletableFuture<Boolean> removeACLNodeFileIfPresent(@NotNull String aclNameAbsolutePathString, @NotNull Client client) {
        logger.trace("requested remove ACL node file '{}'", aclNameAbsolutePathString);

        //It's not possible to remove a default node, so the method is skipped but acts like it was removed
        if (this.isDefaultNode(Path.of(aclNameAbsolutePathString))) {
            logger.trace("argument path '{}' is a default node, skipping operation returning true", aclNameAbsolutePathString);
            return CompletableFuture.completedFuture(true);
        }

        KV kvClient = client.getKVClient();
        ByteSequence aclNameAbsolutePathByteSequence = ByteSequence.from(aclNameAbsolutePathString.getBytes());

        logger.trace("about to retrieve node: '{}'", aclNameAbsolutePathString);
        return kvClient.get(aclNameAbsolutePathByteSequence).thenCompose(getResponse -> {
            if (getResponse.getCount() > 0) {
                logger.trace("about to delete node: '{}'", aclNameAbsolutePathString);
                return kvClient.delete(aclNameAbsolutePathByteSequence).thenCompose(putResponse -> CompletableFuture.completedFuture(true));
            } else {
                logger.trace("about to return completed future 'false'");
                return CompletableFuture.completedFuture(false);
            }
        });
    }

    /**
     * Adds a client to the specified ACL node file
     *
     * @param absolutePath                absolute path of the node to add the client
     * @param client                      etcd client
     * @param chubbyHandleTypeToAddClient handle type to add the client
     * @param heldChubbyHandleType        handle type held by the client
     * @param usernames                   usernames to add
     * @return a CompletableFuture containing a byte sequence with the result of the operation
     * @throws ChubbyHandleException    if the handle type is not recognized
     * @throws ChubbyNodeException      if the node cannot be accessed
     * @throws ChubbyACLException       if the client cannot be added to the ACL node file
     * @throws IllegalArgumentException if the usernames array is empty
     */
    public CompletableFuture<ByteSequence> addACLClient(@NotNull Path absolutePath, Client client, @NotNull ChubbyHandleType chubbyHandleTypeToAddClient, @NotNull ChubbyHandleType heldChubbyHandleType, @NotNull String... usernames) throws ChubbyHandleException, ChubbyNodeException, ChubbyACLException, IllegalArgumentException {
        logger.trace("requested addACLClient with 'path:{}', 'chubbyHandleTypeToAddClient:{}', 'heldChubbyHandleType:{}', 'usernames:{}'", absolutePath, chubbyHandleTypeToAddClient, heldChubbyHandleType, usernames);

        if (!heldChubbyHandleType.equals(ChubbyHandleType.CHANGE_ACL)) {
            throw new ChubbyHandleException("it's not possible to change acl names of current node with '" + heldChubbyHandleType + "' handle type, acquire '" + ChubbyHandleType.CHANGE_ACL + "' handle type before proceeding");
        }

        ByteSequence nodePathKey = ByteSequence.from(absolutePath.toString().getBytes());
        KV kvClient = client.getKVClient();

        logger.trace("about to retrieve key '{}'", absolutePath.toString());
        return kvClient.get(nodePathKey).thenCompose(getResponse -> {
            if (getResponse.getCount() > 0) {
                String aclName = ChubbyNodeValueDeserializer.deserialize(getResponse.getKvs().getFirst().getValue().toString()).getMetadata().getAclNamesMap().get(chubbyHandleTypeToAddClient);
                logger.trace("extracted aclName '{}'", aclName);
                String aclNameAbsolutePathString = this.aclNameToAbsolutePath(aclName);
                logger.trace("extracted aclNameAbsolutePathString '{}'", aclNameAbsolutePathString);

                if (this.isDefaultNode(Path.of(aclNameAbsolutePathString))) {
                    logger.error("extracted node is default node '{}', throwing exception", aclNameAbsolutePathString);
                    throw new RuntimeException(new ChubbyACLException("cannot add client to default acl node name"));
                }

                try {
                    logger.trace("about to 'addACLClientToFile'");
                    return this.addACLClientToFile(aclNameAbsolutePathString, client, usernames).thenCompose(added -> CompletableFuture.completedFuture(ByteSequence.from("client added successfully".getBytes())));
                } catch (ChubbyNodeException e) {
                    throw new RuntimeException(e);
                }
            } else {
                throw new RuntimeException(new ChubbyNodeException("node '" + absolutePath + "' does not exist"));
            }
        });
    }

    /**
     * Adds a client to the specified ACL node file
     *
     * @param existingACLNameFileAbsolutePathString absolute path of the ACL name
     * @param client                                etcd client
     * @param usernames                             usernames to add
     * @return a CompletableFuture containing a boolean 'true' if the client was successfully added to the ACL node file
     * @throws IllegalArgumentException if the usernames array is empty
     * @throws ChubbyNodeException      if the ACL node file cannot be accessed
     */
    private CompletableFuture<Boolean> addACLClientToFile(@NotNull String existingACLNameFileAbsolutePathString, @NotNull Client client, @NotNull String @NotNull ... usernames) throws IllegalArgumentException, ChubbyNodeException {
        logger.trace("requested addACLClientToFile with 'path:{}', 'usernames:{}'", existingACLNameFileAbsolutePathString, usernames);

        if (usernames.length == 0) {
            throw new IllegalArgumentException("need at least 1 username");
        }

        KV kvClient = client.getKVClient();
        ByteSequence existingACLNameAbsolutePathByteSequence = ByteSequence.from(existingACLNameFileAbsolutePathString.getBytes());

        logger.trace("about to retrieve acl name file '{}' from kv store", existingACLNameAbsolutePathByteSequence);
        return kvClient.get(existingACLNameAbsolutePathByteSequence).thenCompose(getResponse -> {
            if (getResponse.getCount() > 0) {
                logger.trace("about to unmarshal acl names of given acl file '{}'", getResponse.getKvs().getFirst().getKey().toString());
                Type listStringType = new TypeToken<List<String>>() {
                }.getType();
                List<String> fileValueList = ChubbyUtils.gsonBuild().fromJson(getResponse.getKvs().getFirst().getValue().toString(), listStringType);

                logger.trace("unmarshalled fileValueList: '{}'", fileValueList);
                Arrays.stream(usernames).forEach(e -> {
                    if (!fileValueList.contains(e)) {
                        fileValueList.add(e);
                    }
                });
                logger.trace("updated fileValueList: '{}'", fileValueList);

                String fileValueListString = ChubbyUtils.gsonBuild().toJson(fileValueList);
                logger.trace("marshalled fileValueList: '{}'", fileValueListString);

                logger.trace("about to put updated kv pair: k='{}', v='{}'", existingACLNameAbsolutePathByteSequence, fileValueListString);
                return kvClient.put(existingACLNameAbsolutePathByteSequence, ByteSequence.from(fileValueListString.getBytes())).thenCompose(putResponse -> CompletableFuture.completedFuture(true));
            } else {
                throw new RuntimeException(new ChubbyNodeException("node '" + existingACLNameAbsolutePathByteSequence + "' not found"));
            }
        });
    }

    /**
     * returns the absolute path of the ACL name file
     *
     * @param aclName ACL name file
     * @return the absolute path of the ACL name file
     */
    private @NotNull String aclNameToAbsolutePath(@NotNull String aclName) {
        return this.aclNodeAbsolutePath.resolve(aclName + ".txt").toString();
    }

    /**
     * Creates default nodes and initializes ACLs
     *
     * @param client   etcd client
     * @return a CompletableFuture containing a ChubbyHandleResponse with the handle of the root node
     * @throws ExecutionException   if the computation threw an exception
     * @throws InterruptedException if the current thread was interrupted while waiting
     */
    public CompletableFuture<Void> createDefaultNodes(Client client) throws ExecutionException, InterruptedException {
        logger.trace("requested default nodes creation");

        //create default directories without any event subscription
        for (Path path : this.defaultNodesToCreate) {
            logger.trace("about to create default node '{}'", path.toString());

            try {
                this.createNode(client, path, ChubbyNodeAttribute.PERMANENT, true).get();

            } catch (InterruptedException | ExecutionException | ChubbyLockException | ChubbyNodeException e) {
                throw new RuntimeException(e);
            }
        }
        logger.trace("default nodes created");

        logger.trace("about to initialize ACLs");
        this.initializeRootNodeACLs(client).get();

        logger.trace("RootACLs initialized, about to initialize ls node");
        this.initializeLsNodeACLs(client).get();

        try {
            this.initializeACLs(client);

        } catch (ChubbyACLException e) {
            throw new RuntimeException(e);
        }

        return CompletableFuture.completedFuture(null);
    }

    public CompletableFuture<ChubbyHandleResponse> createDefaultHandle(String username, Client client) throws ExecutionException, InterruptedException {
        logger.trace("requested default handle creation");

        try {
            return this.createHandle(username, client, new ChubbyHandleRequest(this.getRoot(), ChubbyHandleType.READ, new ChubbyLockDelay(MAX_LOCKDELAY_SECONDS)));
        } catch (ChubbyLockException | ChubbyHandleException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Tries to remove the specified node if it's ephemeral
     *
     * @param chubbyRequest request
     * @param client        etcd client
     * @return a CompletableFuture containing a boolean 'true' if the node was successfully removed
     * @throws ChubbyNodeException                 if the node cannot be accessed
     * @throws ChubbyCannotRemoveHeldNodeException if the node cannot be removed
     * @throws ChubbyLockException                 if the lock cannot be acquired
     * @throws ChubbyObserverException             if the observer for the event subscriptions cannot be activated
     * @throws ChubbyHandleException               if the handle type is not recognized
     */
    public CompletableFuture<Boolean> tryRemoveIfEphemeral(@NotNull ChubbyRequest chubbyRequest, Client client) throws ChubbyNodeException, ChubbyCannotRemoveHeldNodeException, ChubbyLockException, ChubbyObserverException, ChubbyHandleException {
        return this.tryRemoveIfEphemeral(chubbyRequest.getUsername(), client, Path.of(chubbyRequest.getHandleAbsolutePath()), chubbyRequest.getChubbyCurrentHandleType(), chubbyRequest.getLockId(), chubbyRequest.getLeaseId());
    }

    /**
     * Tries to remove the specified node if it's ephemeral
     *
     * @param username         username
     * @param client           etcd client
     * @param absolutePath     absolute path of the node to be removed
     * @param chubbyHandleType handle type of the client to be removed
     * @param lockID           lock ID
     * @param leaseId          lease ID
     * @return a CompletableFuture containing a boolean 'true' if the node was successfully removed
     * @throws ChubbyNodeException                 if the node cannot be accessed
     * @throws ChubbyCannotRemoveHeldNodeException if the node cannot be removed
     * @throws ChubbyLockException                 if the lock cannot be acquired
     * @throws ChubbyObserverException             if the observer for the event subscriptions cannot be activated
     * @throws ChubbyHandleException               if the handle type is not recognized
     */
    protected CompletableFuture<Boolean> tryRemoveIfEphemeral(@Nullable String username, @NotNull Client client, @NotNull Path absolutePath, ChubbyHandleType chubbyHandleType, String lockID, String leaseId) throws ChubbyNodeException, ChubbyCannotRemoveHeldNodeException, ChubbyLockException, ChubbyObserverException, ChubbyHandleException {
        logger.trace("requested tryRemoveIfEphemeral");

        return client.getKVClient().get(ByteSequence.from(absolutePath.toString().getBytes())).thenCompose(getResponse -> {
            logger.trace(getResponse);

            //if it exists
            if (getResponse.getCount() > 0) {
                logger.trace("exists");

                ChubbyNodeValue chubbyNodeValue = ChubbyNodeValueDeserializer.deserialize(getResponse.getKvs().getFirst().getValue().toString());

                //if it's ephemeral try to remove it
                if (chubbyNodeValue.getMetadata().getChubbyNodeAttribute().equals(ChubbyNodeAttribute.EPHEMERAL)) {
                    logger.trace("is ephemeral");

                    try {
                        boolean ignoreHandleType = true;
                        return this.removeNode(username, client, absolutePath, chubbyHandleType, lockID, leaseId, ignoreHandleType).thenCompose(r -> {

                            //check if any ephemeral parent node exist and try to remove them recursively
                            try {
                                return this.tryRemoveIfEphemeral(username, client, absolutePath, chubbyHandleType, lockID, leaseId).thenCompose(ret -> CompletableFuture.completedFuture(true));
                            } catch (ChubbyNodeException | ChubbyHandleException | ChubbyObserverException |
                                     ChubbyLockException | ChubbyCannotRemoveHeldNodeException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    } catch (ChubbyNodeException | ChubbyHandleException | ChubbyObserverException |
                             ChubbyLockException | ChubbyCannotRemoveHeldNodeException e) {
                        throw new RuntimeException(e);
                    }

                } else {
                    logger.trace("not ephemeral, returning false future");

                    //if not ephemeral, return false
                    return CompletableFuture.completedFuture(false);
                }

            } else {
                logger.trace("does not exist, returning false future");

                //if it does not exist, return false
                return CompletableFuture.completedFuture(false);
            }
        });
    }

    /**
     * Removes the specified node
     *
     * @param chubbyRequest request
     * @param client        etcd client
     * @return a CompletableFuture containing a boolean 'true' if the node was successfully removed
     * @throws ChubbyNodeException                 if the node cannot be accessed
     * @throws ChubbyCannotRemoveHeldNodeException if the node cannot be removed
     * @throws ChubbyLockException                 if the lock cannot be acquired
     * @throws ChubbyObserverException             if the observer for the event subscriptions cannot be activated
     * @throws ChubbyHandleException               if the handle type is not recognized
     */
    public CompletableFuture<Void> removeNode(@NotNull ChubbyRequest chubbyRequest, Client client) throws ChubbyNodeException, ChubbyCannotRemoveHeldNodeException, ChubbyLockException, ChubbyObserverException, ChubbyHandleException {
        return this.removeNode(chubbyRequest.getUsername(), client, Path.of(chubbyRequest.getHandleAbsolutePath()), chubbyRequest.getChubbyCurrentHandleType(), chubbyRequest.getLockId(), chubbyRequest.getLeaseId(), false);
    }

    /**
     * Removes the specified node
     *
     * @param username         username
     * @param client           etcd client
     * @param absolutePath     absolute path of the node to be removed
     * @param chubbyHandleType handle type of the client containing the lock to be removed
     * @param lockID           lock ID
     * @param leaseId          lease ID
     * @return a CompletableFuture containing a boolean 'true' if the node was successfully removed
     * @throws ChubbyNodeException                 if the node cannot be accessed
     * @throws ChubbyCannotRemoveHeldNodeException if the node cannot be removed
     * @throws ChubbyLockException                 if the lock cannot be acquired
     * @throws ChubbyObserverException             if the observer for the event subscriptions cannot be activated
     * @throws ChubbyHandleException               if the handle type is not recognized
     */
    protected CompletableFuture<Void> removeNode(String username, @NotNull Client client, @NotNull Path absolutePath, ChubbyHandleType chubbyHandleType, String lockID, String leaseId, boolean ignoreHandleType) throws ChubbyNodeException, ChubbyCannotRemoveHeldNodeException, ChubbyLockException, ChubbyObserverException, ChubbyHandleException {
        logger.trace("requested remove node on 'path:{}'", absolutePath);

        if (!chubbyHandleType.equals(ChubbyHandleType.WRITE) && !ignoreHandleType) {
            logger.error("cannot remove node with handle type '{}'", chubbyHandleType);
            throw new ChubbyHandleException("cannot remove node with handle type " + chubbyHandleType);
        }

        if (this.isDefaultNode(absolutePath)) {
            logger.error("cannot remove default node {}", absolutePath);
            throw new ChubbyNodeException("cannot remove default node " + absolutePath);
        }

        logger.trace("about to check if specified node is stored inside kv store");
        return client.getKVClient().get(ByteSequence.from(absolutePath.toString().getBytes())).thenCompose(getResponse -> {
            logger.trace("kv client getResponse: '{}'", getResponse);

            String currentNodeValueJsonString = getResponse.getKvs().getFirst().getValue().toString();
            String currentNodePathString = getResponse.getKvs().getFirst().getKey().toString();
            logger.trace("currentNodeValueJsonString: '{}'", currentNodeValueJsonString);
            logger.trace("currentNodePathString: '{}'", currentNodePathString);

            ChubbyNodeValue chubbyNodeValue = ChubbyNodeValueDeserializer.deserialize(currentNodeValueJsonString);
            logger.trace("deserialized chubby node value: '{}'", chubbyNodeValue);

            //if the node is held by only this client, and it has no child nodes, unlock it and then delete it
            logger.trace("about to check if specified node can be removed: '{}', '{}'", chubbyNodeValue.getMetadata().getLockClientMapSize() == 1, chubbyNodeValue.getMetadata().getChildNodeNumber() == 0);
            if (chubbyNodeValue.getMetadata().getLockClientMapSize() == 1 && chubbyNodeValue.getMetadata().getChildNodeNumber() == 0) {
                logger.trace("can be deleted: '{}', '{}'", chubbyNodeValue.getMetadata().getLockClientMapSize() == 1, chubbyNodeValue.getMetadata().getChildNodeNumber() == 0);

                //unlock
                logger.trace("about to unlock node");
                try {
                    return this.unlock(username, client, absolutePath, chubbyHandleType, lockID, leaseId, false).thenCompose(response -> {

                        //delete
                        logger.trace("about to delete node from kv store");
                        return client.getKVClient().delete(ByteSequence.from(currentNodePathString.getBytes())).thenCompose(deleteResponse -> {

                            //update parent's node child number
                            String parentPathString = Path.of(currentNodePathString).getParent().toString();
                            logger.trace("about to update parent's node '{}' child number", parentPathString);
                            return client.getKVClient().get(ByteSequence.from(parentPathString.getBytes())).thenCompose(getResponse1 -> {

                                //if exists
                                if (getResponse1.getCount() > 0) {
                                    logger.trace("parent node's kv pair exists in the kv store");
                                    //looks for children node, update its value
                                    return this.getLs(client, Path.of(parentPathString), 1).thenCompose(list -> {

                                        List<String> listWithOnlyNodes = list.stream().filter(e -> !e.startsWith("/")).toList();

                                        logger.trace("setting child node of '{}' to '{}'", parentPathString, listWithOnlyNodes.size());

                                        ChubbyNodeValue parentChubbyNodeValue = ChubbyNodeValueDeserializer.deserialize(getResponse1.getKvs().getFirst().getValue().toString());
                                        logger.trace("parent chubby node value metadata before the update '{}'", parentChubbyNodeValue.getMetadata());

                                        parentChubbyNodeValue.getMetadata().setChildNodeNumber(listWithOnlyNodes.size());
                                        logger.trace("parent chubby node value metadata after child number update '{}'", parentChubbyNodeValue.getMetadata());

                                        parentChubbyNodeValue.getMetadata().removeClientLock(username, chubbyHandleType);
                                        logger.trace("parent chubby node value metadata after client lock map update '{}'", parentChubbyNodeValue.getMetadata());

                                        ByteSequence parentChubbyNodeValueByteSequence = ByteSequence.from(ChubbyNodeValueSerializer.serialize(parentChubbyNodeValue).getBytes());
                                        logger.trace("about to put updated value of the node to kv store");
                                        return client.getKVClient().put(ByteSequence.from(parentPathString.getBytes()), parentChubbyNodeValueByteSequence).thenCompose(putResponse -> {
                                            logger.trace("parent node's kv pair updated in the kv store");
                                            return CompletableFuture.completedFuture(null);
                                        });
                                    });
                                } else {
                                    logger.trace("parent node's kv pair does not exist in the kv store, returning 'CompletableFuture.completedFuture(null)'...");
                                    return CompletableFuture.completedFuture(null);
                                }
                            });
                        });
                    });
                } catch (ChubbyNodeException | ChubbyHandleException | ChubbyObserverException |
                         ChubbyLockException e) {
                    throw new RuntimeException(e);
                }

            } else {
                if (chubbyNodeValue.getMetadata().getLockClientMapSize() == 1) {
                    logger.error("specified node cannot be deleted because it has at least 1 child node");
                    throw new RuntimeException(new ChubbyNodeException("specified node cannot be deleted because it has at least 1 child node"));
                } else {
                    logger.error("specified node is currently held by another client");
                    throw new RuntimeException(new ChubbyCannotRemoveHeldNodeException("specified node is currently held by another client"));
                }
            }
        });
    }

    /**
     * Unlocks the lock from the specified node
     *
     * @param chubbyRequest request
     * @param client        etcd client
     * @param canUnlockRoot if the root node can be unlocked
     * @return a CompletableFuture containing a byte sequence with the result of the operation
     * @throws ChubbyNodeException     if the node cannot be accessed
     * @throws ChubbyLockException     if the lock cannot be acquired
     * @throws ChubbyObserverException if the observer for the event subscriptions cannot be activated
     * @throws ChubbyHandleException   if the handle type is not recognized
     */
    public CompletableFuture<ByteSequence> unlock(@NotNull ChubbyRequest chubbyRequest, Client client, boolean canUnlockRoot) throws ChubbyNodeException, ChubbyLockException, ChubbyObserverException, ChubbyHandleException {
        return this.unlock(chubbyRequest.getUsername(), client, Path.of(chubbyRequest.getHandleAbsolutePath()), chubbyRequest.getChubbyCurrentHandleType(), chubbyRequest.getLockId(), chubbyRequest.getLeaseId(), canUnlockRoot);
    }

    /**
     * Unlocks the lock from the specified node
     *
     * @param username           username
     * @param client             etcd client
     * @param handleAbsolutePath absolute path of the node to unlock
     * @param chubbyHandleType   handle type of the client to unlock
     * @param lockID             lock ID
     * @param leaseId            lease ID
     * @param canUnlockRoot      if the root node can be unlocked
     * @return a CompletableFuture containing a byte sequence with the result of the operation
     * @throws ChubbyNodeException     if the node cannot be accessed
     * @throws ChubbyLockException     if the lock cannot be acquired
     * @throws ChubbyObserverException if the observer for the event subscriptions cannot be activated
     * @throws ChubbyHandleException   if the handle type is not recognized
     */
    protected CompletableFuture<ByteSequence> unlock(String username, @NotNull Client client, @NotNull Path handleAbsolutePath, ChubbyHandleType chubbyHandleType, @NotNull String lockID, @NotNull String leaseId, boolean canUnlockRoot) throws ChubbyNodeException, ChubbyLockException, ChubbyObserverException, ChubbyHandleException {
        logger.trace("requested 'unlock' operation on 'path:{}' with handle type: '{}'", handleAbsolutePath, chubbyHandleType);

        CompletableFuture<ByteSequence> result = new CompletableFuture<>();

        if (chubbyHandleType.equals(ChubbyHandleType.WRITE) || chubbyHandleType.equals(ChubbyHandleType.CHANGE_ACL)) {
            // create a byte sequence for the lock key
            ByteSequence lockKeyByteSequence = ByteSequence.from((lockID).getBytes(charset));
            logger.trace("looking for exclusive lock on 'path:{}' into kv store...", lockKeyByteSequence.toString());

            if (lockKeyByteSequence.isEmpty()) {
                throw new ChubbyNodeException("provided key is empty");
            }

            // check if a lock exists on the resource
            CompletableFuture<GetResponse> getFuture = client.getKVClient().get(lockKeyByteSequence);
            getFuture.whenComplete((getResponse, throwable) -> {
                if (throwable != null) {
                    logger.error("failed to retrieve key", throwable);
                    throw new RuntimeException(new ChubbyNodeException("failed to retrieve key"));
                }

                // if a lock exists (i.e., the count of keys is greater than 0), delete the lock key
                else if (getResponse.getCount() > 0) {
                    logger.trace("found lock 'path:{}'", lockKeyByteSequence.toString());

                    CompletableFuture<DeleteResponse> deleteFuture = client.getKVClient().delete(lockKeyByteSequence);
                    deleteFuture.whenComplete((deleteResponse, throwable1) -> {
                        if (throwable1 != null) {
                            logger.error("Failed to delete lock key", throwable1);
                            throw new RuntimeException(new ChubbyNodeException("failed to delete key"));
                        }

                        // if the key was deleted successfully, remove the lease and remove the client from node's value
                        else {
                            client.getLeaseClient().revoke(Long.parseLong(leaseId)).thenCompose(leaseRevokeResponse -> client.getKVClient().get(ByteSequence.from(handleAbsolutePath.toString().getBytes())).thenCompose(getResponse1 -> {
                                //check if node exists
                                if (getResponse1.getCount() > 0) {
                                    ChubbyNodeValue chubbyNodeValue = ChubbyNodeValueDeserializer.deserialize(getResponse1.getKvs().getFirst().getValue().toString());

                                    chubbyNodeValue.getMetadata().removeClientLock(username, chubbyHandleType);

                                    ByteSequence chubbyNodeValueByteSequence = ByteSequence.from(ChubbyNodeValueSerializer.serialize(chubbyNodeValue).getBytes());

                                    return client.getKVClient().put(getResponse1.getKvs().getFirst().getKey(), chubbyNodeValueByteSequence).thenCompose(putResponse -> {
                                        logger.trace("released lock on {}", handleAbsolutePath);
                                        return CompletableFuture.completedFuture(result.complete(ByteSequence.from(("lock released").getBytes(charset))));
                                    });
                                } else {
                                    throw new RuntimeException(new ChubbyNodeException("node not found"));
                                }
                            }));
                        }
                    });
                }

                // if no lock exists, log a trace message and complete the result
                else {
                    logger.trace("No exclusive lock exists on {}, nothing to release (getResponse value:{})", handleAbsolutePath, getResponse);
                    throw new RuntimeException(new ChubbyLockException("no exclusive lock is currently held on given path, nothing to release"));
                }
            });

            //since the read handle is not a real lock but a lease on the resource, a simple revoke is sufficient
        } else if (chubbyHandleType.equals(ChubbyHandleType.READ)) {

            if (!canUnlockRoot && handleAbsolutePath.equals(this.rootPath)) {
                throw new ChubbyHandleException("cannot release shared lock from root node");
            }
            client.getLeaseClient().revoke(Long.parseLong(leaseId)).thenCompose(leaseRevokeResponse -> client.getKVClient().get(ByteSequence.from(handleAbsolutePath.toString().getBytes())).thenCompose(getResponse1 -> {
                //check if node exists
                if (getResponse1.getCount() > 0) {
                    ChubbyNodeValue chubbyNodeValue = ChubbyNodeValueDeserializer.deserialize(getResponse1.getKvs().getFirst().getValue().toString());

                    chubbyNodeValue.getMetadata().removeClientLock(username, chubbyHandleType);

                    ByteSequence chubbyNodeValueByteSequence = ByteSequence.from(ChubbyNodeValueSerializer.serialize(chubbyNodeValue).getBytes());

                    return client.getKVClient().put(getResponse1.getKvs().getFirst().getKey(), chubbyNodeValueByteSequence).thenCompose(putResponse -> {
                        logger.trace("released lock on {}", handleAbsolutePath);
                        return CompletableFuture.completedFuture(result.complete(ByteSequence.from(("lock released").getBytes(charset))));
                    });
                } else {
                    throw new RuntimeException(new ChubbyNodeException("node not found"));
                }
            }));
        }

        //root does not have any event subscriptions, so it can be skipped
        if (!handleAbsolutePath.equals(this.rootPath)) {
            logger.trace("about to unsubscribe from all active subscriptions");
            this.unsubscribeFromAllActiveSubscriptions();
        }

        logger.trace("returning");

        // return the result
        return result;
    }

    protected void unsubscribeFromAllActiveSubscriptions() {
        ChubbyUnsubscribeProcessor.process(this.watcherResponse);
    }

    /**
     * Writes the specified content to the specified node
     *
     * @param chubbyRequest request
     * @param client        etcd client
     * @param filecontent   file content
     * @return a CompletableFuture containing a byte sequence with the result of the operation
     * @throws ChubbyNodeException   if the node cannot be accessed
     * @throws ChubbyHandleException if the handle type is not recognized
     */
    public CompletableFuture<ByteSequence> write(@NotNull ChubbyRequest chubbyRequest, @NotNull Client client, String filecontent) throws ChubbyNodeException, ChubbyHandleException {
        return this.write(client, Path.of(chubbyRequest.getHandleAbsolutePath()), chubbyRequest.getChubbyCurrentHandleType(), filecontent);
    }

    /**
     * Writes the specified content to the specified node
     *
     * @param client             etcd client
     * @param handleAbsolutePath absolute path of the node to write
     * @param chubbyHandleType   handle type of the client to write
     * @param filecontent        file content
     * @return a CompletableFuture containing a byte sequence with the result of the operation
     * @throws ChubbyNodeException   if the node cannot be accessed
     * @throws ChubbyHandleException if the handle type is not recognized
     */
    protected CompletableFuture<ByteSequence> write(@NotNull Client client, @NotNull Path
            handleAbsolutePath, ChubbyHandleType chubbyHandleType, String filecontent) throws
            ChubbyNodeException, ChubbyHandleException {
        CompletableFuture<ByteSequence> resultMessage = new CompletableFuture<>();

        if (!ChubbyUtils.isFile(handleAbsolutePath)) {
            logger.error("cannot do write operation into directory nodes");
            throw new ChubbyNodeException("cannot do write operation into directory nodes");
        }

        if (!chubbyHandleType.equals(ChubbyHandleType.WRITE)) {
            logger.error("cannot do write operation with current handle");
            throw new ChubbyHandleException("cannot do write operation with current handle");
        }

        CompletableFuture<GetResponse> getFuture = client.getKVClient().get(ByteSequence.from(handleAbsolutePath.toString().getBytes(charset)));
        getFuture.whenComplete((getResponse, throwable) -> {
            if (throwable != null) {
                logger.error("failed to retrieve node", throwable);
                throw new RuntimeException(new ChubbyNodeException("failed to retrieve node"));
            }
            // if the resource exists (count of keys is greater than 0), modify its 'file_content' field
            else if (getResponse.getCount() > 0) {
                logger.trace("acquired node {}", handleAbsolutePath);

                ByteSequence nodeValueByteSequence = getResponse.getKvs().getFirst().getValue();

                ChubbyNodeValue chubbyNodeValue = ChubbyNodeValueDeserializer.deserialize(nodeValueByteSequence.toString());
                chubbyNodeValue.setFilecontent(filecontent);

                String chubbyNodeValueJsonString = ChubbyNodeValueSerializer.serialize(chubbyNodeValue);

                // put the key-value pair into the KV store
                CompletableFuture<PutResponse> putFuture = client.getKVClient().put(
                        ByteSequence.from(handleAbsolutePath.toString().getBytes(charset)),
                        ByteSequence.from(chubbyNodeValueJsonString.getBytes(charset))
                );

                putFuture.whenComplete((putResponse, putThrowable) -> {
                    if (putThrowable != null) {
                        logger.error("failed to update node's 'file content'", putThrowable);
                        throw new RuntimeException(new ChubbyNodeException("failed to update node's 'file content'"));

                    } else {
                        logger.trace("updated node {}", handleAbsolutePath);
                        resultMessage.complete(ByteSequence.from("node content updated successfully".getBytes()));
                    }
                });
            }
        });

        return resultMessage;
    }

    /**
     * returns the specified chubby node
     *
     * @param client           etcd client
     * @param nodeAbsolutePath absolute path of the node
     * @return a CompletableFuture containing the chubby node
     */
    public CompletableFuture<ChubbyNode> getNode(@NotNull Client client, Path nodeAbsolutePath) {
        logger.trace("requested 'node data' operation on 'path:{}'", nodeAbsolutePath);

        KV kvClient = client.getKVClient();
        return kvClient.get(ByteSequence.from(nodeAbsolutePath.toString().getBytes())).thenCompose(getResponse -> {
            Path responseAbsolutePath = Path.of(getResponse.getKvs().getFirst().getKey().toString());

            ChubbyNodeValue chubbyNodeValue = ChubbyNodeValueDeserializer.deserialize(getResponse.getKvs().getFirst().getValue().toString());

            return CompletableFuture.completedFuture(new ChubbyNode(responseAbsolutePath, chubbyNodeValue));
        });
    }

    /**
     * returns a list of child nodes of argument node
     *
     * @param client           etcd client
     * @param startingNodePath absolute path of the node
     * @param depth            depth of the search
     * @return a CompletableFuture containing a list of child nodes
     */
    public CompletableFuture<List<String>> getLs(@NotNull Client client, @NotNull Path startingNodePath, int depth) {
        logger.trace("requested 'ls' operation on 'path:{}' with 'depth:{}'", startingNodePath, depth);

        KV kvClient = client.getKVClient();
        ByteSequence prefix = ByteSequence.from(startingNodePath.toString().getBytes());
        logger.trace("prefix: '{}'", prefix.toString());

        GetOption getOption = GetOption.newBuilder()
                .withPrefix(prefix)
                .build();

        int startingPathDepth;
        if (startingNodePath.toString().equals("\\")) {
            startingPathDepth = startingNodePath.toString().split("\\\\", -1).length;
        } else {
            startingPathDepth = startingNodePath.toString().split("\\\\", -1).length + 1;
        }
        int finalDepth = depth + startingPathDepth;

        logger.trace("starting path depth set at '{}'", startingPathDepth);
        logger.trace("final path depth set at '{}'", finalDepth);

        return kvClient.get(prefix, getOption).thenCompose(response -> CompletableFuture.completedFuture(response.getKvs().stream()
                .map(kv -> {
                    logger.trace("mapping to string: '{}'", kv.getKey().toString());
                    return kv.getKey().toString();
                })
                .filter(key -> {
                    int keyDepth = key.split("\\\\").length + 1;
                    logger.trace("filtering out key if keyDepth is not between starting and final depth: '{}<={}<={}'", startingPathDepth, keyDepth, finalDepth);
                    return keyDepth >= startingPathDepth && keyDepth <= finalDepth;
                })
                .filter(key -> {
                    Path path = Path.of(key);
                    logger.trace("filtering out key if it's the same one passed as argument: '({})==({})?'", path, startingNodePath);
                    return !path.equals(startingNodePath);
                })
                .map(key -> {
                    String ret = key.substring(startingNodePath.toString().length());
                    logger.trace("removed starting node path from key string, adding to results: '{}'", ret);
                    return ret;
                })
                .filter(key -> !key.isEmpty())
                .collect(Collectors.toList())));
    }

    /**
     * returns a list of child nodes of argument node
     *
     * @param client           etcd client
     * @param startingNodePath absolute path of the node
     * @param depth            depth of the search
     * @return a CompletableFuture containing a list of child nodes
     */
    protected CompletableFuture<List<String>> getLs(@NotNull Client client, @NotNull ByteSequence startingNodePath, int depth) {
        return this.getLs(client, Path.of(startingNodePath.toString()), depth);
    }

    /**
     * returns a list of child nodes of argument node
     *
     * @param client           etcd client
     * @param startingNodePath absolute path of the node
     * @param depth            depth of the search
     * @return a CompletableFuture containing a list of child nodes
     */
    protected CompletableFuture<List<String>> getLs(@NotNull Client client, @NotNull String startingNodePath, int depth) {
        return this.getLs(client, Path.of(startingNodePath), depth);
    }

    /**
     * checks if the specified node is a default node
     *
     * @param absolutePath absolute path of the node
     * @return a boolean indicating if the specified node is a default node
     */
    protected boolean isDefaultNode(Path absolutePath) {
        return this.defaultNodesCompleteList.contains(absolutePath);
    }

    public List<Path> getDefaultNodeList() {
        return this.defaultNodesCompleteList;
    }

    public Path getRoot() {
        return this.rootPath;
    }
}