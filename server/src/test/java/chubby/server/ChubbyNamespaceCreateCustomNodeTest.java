package chubby.server;

import chubby.server.node.ChubbyNodeAttribute;
import chubby.utils.exceptions.ChubbyNodeException;
import io.etcd.jetcd.Client;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChubbyNamespaceCreateCustomNodeTest extends ChubbyNamespaceTestInitializer {
    // CREATE CUSTOM NODE CHILD OF DEFAULT NODE

    @Test
    void user_create_customNode_dir_childOfDefaultNode_root_permanent() {
        Client client = Client.builder().endpoints(this.servers).build();
        ChubbyNodeException exception = assertThrows(ChubbyNodeException.class, () -> this.chubbyNamespace.createNode(client, this.chubbyNamespace.getRoot().resolve("prova"), ChubbyNodeAttribute.PERMANENT, false).get());
        assertEquals("cannot create child of a default node except for '\\ls\\local'", exception.getMessage());
    }

    @Test
    void user_create_customNode_dir_childOfDefaultNode_ls_permanent() {
        Client client = Client.builder().endpoints(this.servers).build();
        ChubbyNodeException exception = assertThrows(ChubbyNodeException.class, () -> this.chubbyNamespace.createNode(client, this.defNodeList.get(1).resolve("prova"), ChubbyNodeAttribute.PERMANENT, false).get());
        assertEquals("cannot create child of a default node except for '\\ls\\local'", exception.getMessage());
    }

    @Test
    void user_create_customNode_dir_childOfDefaultNode_local_permanent() {
        Client client = Client.builder().endpoints(this.servers).build();
        assertDoesNotThrow(() -> this.chubbyNamespace.createNode(client, this.defNodeList.get(2).resolve("prova"), ChubbyNodeAttribute.PERMANENT, false).get());
    }

    @Test
    void user_create_customNode_dir_childOfDefaultNode_acl_permanent() {
        Client client = Client.builder().endpoints(this.servers).build();
        ChubbyNodeException exception = assertThrows(ChubbyNodeException.class, () -> this.chubbyNamespace.createNode(client, this.defNodeList.get(3).resolve("prova"), ChubbyNodeAttribute.PERMANENT, false).get());
        assertEquals("cannot create child of a default node except for '\\ls\\local'", exception.getMessage());
    }

    @Test
    void user_create_customNode_dir_childOfDefaultNode_write_permanent() {
        Client client = Client.builder().endpoints(this.servers).build();
        ChubbyNodeException exception = assertThrows(ChubbyNodeException.class, () -> this.chubbyNamespace.createNode(client, this.defNodeList.get(4).resolve("prova"), ChubbyNodeAttribute.PERMANENT, false).get());
        assertEquals("cannot create child of a default node except for '\\ls\\local'", exception.getMessage());
    }

    @Test
    void user_create_customNode_dir_childOfDefaultNode_read_permanent() {
        Client client = Client.builder().endpoints(this.servers).build();
        ChubbyNodeException exception = assertThrows(ChubbyNodeException.class, () -> this.chubbyNamespace.createNode(client, this.defNodeList.get(5).resolve("prova"), ChubbyNodeAttribute.PERMANENT, false).get());
        assertEquals("cannot create child of a default node except for '\\ls\\local'", exception.getMessage());
    }

    @Test
    void user_create_customNode_dir_childOfDefaultNode_changeAcl_permanent() {
        Client client = Client.builder().endpoints(this.servers).build();
        ChubbyNodeException exception = assertThrows(ChubbyNodeException.class, () -> this.chubbyNamespace.createNode(client, this.defNodeList.get(6).resolve("prova"), ChubbyNodeAttribute.PERMANENT, false).get());
        assertEquals("cannot create child of a default node except for '\\ls\\local'", exception.getMessage());
    }

    @Test
    void user_create_customNode_dir_sameNameAsDefaultNode() {
        Client client = Client.builder().endpoints(this.servers).build();

        assertDoesNotThrow(() -> this.chubbyNamespace.createNode(client, Path.of("/ls/local/prova/local/prova"), ChubbyNodeAttribute.PERMANENT, false).get());
    }

    // CREATE NODE OUTSIDE PERMITTED PATH

    @Test
    void user_create_customNode_dir_outsidePermittedPath_root() {
        Client client = Client.builder().endpoints(this.servers).build();
        ChubbyNodeException exception = assertThrows(ChubbyNodeException.class, () -> this.chubbyNamespace.createNode(client, Path.of("/test_dir1/test_dir2/test_dir3"), ChubbyNodeAttribute.PERMANENT, false).get());
        assertEquals("cannot create node here '\\test_dir1\\test_dir2\\test_dir3', all nodes of this cell must start with '\\ls\\local'", exception.getMessage());
    }

    @Test
    void user_create_customNode_dir_outsidePermittedPath_ls() {
        Client client = Client.builder().endpoints(this.servers).build();
        ChubbyNodeException exception = assertThrows(ChubbyNodeException.class, () -> this.chubbyNamespace.createNode(client, Path.of("/ls/test_dir1/test_dir2/test_dir3"), ChubbyNodeAttribute.PERMANENT, false).get());
        assertEquals("cannot create node here '\\ls\\test_dir1\\test_dir2\\test_dir3', all nodes of this cell must start with '\\ls\\local'", exception.getMessage());
    }

    @Test
    void user_create_customNode_file_outsidePermittedPath_root() {
        Client client = Client.builder().endpoints(this.servers).build();
        ChubbyNodeException exception = assertThrows(ChubbyNodeException.class, () -> this.chubbyNamespace.createNode(client, Path.of("/ls/test_dir1/test_dir2/test_dir3"), ChubbyNodeAttribute.PERMANENT, false).get());
        assertEquals("cannot create node here '\\ls\\test_dir1\\test_dir2\\test_dir3', all nodes of this cell must start with '\\ls\\local'", exception.getMessage());
    }

    @Test
    void user_create_customNode_file_outsidePermittedPath_ls() {
        Client client = Client.builder().endpoints(this.servers).build();
        ChubbyNodeException exception = assertThrows(ChubbyNodeException.class, () -> this.chubbyNamespace.createNode(client, Path.of("/ls/test_dir1/test_dir2/test_dir3"), ChubbyNodeAttribute.PERMANENT, false).get());
        assertEquals("cannot create node here '\\ls\\test_dir1\\test_dir2\\test_dir3', all nodes of this cell must start with '\\ls\\local'", exception.getMessage());
    }
}
