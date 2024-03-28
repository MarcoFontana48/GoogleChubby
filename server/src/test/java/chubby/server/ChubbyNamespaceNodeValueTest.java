package chubby.server;

import chubby.control.handle.ChubbyHandleType;
import chubby.server.node.ChubbyNodeAttribute;
import chubby.server.node.ChubbyNodeMetadata;
import chubby.server.node.ChubbyNodeValue;
import chubby.server.node.ChubbyNodeValueDeserializer;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.kv.GetResponse;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChubbyNamespaceNodeValueTest extends ChubbyNamespaceTestInitializer {
    // DEFAULT METADATA NODES

    @Test
    void check_metadata_defaultNode_dir_root_permanent() throws ExecutionException, InterruptedException {
        Client client = Client.builder().endpoints(this.servers).build();

        GetResponse getResponse = client.getKVClient().get(ByteSequence.from(this.chubbyNamespace.getRoot().toString().getBytes())).get();

        String responseNodeValueJsonString = getResponse.getKvs().getFirst().getValue().toString();

        ChubbyNodeValue chubbyNodeValue = ChubbyNodeValueDeserializer.deserialize(responseNodeValueJsonString);
        ChubbyNodeMetadata actualMetadata = chubbyNodeValue.getMetadata();

        ChubbyNodeValue expectedChubbyNodeValue = new ChubbyNodeValue(new ChubbyNodeMetadata(this.chubbyNamespace.getRoot(), null, ChubbyNodeAttribute.PERMANENT));
        expectedChubbyNodeValue.getMetadata().setAclNamesMap(Map.of(ChubbyHandleType.CHANGE_ACL, "change_acl", ChubbyHandleType.WRITE, "write", ChubbyHandleType.READ, "read"));
        expectedChubbyNodeValue.getMetadata().setChildNodeNumber(1);
        expectedChubbyNodeValue.getMetadata().increaseLockGenerationNumber();
        expectedChubbyNodeValue.getMetadata().addClientLock("test_client", ChubbyHandleType.READ);

        assertAll(
                () -> assertEquals(expectedChubbyNodeValue.getMetadata().getChecksum(), actualMetadata.getChecksum()),
                () -> assertEquals(expectedChubbyNodeValue.getMetadata().getInstanceNumber(), actualMetadata.getInstanceNumber()),
                () -> assertEquals(expectedChubbyNodeValue.getMetadata().getLockGenerationNumber(), actualMetadata.getLockGenerationNumber()),
                () -> assertEquals(expectedChubbyNodeValue.getMetadata().getLockRequestNumber(), actualMetadata.getLockRequestNumber()),
                () -> assertEquals(expectedChubbyNodeValue.getMetadata().getAclGenerationNumber(), actualMetadata.getAclGenerationNumber()),
                () -> assertEquals(expectedChubbyNodeValue.getMetadata().getAclGenerationNumber(), actualMetadata.getAclGenerationNumber()),
                () -> assertEquals(expectedChubbyNodeValue.getMetadata().getAclNamesMap(), actualMetadata.getAclNamesMap()),
                () -> assertEquals(expectedChubbyNodeValue.getMetadata().getChubbyNodeType(), actualMetadata.getChubbyNodeType()),
                () -> assertEquals(expectedChubbyNodeValue.getMetadata().getChubbyNodeAttribute(), actualMetadata.getChubbyNodeAttribute())
        );
    }

    @Test
    void check_metadata_defaultNode_dir_ls_permanent() throws ExecutionException, InterruptedException {
        Client client = Client.builder().endpoints(this.servers).build();
        GetResponse getResponse = client.getKVClient().get(ByteSequence.from(this.chubbyNamespace.getDefaultNodeList().get(1).toString().getBytes())).get();

        String responseNodeValueJsonString = getResponse.getKvs().getFirst().getValue().toString();

        ChubbyNodeValue chubbyNodeValue = ChubbyNodeValueDeserializer.deserialize(responseNodeValueJsonString);
        ChubbyNodeMetadata actualMetadata = chubbyNodeValue.getMetadata();

        ChubbyNodeMetadata expectedMetadata = new ChubbyNodeMetadata(this.chubbyNamespace.getDefaultNodeList().get(1), null, ChubbyNodeAttribute.PERMANENT);
        expectedMetadata.setChildNodeNumber(1);

        assertEquals(expectedMetadata, actualMetadata);
    }

    @Test
    void check_metadata_defaultNode_dir_local_permanent() throws ExecutionException, InterruptedException {
        Client client = Client.builder().endpoints(this.servers).build();
        GetResponse getResponse = client.getKVClient().get(ByteSequence.from(this.chubbyNamespace.getDefaultNodeList().get(2).toString().getBytes())).get();

        String responseNodeValueJsonString = getResponse.getKvs().getFirst().getValue().toString();

        ChubbyNodeValue chubbyNodeValue = ChubbyNodeValueDeserializer.deserialize(responseNodeValueJsonString);
        ChubbyNodeMetadata actualMetadata = chubbyNodeValue.getMetadata();

        ChubbyNodeMetadata expectedMetadata = new ChubbyNodeMetadata(this.chubbyNamespace.getDefaultNodeList().get(2), null, ChubbyNodeAttribute.PERMANENT);
        expectedMetadata.setChildNodeNumber(2);

        assertEquals(expectedMetadata, actualMetadata);
    }

    @Test
    void check_metadata_defaultNode_dir_acl_permanent() throws ExecutionException, InterruptedException {
        Client client = Client.builder().endpoints(this.servers).build();
        GetResponse getResponse = client.getKVClient().get(ByteSequence.from(this.chubbyNamespace.getDefaultNodeList().get(3).toString().getBytes())).get();

        String responseNodeValueJsonString = getResponse.getKvs().getFirst().getValue().toString();

        ChubbyNodeValue chubbyNodeValue = ChubbyNodeValueDeserializer.deserialize(responseNodeValueJsonString);
        ChubbyNodeMetadata actualMetadata = chubbyNodeValue.getMetadata();

        ChubbyNodeMetadata expectedMetadata = new ChubbyNodeMetadata(this.chubbyNamespace.getDefaultNodeList().get(3), null, ChubbyNodeAttribute.PERMANENT);
        expectedMetadata.setChildNodeNumber(3);

        assertEquals(expectedMetadata, actualMetadata);
    }

    @Test
    void check_metadata_defaultNode_dir_write_permanent() throws ExecutionException, InterruptedException {
        Client client = Client.builder().endpoints(this.servers).build();
        GetResponse getResponse = client.getKVClient().get(ByteSequence.from(this.chubbyNamespace.getDefaultNodeList().get(4).toString().getBytes())).get();
        String responseNodeValueJsonString = getResponse.getKvs().getFirst().getValue().toString();
        ChubbyNodeValue chubbyNodeValue = ChubbyNodeValueDeserializer.deserialize(responseNodeValueJsonString);
        ChubbyNodeMetadata actualMetadata = chubbyNodeValue.getMetadata();

        ChubbyNodeValue expectedChubbyNodeValue = new ChubbyNodeValue(new ChubbyNodeMetadata(this.chubbyNamespace.getDefaultNodeList().get(4), null, ChubbyNodeAttribute.PERMANENT));
        expectedChubbyNodeValue.getMetadata().setAclNamesMap(Map.of(ChubbyHandleType.CHANGE_ACL, "change_acl", ChubbyHandleType.WRITE, "write", ChubbyHandleType.READ, "read"));

        assertEquals(expectedChubbyNodeValue.getMetadata(), actualMetadata);
    }

    @Test
    void check_metadata_defaultNode_dir_read_permanent() throws ExecutionException, InterruptedException {
        Client client = Client.builder().endpoints(this.servers).build();
        GetResponse getResponse = client.getKVClient().get(ByteSequence.from(this.chubbyNamespace.getDefaultNodeList().get(5).toString().getBytes())).get();
        String responseNodeValueJsonString = getResponse.getKvs().getFirst().getValue().toString();
        ChubbyNodeValue chubbyNodeValue = ChubbyNodeValueDeserializer.deserialize(responseNodeValueJsonString);
        ChubbyNodeMetadata actualMetadata = chubbyNodeValue.getMetadata();

        ChubbyNodeValue expectedChubbyNodeValue = new ChubbyNodeValue(new ChubbyNodeMetadata(this.chubbyNamespace.getDefaultNodeList().get(5), null, ChubbyNodeAttribute.PERMANENT));
        expectedChubbyNodeValue.getMetadata().setAclNamesMap(Map.of(ChubbyHandleType.CHANGE_ACL, "change_acl", ChubbyHandleType.WRITE, "write", ChubbyHandleType.READ, "read"));

        assertEquals(expectedChubbyNodeValue.getMetadata(), actualMetadata);
    }

    @Test
    void check_metadata_defaultNode_dir_changeAcl_permanent() throws ExecutionException, InterruptedException {
        Client client = Client.builder().endpoints(this.servers).build();
        GetResponse getResponse = client.getKVClient().get(ByteSequence.from(this.chubbyNamespace.getDefaultNodeList().get(6).toString().getBytes())).get();
        String responseNodeValueJsonString = getResponse.getKvs().getFirst().getValue().toString();
        ChubbyNodeValue chubbyNodeValue = ChubbyNodeValueDeserializer.deserialize(responseNodeValueJsonString);
        ChubbyNodeMetadata actualMetadata = chubbyNodeValue.getMetadata();

        ChubbyNodeValue expectedChubbyNodeValue = new ChubbyNodeValue(new ChubbyNodeMetadata(this.chubbyNamespace.getDefaultNodeList().get(6), null, ChubbyNodeAttribute.PERMANENT));
        expectedChubbyNodeValue.getMetadata().setAclNamesMap(Map.of(ChubbyHandleType.CHANGE_ACL, "change_acl", ChubbyHandleType.WRITE, "write", ChubbyHandleType.READ, "read"));

        assertEquals(expectedChubbyNodeValue.getMetadata(), actualMetadata);
    }

    // DEFAULT NODE VALUE NODES

    @Test
    void check_nodeValue_defaultNode_dir_root_permanent() throws ExecutionException, InterruptedException {
        Client client = Client.builder().endpoints(this.servers).build();
        GetResponse getResponse = client.getKVClient().get(ByteSequence.from(this.chubbyNamespace.getRoot().toString().getBytes())).get();
        String responseNodeValueJsonString = getResponse.getKvs().getFirst().getValue().toString();
        ChubbyNodeValue actualChubbyNodeValue = ChubbyNodeValueDeserializer.deserialize(responseNodeValueJsonString);

        ChubbyNodeValue expectedChubbyNodeValue = new ChubbyNodeValue(new ChubbyNodeMetadata(this.chubbyNamespace.getRoot(), null, ChubbyNodeAttribute.PERMANENT));
        expectedChubbyNodeValue.getMetadata().setAclNamesMap(Map.of(ChubbyHandleType.CHANGE_ACL, "change_acl", ChubbyHandleType.WRITE, "write", ChubbyHandleType.READ, "read"));
        expectedChubbyNodeValue.getMetadata().setChildNodeNumber(1);
        expectedChubbyNodeValue.getMetadata().increaseLockGenerationNumber();
        expectedChubbyNodeValue.getMetadata().addClientLock("test_client", ChubbyHandleType.READ);

        assertAll(
                () -> assertEquals(expectedChubbyNodeValue.getMetadata().getChecksum(), actualChubbyNodeValue.getMetadata().getChecksum()),
                () -> assertEquals(expectedChubbyNodeValue.getMetadata().getInstanceNumber(), actualChubbyNodeValue.getMetadata().getInstanceNumber()),
                () -> assertEquals(expectedChubbyNodeValue.getMetadata().getLockGenerationNumber(), actualChubbyNodeValue.getMetadata().getLockGenerationNumber()),
                () -> assertEquals(expectedChubbyNodeValue.getMetadata().getLockRequestNumber(), actualChubbyNodeValue.getMetadata().getLockRequestNumber()),
                () -> assertEquals(expectedChubbyNodeValue.getMetadata().getAclGenerationNumber(), actualChubbyNodeValue.getMetadata().getAclGenerationNumber()),
                () -> assertEquals(expectedChubbyNodeValue.getMetadata().getAclGenerationNumber(), actualChubbyNodeValue.getMetadata().getAclGenerationNumber()),
                () -> assertEquals(expectedChubbyNodeValue.getMetadata().getAclNamesMap(), actualChubbyNodeValue.getMetadata().getAclNamesMap()),
                () -> assertEquals(expectedChubbyNodeValue.getMetadata().getChubbyNodeType(), actualChubbyNodeValue.getMetadata().getChubbyNodeType()),
                () -> assertEquals(expectedChubbyNodeValue.getMetadata().getChubbyNodeAttribute(), actualChubbyNodeValue.getMetadata().getChubbyNodeAttribute())
        );
    }

    @Test
    void check_nodeValue_defaultNode_dir_ls_permanent() throws ExecutionException, InterruptedException {
        Client client = Client.builder().endpoints(this.servers).build();
        GetResponse getResponse = client.getKVClient().get(ByteSequence.from(this.chubbyNamespace.getDefaultNodeList().get(1).toString().getBytes())).get();

        String responseNodeValueJsonString = getResponse.getKvs().getFirst().getValue().toString();

        ChubbyNodeValue actualChubbyNodeValue = ChubbyNodeValueDeserializer.deserialize(responseNodeValueJsonString);

        ChubbyNodeMetadata expectedMetadata = new ChubbyNodeMetadata(this.chubbyNamespace.getDefaultNodeList().get(1), null, ChubbyNodeAttribute.PERMANENT);
        expectedMetadata.setChildNodeNumber(1);
        ChubbyNodeValue expectedChubbyNodeValue = new ChubbyNodeValue(null, expectedMetadata);

        assertEquals(expectedChubbyNodeValue, actualChubbyNodeValue);
    }

    @Test
    void check_nodeValue_defaultNode_dir_local_permanent() throws ExecutionException, InterruptedException {
        Client client = Client.builder().endpoints(this.servers).build();
        GetResponse getResponse = client.getKVClient().get(ByteSequence.from(this.chubbyNamespace.getDefaultNodeList().get(2).toString().getBytes())).get();

        String responseNodeValueJsonString = getResponse.getKvs().getFirst().getValue().toString();

        ChubbyNodeValue actualChubbyNodeValue = ChubbyNodeValueDeserializer.deserialize(responseNodeValueJsonString);

        ChubbyNodeMetadata expectedMetadata = new ChubbyNodeMetadata(this.chubbyNamespace.getDefaultNodeList().get(2), null, ChubbyNodeAttribute.PERMANENT);
        expectedMetadata.setChildNodeNumber(2);
        ChubbyNodeValue expectedChubbyNodeValue = new ChubbyNodeValue(null, expectedMetadata);

        assertEquals(expectedChubbyNodeValue, actualChubbyNodeValue);
    }

    @Test
    void check_nodeValue_defaultNode_dir_acl_permanent() throws ExecutionException, InterruptedException {
        Client client = Client.builder().endpoints(this.servers).build();
        GetResponse getResponse = client.getKVClient().get(ByteSequence.from(this.chubbyNamespace.getDefaultNodeList().get(3).toString().getBytes())).get();

        String responseNodeValueJsonString = getResponse.getKvs().getFirst().getValue().toString();

        ChubbyNodeValue actualChubbyNodeValue = ChubbyNodeValueDeserializer.deserialize(responseNodeValueJsonString);

        ChubbyNodeMetadata expectedMetadata = new ChubbyNodeMetadata(this.chubbyNamespace.getDefaultNodeList().get(3), null, ChubbyNodeAttribute.PERMANENT);
        expectedMetadata.setChildNodeNumber(3);
        ChubbyNodeValue expectedChubbyNodeValue = new ChubbyNodeValue(null, expectedMetadata);

        assertEquals(expectedChubbyNodeValue, actualChubbyNodeValue);
    }

    @Test
    void check_nodeValue_defaultNode_dir_write_permanent() throws ExecutionException, InterruptedException {
        Client client = Client.builder().endpoints(this.servers).build();
        GetResponse getResponse = client.getKVClient().get(ByteSequence.from(this.chubbyNamespace.getDefaultNodeList().get(4).toString().getBytes())).get();
        String responseNodeValueJsonString = getResponse.getKvs().getFirst().getValue().toString();
        ChubbyNodeValue actualChubbyNodeValue = ChubbyNodeValueDeserializer.deserialize(responseNodeValueJsonString);

        ChubbyNodeValue expectedChubbyNodeValue = new ChubbyNodeValue(new ChubbyNodeMetadata(this.chubbyNamespace.getDefaultNodeList().get(4), null, ChubbyNodeAttribute.PERMANENT));
        expectedChubbyNodeValue.getMetadata().setAclNamesMap(Map.of(ChubbyHandleType.CHANGE_ACL, "change_acl", ChubbyHandleType.WRITE, "write", ChubbyHandleType.READ, "read"));

        assertEquals(expectedChubbyNodeValue, actualChubbyNodeValue);
    }

    @Test
    void check_nodeValue_defaultNode_dir_read_permanent() throws ExecutionException, InterruptedException {
        Client client = Client.builder().endpoints(this.servers).build();
        GetResponse getResponse = client.getKVClient().get(ByteSequence.from(this.chubbyNamespace.getDefaultNodeList().get(5).toString().getBytes())).get();
        String responseNodeValueJsonString = getResponse.getKvs().getFirst().getValue().toString();
        ChubbyNodeValue actualChubbyNodeValue = ChubbyNodeValueDeserializer.deserialize(responseNodeValueJsonString);

        ChubbyNodeValue expectedChubbyNodeValue = new ChubbyNodeValue(new ChubbyNodeMetadata(this.chubbyNamespace.getDefaultNodeList().get(5), null, ChubbyNodeAttribute.PERMANENT));
        expectedChubbyNodeValue.getMetadata().setAclNamesMap(Map.of(ChubbyHandleType.CHANGE_ACL, "change_acl", ChubbyHandleType.WRITE, "write", ChubbyHandleType.READ, "read"));

        assertEquals(expectedChubbyNodeValue, actualChubbyNodeValue);
    }

    @Test
    void check_nodeValue_defaultNode_dir_changeAcl_permanent() throws ExecutionException, InterruptedException {
        Client client = Client.builder().endpoints(this.servers).build();
        GetResponse getResponse = client.getKVClient().get(ByteSequence.from(this.chubbyNamespace.getDefaultNodeList().get(6).toString().getBytes())).get();
        String responseNodeValueJsonString = getResponse.getKvs().getFirst().getValue().toString();
        ChubbyNodeValue actualChubbyNodeValue = ChubbyNodeValueDeserializer.deserialize(responseNodeValueJsonString);

        ChubbyNodeValue expectedChubbyNodeValue = new ChubbyNodeValue(new ChubbyNodeMetadata(this.chubbyNamespace.getDefaultNodeList().get(6), null, ChubbyNodeAttribute.PERMANENT));
        expectedChubbyNodeValue.getMetadata().setAclNamesMap(Map.of(ChubbyHandleType.CHANGE_ACL, "change_acl", ChubbyHandleType.WRITE, "write", ChubbyHandleType.READ, "read"));

        assertEquals(expectedChubbyNodeValue, actualChubbyNodeValue);
    }

}
