package chubby.server;

import io.etcd.jetcd.Client;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChubbyNamespaceLsCommandTest extends ChubbyNamespaceTestInitializer {
    @Test
    void user_ls_default_depth1() throws ExecutionException, InterruptedException {
        Client client = Client.builder().endpoints(this.servers).build();
        Path startingPath = Path.of("/ls/local/prova/getLs");

        assertEquals(List.of("\\testDepthA1", "\\testDepthB1", "\\testDepthC1"), this.chubbyNamespace.getLs(client, startingPath, 1).get());
    }

    @Test
    void user_ls_default_depth2() throws ExecutionException, InterruptedException {
        Client client = Client.builder().endpoints(this.servers).build();
        Path startingPath = Path.of("/ls/local/prova/getLs");

        assertEquals(List.of("\\testDepthA1", "\\testDepthB1", "\\testDepthB1\\testDepthA2", "\\testDepthC1", "\\testDepthC1\\testDepthB2", "\\testDepthC1\\testDepthC2", "\\testDepthC1\\testDepthD2"), this.chubbyNamespace.getLs(client, startingPath, 2).get());
    }

    @Test
    void user_ls_default_depth3() throws ExecutionException, InterruptedException {
        Client client = Client.builder().endpoints(this.servers).build();
        Path startingPath = Path.of("/ls/local/prova/getLs");

        assertEquals(List.of("\\testDepthA1", "\\testDepthB1", "\\testDepthB1\\testDepthA2", "\\testDepthC1", "\\testDepthC1\\testDepthB2", "\\testDepthC1\\testDepthB2\\testDepthA3", "\\testDepthC1\\testDepthC2", "\\testDepthC1\\testDepthC2\\testDepthB3", "\\testDepthC1\\testDepthD2", "\\testDepthC1\\testDepthD2\\testDepthC3"), this.chubbyNamespace.getLs(client, startingPath, 3).get());
    }

    @Test
    void user_ls_default_sameStartingName() throws ExecutionException, InterruptedException {
        Client client = Client.builder().endpoints(this.servers).build();
        Path startingPath = Path.of("/ls/local/prova/getLs/testDepthC1");

        assertEquals(List.of("\\testDepthB2", "\\testDepthC2", "\\testDepthD2"), this.chubbyNamespace.getLs(client, startingPath, 1).get());
    }

}
