package chubby.server;

import chubby.control.handle.ChubbyHandleType;
import chubby.utils.exceptions.ChubbyHandleException;
import io.etcd.jetcd.Client;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ChubbyNamespaceRemoveDefaultNodeTest extends ChubbyNamespaceTestInitializer {
    @Test
    void user_remove_defaultNode_dir_root_permanent() {
        Client client = Client.builder().endpoints(this.servers).build();
        ChubbyHandleException exception = assertThrows(ChubbyHandleException.class, () -> this.chubbyNamespace.removeNode("test_client", client, this.chubbyNamespace.getRoot(), ChubbyHandleType.READ, null, null, false).get());
        assertEquals("cannot remove node with handle type READ", exception.getMessage());
    }

    @Test
    void user_remove_defaultNode_dir_ls_permanent() {
        Client client = Client.builder().endpoints(this.servers).build();
        ChubbyHandleException exception = assertThrows(ChubbyHandleException.class, () -> this.chubbyNamespace.removeNode("test_client", client, this.defNodeList.get(1), ChubbyHandleType.READ, null, null, false).get());
        assertEquals("cannot remove node with handle type READ", exception.getMessage());
    }

    @Test
    void user_remove_defaultNode_dir_local_permanent() {
        Client client = Client.builder().endpoints(this.servers).build();
        ChubbyHandleException exception = assertThrows(ChubbyHandleException.class, () -> this.chubbyNamespace.removeNode("test_client", client, this.defNodeList.get(2), ChubbyHandleType.READ, null, null, false).get());
        assertEquals("cannot remove node with handle type READ", exception.getMessage());
    }

    @Test
    void user_remove_defaultNode_dir_acl_permanent() {
        Client client = Client.builder().endpoints(this.servers).build();
        ChubbyHandleException exception = assertThrows(ChubbyHandleException.class, () -> this.chubbyNamespace.removeNode("test_client", client, this.defNodeList.get(3), ChubbyHandleType.READ, null, null, false).get());
        assertEquals("cannot remove node with handle type READ", exception.getMessage());
    }

    @Test
    void user_remove_defaultNode_dir_write_permanent() {
        Client client = Client.builder().endpoints(this.servers).build();
        ChubbyHandleException exception = assertThrows(ChubbyHandleException.class, () -> this.chubbyNamespace.removeNode("test_client", client, this.defNodeList.get(4), ChubbyHandleType.READ, null, null, false).get());
        assertEquals("cannot remove node with handle type READ", exception.getMessage());
    }

    @Test
    void user_remove_defaultNode_dir_read_permanent() {
        Client client = Client.builder().endpoints(this.servers).build();
        ChubbyHandleException exception = assertThrows(ChubbyHandleException.class, () -> this.chubbyNamespace.removeNode("test_client", client, this.defNodeList.get(5), ChubbyHandleType.READ, null, null, false).get());
        assertEquals("cannot remove node with handle type READ", exception.getMessage());
    }

    @Test
    void user_remove_defaultNode_dir_changeAcl_permanent() {
        Client client = Client.builder().endpoints(this.servers).build();
        ChubbyHandleException exception = assertThrows(ChubbyHandleException.class, () -> this.chubbyNamespace.removeNode("test_client", client, this.defNodeList.get(6), ChubbyHandleType.READ, null, null, false).get());
        assertEquals("cannot remove node with handle type READ", exception.getMessage());
    }

}
