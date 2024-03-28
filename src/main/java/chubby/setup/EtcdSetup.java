package chubby.setup;

import chubby.server.ChubbyNamespace;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;

import java.util.List;

public class EtcdSetup {
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

    public static void main(String[] args) throws Exception {
        // create client
        Client clientLocal = Client.builder().endpoints(localCellServers).build();
        Client clientCell1 = Client.builder().endpoints(cell1Servers).build();
        Client clientCell2 = Client.builder().endpoints(cell2Servers).build();
        KV kvClientLocal = clientLocal.getKVClient();
        KV kvClientCell1 = clientCell1.getKVClient();
        KV kvClientCell2 = clientCell2.getKVClient();

        // put the kv pair
        List<String> usernames = List.of("client0","client1","client2","client3");
        for (String currentUser : usernames) {
            ByteSequence key = ByteSequence.from(currentUser.getBytes());
            ByteSequence value = ByteSequence.from(String.valueOf("password".hashCode()).getBytes());
            kvClientLocal.put(key, value).get();
            kvClientCell1.put(key, value).get();
            kvClientCell2.put(key, value).get();
        }

        ChubbyNamespace chubbyNamespaceLocal = new ChubbyNamespace("local");
        chubbyNamespaceLocal.createDefaultNodes(clientLocal).get();

        ChubbyNamespace chubbyNamespaceCell1 = new ChubbyNamespace("cell1");
        chubbyNamespaceCell1.createDefaultNodes(clientCell1).get();

        ChubbyNamespace chubbyNamespaceCell2 = new ChubbyNamespace("cell2");
        chubbyNamespaceCell2.createDefaultNodes(clientCell2).get();

        System.out.println("cells setup completed successfully");

        // close the client
        clientLocal.close();
        clientCell1.close();
        clientCell2.close();
    }

}
