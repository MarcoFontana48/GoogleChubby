package chubby.server;

import chubby.control.handle.ChubbyLockDelay;
import chubby.utils.exceptions.ChubbyHandleException;
import chubby.utils.exceptions.ChubbyLockException;
import chubby.utils.exceptions.ChubbyNodeException;
import chubby.control.handle.ChubbyHandleRequest;
import chubby.control.handle.ChubbyHandleResponse;
import chubby.control.handle.ChubbyHandleType;
import chubby.control.message.ChubbyResponse;
import chubby.server.node.*;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.kv.GetResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static chubby.server.ChubbyCell.*;
import static org.junit.jupiter.api.Assertions.*;

abstract class ChubbyNamespaceTestInitializer {
    protected static final int MAX_LOCKDELAY_SECONDS = 60;
    protected ChubbyNamespace chubbyNamespace;
    protected List<Path> defNodeList;
    private Path defaultTestDir;
    private Client client;
    private ChubbyRequestProcessor chubbyRequestProcessor;
    private ChubbyResponse rootChubbyResponse;
    private static final int SETUP_TEARDOWN_WAIT_MILLIS = 10000;
    final protected String[] servers = new String[]{
            "http://localhost:10000",
            "http://localhost:10001",
            "http://localhost:10002",
            "http://localhost:10003",
            "http://localhost:10004"};

    @BeforeEach
    void setUp() throws ExecutionException, InterruptedException, ChubbyLockException, ChubbyNodeException, IOException, ChubbyHandleException {
        Process process = this.startProcess(new File(".."), "docker-compose", "up");
        Thread.sleep(SETUP_TEARDOWN_WAIT_MILLIS);

        String[] args = new String[]{"test_client", "", "local"};
        setChubbyCellTestModeTo(true);
        ChubbyCell.main(args);
        this.chubbyNamespace = new ChubbyNamespace("local");
        this.defNodeList = this.chubbyNamespace.getDefaultNodeList();
        this.client = Client.builder().endpoints(this.servers).build();
        this.chubbyRequestProcessor = new ChubbyRequestProcessor();

        //get initial shared lock on root node
        ChubbyHandleResponse rootChubbyHandleResponse = this.chubbyNamespace.createHandle("test_client", this.client, new ChubbyHandleRequest(this.chubbyNamespace.getRoot(), ChubbyHandleType.READ, new ChubbyLockDelay(MAX_LOCKDELAY_SECONDS))).get();
        this.rootChubbyResponse = new ChubbyResponse("test_client", null, rootChubbyHandleResponse);
        this.defaultTestDir = Path.of("/ls/local/prova");           //fare tutti i nodi test a partire da questo nodo
        this.client = Client.builder().endpoints(this.servers).build();

        this.chubbyNamespace.createNode(this.client, Path.of("/ls/local/prova/getLs/testDepthA1"), ChubbyNodeAttribute.PERMANENT, false).get();
        this.chubbyNamespace.createNode(this.client, Path.of("/ls/local/prova/getLs/testDepthB1/testDepthA2"), ChubbyNodeAttribute.PERMANENT, false).get();
        this.chubbyNamespace.createNode(this.client, Path.of("/ls/local/prova/getLs/testDepthC1/testDepthB2/testDepthA3"), ChubbyNodeAttribute.PERMANENT, false).get();
        this.chubbyNamespace.createNode(this.client, Path.of("/ls/local/prova/getLs/testDepthC1/testDepthC2/testDepthB3"), ChubbyNodeAttribute.PERMANENT, false).get();
        this.chubbyNamespace.createNode(this.client, Path.of("/ls/local/prova/getLs/testDepthC1/testDepthD2/testDepthC3"), ChubbyNodeAttribute.PERMANENT, false).get();
        this.chubbyNamespace.createNode(this.client, Path.of("/ls/local/prova/writeLock"), ChubbyNodeAttribute.PERMANENT, false).get();
        this.chubbyNamespace.createNode(this.client, Path.of("/ls/local/prova/writeLock/testFile.txt"), ChubbyNodeAttribute.PERMANENT, false).get();
    }

    @AfterEach
    void tearDown() throws IOException, InterruptedException {
        Process process = this.startProcess(new File(".."), "docker-compose", "down");
        process.waitFor();
        Thread.sleep(SETUP_TEARDOWN_WAIT_MILLIS);
    }

    private Process startProcess(File workDir, String... cmdLine) throws IOException {
        var prefix = this.getClass().getName() + "-" + Arrays.hashCode(cmdLine);
        var stdOut = File.createTempFile(prefix + "-stdout", ".txt");
        stdOut.deleteOnExit();
        var stdErr = File.createTempFile(prefix + "-stderr", ".txt");
        stdErr.deleteOnExit();
        return new ProcessBuilder(cmdLine)
                .redirectOutput(ProcessBuilder.Redirect.to(stdOut))
                .redirectError(ProcessBuilder.Redirect.to(stdErr))
                .directory(workDir)
                .start();
    }
}