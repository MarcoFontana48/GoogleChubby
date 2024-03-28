package chubby.server;

import chubby.control.message.ChubbyError;
import chubby.control.message.ChubbyRequest;
import chubby.control.message.ChubbyResponse;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class ChubbyRequestProcessorNodeCommandTest extends ChubbyRequestProcessorTestInitializer {
    @Test
    void process_fromRoot_sharedLock_node_empty() {
        String commandString = "node";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "expecting one argument after 'node' command, possible arguments: 'data', 'metadata'";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_node_blank() {
        String commandString = "node ";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "expecting one argument after 'node' command, possible arguments: 'data', 'metadata'";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_node_null() {
        String commandString = "node null";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "wrong argument, possible arguments: 'data', 'metadata'";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_node_wrongArgument() {
        String commandString = "node notAValidArgument";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "wrong argument, possible arguments: 'data', 'metadata'";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_node_data() {
        String commandString = "node data";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();

        assertTrue(actualMessageResponse.startsWith("\\:ChubbyNodeValue {\n" +
                "\tfilecontent"));
    }

    @Test
    void process_fromRoot_sharedLock_node_metadata() {
        String commandString = "node metadata";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();

        assertTrue(actualMessageResponse.startsWith("\\:ChubbyNodeMetadata {\n" +
                "\tchecksum"));
    }

    @Test
    void process_fromRoot_sharedLock_node_metadata_tooManyArguments() {
        String commandString = "node metadata other invalid arguments"; //arguments in excess need to be ignored
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();

        assertTrue(actualMessageResponse.startsWith("\\:ChubbyNodeMetadata {\n" +
                "\tchecksum"));
    }

    @Test
    void check_metadata_afterLockRequest() {
        String commandString1 = "open /ls/local/test write";
        ChubbyRequest chubbyRequest1 = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString1);
        ChubbyResponse chubbyResponse1 = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest1, this.client);
        String chubbyResponse1Message = chubbyResponse1.getMessage();

        String commandString2 = "node metadata"; //arguments in excess need to be ignored
        ChubbyRequest chubbyRequest2 = new ChubbyRequest("test_client", chubbyResponse1, commandString2);
        ChubbyResponse chubbyResponse2 = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest2, this.client);
        String chubbyResponse2Message = chubbyResponse2.getMessage();

        assertTrue(chubbyResponse2Message.contains("lockGenerationNumber = -9223372036854775807"));
    }

    @Test
    void check_metadata_afterChangeAcl() {
        String commandString1 = "open /ls/local/test change_acl";
        ChubbyRequest chubbyRequest1 = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString1);
        ChubbyResponse chubbyResponse1 = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest1, this.client);
        String chubbyResponse1Message = chubbyResponse1.getMessage();

        String commandString2 = "write acl read new_read_acl_name";
        ChubbyRequest chubbyRequest2 = new ChubbyRequest("test_client", chubbyResponse1, commandString2);
        ChubbyResponse chubbyResponse2 = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest2, this.client);
        String chubbyResponse2Message = chubbyResponse2.getMessage();

        String commandString3 = "node metadata";
        ChubbyRequest chubbyRequest3 = new ChubbyRequest("test_client", chubbyResponse2, commandString3);
        ChubbyResponse chubbyResponse3 = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest3, this.client);
        String chubbyResponse3Message = chubbyResponse3.getMessage();

        assertAll(
                () -> assertTrue(chubbyResponse3Message.contains("aclGenerationNumber = -9223372036854775807")),
                () -> assertTrue(chubbyResponse3Message.contains("READ=new_read_acl_name"))
        );
    }
}
