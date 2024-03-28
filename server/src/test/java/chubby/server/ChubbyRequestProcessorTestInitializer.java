package chubby.server;

import chubby.control.handle.ChubbyHandleRequest;
import chubby.control.handle.ChubbyHandleResponse;
import chubby.control.handle.ChubbyHandleType;
import chubby.control.handle.ChubbyLockDelay;
import chubby.control.message.ChubbyResponse;
import chubby.utils.exceptions.ChubbyHandleException;
import chubby.utils.exceptions.ChubbyLockException;
import io.etcd.jetcd.Client;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static chubby.server.ChubbyCell.setChubbyCellTestModeTo;

public abstract class ChubbyRequestProcessorTestInitializer {
    private static final int MAX_LOCKDELAY_SECONDS = 60;
    private static final int SETUP_TEARDOWN_WAIT_MILLIS = 10000;    //tests may sometimes fail if this value is too low, because the docker containers may not be up and running at the moment of the test execution
    protected ChubbyNamespace chubbyNamespace;
    protected Client client;
    protected ChubbyRequestProcessor chubbyRequestProcessor;
    protected ChubbyResponse rootChubbyResponse;
    private PrintStream originalOut;
    private PrintStream fileOut;
    private File outputFile;
    private final String[] servers = new String[]{
            "http://localhost:10000",
            "http://localhost:10001",
            "http://localhost:10002",
            "http://localhost:10003",
            "http://localhost:10004"};

    @BeforeEach
    void setUp() throws ExecutionException, InterruptedException, ChubbyHandleException, ChubbyLockException, IOException {
        // Save the original System.out
        this.originalOut = System.out;

        // Create a file to store the output
        this.outputFile = new File("output.txt");
        this.fileOut = new PrintStream(this.outputFile);

        // Redirect System.out to the file
        System.setOut(this.fileOut);

        Process process = this.startProcess(new File(".."), "docker-compose", "up");
        // startProcess(new File(".."), "gradle", "run_client_0");

        Thread.sleep(SETUP_TEARDOWN_WAIT_MILLIS);   //used to make sure that the docker containers are up and running

        String[] args = new String[]{"test_client", "", "local"};
        setChubbyCellTestModeTo(true);
        ChubbyCell.main(args);
        this.chubbyNamespace = new ChubbyNamespace("local");
        List<Path> defNodeList = this.chubbyNamespace.getDefaultNodeList();

        this.client = Client.builder().endpoints(this.servers).build();
        this.chubbyRequestProcessor = new ChubbyRequestProcessor();

        //get initial shared lock on root node
        ChubbyHandleResponse rootChubbyHandleResponse = this.chubbyNamespace.createHandle("test_client", this.client, new ChubbyHandleRequest(this.chubbyNamespace.getRoot(), ChubbyHandleType.READ, new ChubbyLockDelay(MAX_LOCKDELAY_SECONDS))).get();

        this.rootChubbyResponse = new ChubbyResponse("test_client", null, rootChubbyHandleResponse);
    }

    @AfterEach
    void tearDown() throws IOException, InterruptedException {
        // Close the PrintStream
        this.fileOut.close();

        // Restore System.out and System.in
        System.setOut(this.originalOut);
        System.setIn(System.in);

        // Delete the output file
        if (this.outputFile.exists()) {
//            this.outputFile.delete();
        }

        Process process = this.startProcess(new File(".."), "docker-compose", "down");
        process.waitFor();
        Thread.sleep(SETUP_TEARDOWN_WAIT_MILLIS);   //used to make sure that the docker containers are down

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
