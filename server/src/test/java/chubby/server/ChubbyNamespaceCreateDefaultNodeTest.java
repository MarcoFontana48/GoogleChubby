package chubby.server;

import chubby.server.node.ChubbyNodeAttribute;
import chubby.utils.exceptions.ChubbyNodeException;
import io.etcd.jetcd.Client;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ChubbyNamespaceCreateDefaultNodeTest extends ChubbyNamespaceTestInitializer {
    // CREATE DEFAULT PERMANENT NODES

    @Test
    void user_create_defaultNode_dir_root_permanent() {
        Client client = Client.builder().endpoints(this.servers).build();
        ChubbyNodeException exception = assertThrows(ChubbyNodeException.class, () -> this.chubbyNamespace.createNode(client, this.chubbyNamespace.getRoot(), ChubbyNodeAttribute.PERMANENT, false).get());
        assertEquals("cannot re-create, nor access, default node \\", exception.getMessage());
    }

    @Test
    void user_create_defaultNode_dir_ls_permanent() {
        Client client = Client.builder().endpoints(this.servers).build();
        ChubbyNodeException exception = assertThrows(ChubbyNodeException.class, () -> this.chubbyNamespace.createNode(client, this.defNodeList.get(1), ChubbyNodeAttribute.PERMANENT, false).get());
        assertEquals("cannot re-create, nor access, default node \\ls", exception.getMessage());
    }

    @Test
    void user_create_defaultNode_dir_local_permanent() {
        Client client = Client.builder().endpoints(this.servers).build();
        ChubbyNodeException exception = assertThrows(ChubbyNodeException.class, () -> this.chubbyNamespace.createNode(client, this.defNodeList.get(2), ChubbyNodeAttribute.PERMANENT, false).get());
        assertEquals("cannot re-create, nor access, default node \\ls\\local", exception.getMessage());
    }

    @Test
    void user_create_defaultNode_dir_acl_permanent() {
        Client client = Client.builder().endpoints(this.servers).build();
        ChubbyNodeException exception = assertThrows(ChubbyNodeException.class, () -> this.chubbyNamespace.createNode(client, this.defNodeList.get(3), ChubbyNodeAttribute.PERMANENT, false).get());
        assertEquals("cannot re-create, nor access, default node \\ls\\local\\acl", exception.getMessage());
    }

    @Test
    void user_create_defaultNode_dir_write_permanent() {
        Client client = Client.builder().endpoints(this.servers).build();
        ChubbyNodeException exception = assertThrows(ChubbyNodeException.class, () -> this.chubbyNamespace.createNode(client, this.defNodeList.get(4), ChubbyNodeAttribute.PERMANENT, false).get());
        assertEquals("cannot re-create, nor access, default node \\ls\\local\\acl\\write.txt", exception.getMessage());
    }

    @Test
    void user_create_defaultNode_dir_read_permanent() {
        Client client = Client.builder().endpoints(this.servers).build();
        ChubbyNodeException exception = assertThrows(ChubbyNodeException.class, () -> this.chubbyNamespace.createNode(client, this.defNodeList.get(5), ChubbyNodeAttribute.PERMANENT, false).get());
        assertEquals("cannot re-create, nor access, default node \\ls\\local\\acl\\read.txt", exception.getMessage());
    }

    @Test
    void user_create_defaultNode_dir_changeAcl_permanent() {
        Client client = Client.builder().endpoints(this.servers).build();
        ChubbyNodeException exception = assertThrows(ChubbyNodeException.class, () -> this.chubbyNamespace.createNode(client, this.defNodeList.get(6), ChubbyNodeAttribute.PERMANENT, false).get());
        assertEquals("cannot re-create, nor access, default node \\ls\\local\\acl\\change_acl.txt", exception.getMessage());
    }

    // CREATE DEFAULT EPHEMERAL NODES

    @Test
    void user_create_defaultNode_dir_root_ephemeral() {
        Client client = Client.builder().endpoints(this.servers).build();
        ChubbyNodeException exception = assertThrows(ChubbyNodeException.class, () -> this.chubbyNamespace.createNode(client, this.chubbyNamespace.getRoot(), ChubbyNodeAttribute.EPHEMERAL, false).get());
        assertEquals("cannot re-create, nor access, default node \\", exception.getMessage());
    }

    @Test
    void user_create_defaultNode_dir_ls_ephemeral() {
        Client client = Client.builder().endpoints(this.servers).build();
        ChubbyNodeException exception = assertThrows(ChubbyNodeException.class, () -> this.chubbyNamespace.createNode(client, this.defNodeList.get(1), ChubbyNodeAttribute.EPHEMERAL, false).get());
        assertEquals("cannot re-create, nor access, default node \\ls", exception.getMessage());
    }

    @Test
    void user_create_defaultNode_dir_local_ephemeral() {
        Client client = Client.builder().endpoints(this.servers).build();
        ChubbyNodeException exception = assertThrows(ChubbyNodeException.class, () -> this.chubbyNamespace.createNode(client, this.defNodeList.get(2), ChubbyNodeAttribute.EPHEMERAL, false).get());
        assertEquals("cannot re-create, nor access, default node \\ls\\local", exception.getMessage());
    }

    @Test
    void user_create_defaultNode_dir_acl_ephemeral() {
        Client client = Client.builder().endpoints(this.servers).build();
        ChubbyNodeException exception = assertThrows(ChubbyNodeException.class, () -> this.chubbyNamespace.createNode(client, this.defNodeList.get(3), ChubbyNodeAttribute.EPHEMERAL, false).get());
        assertEquals("cannot re-create, nor access, default node \\ls\\local\\acl", exception.getMessage());
    }

    @Test
    void user_create_defaultNode_dir_write_ephemeral() {
        Client client = Client.builder().endpoints(this.servers).build();
        ChubbyNodeException exception = assertThrows(ChubbyNodeException.class, () -> this.chubbyNamespace.createNode(client, this.defNodeList.get(4), ChubbyNodeAttribute.EPHEMERAL, false).get());
        assertEquals("cannot re-create, nor access, default node \\ls\\local\\acl\\write.txt", exception.getMessage());
    }

    @Test
    void user_create_defaultNode_dir_read_ephemeral() {
        Client client = Client.builder().endpoints(this.servers).build();
        ChubbyNodeException exception = assertThrows(ChubbyNodeException.class, () -> this.chubbyNamespace.createNode(client, this.defNodeList.get(5), ChubbyNodeAttribute.EPHEMERAL, false).get());
        assertEquals("cannot re-create, nor access, default node \\ls\\local\\acl\\read.txt", exception.getMessage());
    }

    @Test
    void user_create_defaultNode_dir_changeAcl_ephemeral() {
        Client client = Client.builder().endpoints(this.servers).build();
        ChubbyNodeException exception = assertThrows(ChubbyNodeException.class, () -> this.chubbyNamespace.createNode(client, this.defNodeList.get(6), ChubbyNodeAttribute.EPHEMERAL, false).get());
        assertEquals("cannot re-create, nor access, default node \\ls\\local\\acl\\change_acl.txt", exception.getMessage());
    }

}
