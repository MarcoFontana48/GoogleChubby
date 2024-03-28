package chubby.server;

import chubby.control.handle.ChubbyHandleRequest;
import chubby.control.handle.ChubbyHandleType;
import chubby.control.handle.ChubbyLockDelay;
import chubby.utils.exceptions.ChubbyHandleException;
import chubby.utils.exceptions.ChubbyLockException;
import io.etcd.jetcd.Client;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ChubbyNamespaceLockCommandTest extends ChubbyNamespaceTestInitializer {
    @Test
    void user_lock_write_defaultNode_dir_root() {
        Client client = Client.builder().endpoints(this.servers).build();
        ChubbyLockException exception = assertThrows(ChubbyLockException.class, () -> this.chubbyNamespace.createHandle("test_client", client, new ChubbyHandleRequest(this.chubbyNamespace.getRoot(), ChubbyHandleType.WRITE, new ChubbyLockDelay(MAX_LOCKDELAY_SECONDS))).get());
        assertEquals("cannot get exclusive lock on '\\' since it's a default node", exception.getMessage());
    }

    @Test
    void user_lock_write_defaultNode_dir_ls() {
        Client client = Client.builder().endpoints(this.servers).build();
        ChubbyLockException exception = assertThrows(ChubbyLockException.class, () -> this.chubbyNamespace.createHandle("test_client", client, new ChubbyHandleRequest(this.defNodeList.get(1), ChubbyHandleType.WRITE, new ChubbyLockDelay(MAX_LOCKDELAY_SECONDS))).get());
        assertEquals("cannot get exclusive lock on '\\ls' since it's a default node", exception.getMessage());
    }

    @Test
    void user_lock_write_defaultNode_dir_local() {
        Client client = Client.builder().endpoints(this.servers).build();
        ChubbyLockException exception = assertThrows(ChubbyLockException.class, () -> this.chubbyNamespace.createHandle("test_client", client, new ChubbyHandleRequest(this.defNodeList.get(2), ChubbyHandleType.WRITE, new ChubbyLockDelay(MAX_LOCKDELAY_SECONDS))).get());
        assertEquals("cannot get exclusive lock on '\\ls\\local' since it's a default node", exception.getMessage());
    }

    @Test
    void user_lock_write_defaultNode_dir_acl() {
        Client client = Client.builder().endpoints(this.servers).build();
        ChubbyLockException exception = assertThrows(ChubbyLockException.class, () -> this.chubbyNamespace.createHandle("test_client", client, new ChubbyHandleRequest(this.defNodeList.get(3), ChubbyHandleType.WRITE, new ChubbyLockDelay(MAX_LOCKDELAY_SECONDS))).get());
        assertEquals("cannot get exclusive lock on '\\ls\\local\\acl' since it's a default node", exception.getMessage());
    }

    @Test
    void user_lock_write_defaultNode_dir_write() {
        Client client = Client.builder().endpoints(this.servers).build();
        ChubbyLockException exception = assertThrows(ChubbyLockException.class, () -> this.chubbyNamespace.createHandle("test_client", client, new ChubbyHandleRequest(this.defNodeList.get(4), ChubbyHandleType.WRITE, new ChubbyLockDelay(MAX_LOCKDELAY_SECONDS))).get());
        assertEquals("cannot get exclusive lock on '\\ls\\local\\acl\\write.txt' since it's a default node", exception.getMessage());
    }

    @Test
    void user_lock_write_defaultNode_dir_read() {
        Client client = Client.builder().endpoints(this.servers).build();
        ChubbyLockException exception = assertThrows(ChubbyLockException.class, () -> this.chubbyNamespace.createHandle("test_client", client, new ChubbyHandleRequest(this.defNodeList.get(5), ChubbyHandleType.WRITE, new ChubbyLockDelay(MAX_LOCKDELAY_SECONDS))).get());
        assertEquals("cannot get exclusive lock on '\\ls\\local\\acl\\read.txt' since it's a default node", exception.getMessage());
    }

    @Test
    void user_lock_write_defaultNode_dir_changeAcl() {
        Client client = Client.builder().endpoints(this.servers).build();
        ChubbyLockException exception = assertThrows(ChubbyLockException.class, () -> this.chubbyNamespace.createHandle("test_client", client, new ChubbyHandleRequest(this.defNodeList.get(6), ChubbyHandleType.WRITE, new ChubbyLockDelay(MAX_LOCKDELAY_SECONDS))).get());
        assertEquals("cannot get exclusive lock on '\\ls\\local\\acl\\change_acl.txt' since it's a default node", exception.getMessage());
    }

    @Test
    void user_unlock_read_defaultNode_dir_root() {
        Client client = Client.builder().endpoints(this.servers).build();
        ChubbyHandleException exception = assertThrows(ChubbyHandleException.class, () -> this.chubbyNamespace.unlock("test_client", client, this.chubbyNamespace.getRoot(), ChubbyHandleType.READ, null, null, false));
        assertEquals("cannot release shared lock from root node", exception.getMessage());
    }

}
