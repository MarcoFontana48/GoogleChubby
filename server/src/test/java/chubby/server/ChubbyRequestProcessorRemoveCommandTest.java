package chubby.server;

import chubby.control.message.ChubbyMessage;
import chubby.control.message.ChubbyRequest;
import chubby.control.message.ChubbyResponse;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ChubbyRequestProcessorRemoveCommandTest extends ChubbyRequestProcessorTestInitializer {
    @Test
    void process_fromRoot_sharedLock_remove() {
        String commandString = "remove";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "cannot remove node with handle type READ";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromCustom_sharedLock_remove_permanent() throws ExecutionException, InterruptedException {
        //obtain shared lock on 'test' node
        String firstCommandString = "open /ls/local/process_fromCustom_sharedLock_remove_permanent read permanent";
        ChubbyRequest firstChubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, firstCommandString);
        ChubbyResponse actualResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, firstChubbyRequest, this.client);

        //close
        String secondCommandString = "remove";
        ChubbyRequest secondChubbyRequest = new ChubbyRequest("test_client", actualResponse, secondCommandString);
        ChubbyMessage chubbyResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, secondChubbyRequest, this.client);
        String actualMessageResponse = chubbyResponse.getMessage();

//        this.exitCommand(chubbyResponse);
        assertEquals("cannot remove node with handle type READ", actualMessageResponse);
    }

    @Test
    void process_fromCustom_sharedLock_remove_ephemeral() throws ExecutionException, InterruptedException {
        //obtain shared lock on 'test' node
        String firstCommandString = "open /ls/local/process_fromCustom_sharedLock_remove_ephemeral read ephemeral";
        ChubbyRequest firstChubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, firstCommandString);
        ChubbyResponse actualResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, firstChubbyRequest, this.client);

        //close
        String secondCommandString = "remove";
        ChubbyRequest secondChubbyRequest = new ChubbyRequest("test_client", actualResponse, secondCommandString);
        ChubbyMessage chubbyResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, secondChubbyRequest, this.client);
        String actualMessageResponse = chubbyResponse.getMessage();

//        this.closeCommand(chubbyResponse);
        assertEquals("cannot remove node with handle type READ", actualMessageResponse);
    }

    @Test
    void process_fromCustom_exclusiveLockWrite_remove_permanent() throws ExecutionException, InterruptedException {
        //obtain shared lock on 'test' node
        String firstCommandString = "open /ls/local/process_fromCustom_exclusiveLockWrite_remove_permanent write permanent";
        ChubbyRequest firstChubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, firstCommandString);
        ChubbyResponse actualResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, firstChubbyRequest, this.client);

        List<String> firstLsResponse = this.chubbyNamespace.getLs(this.client, this.chubbyNamespace.getRoot(), 100).get();

        //close
        String secondCommandString = "remove";
        ChubbyRequest secondChubbyRequest = new ChubbyRequest("test_client", actualResponse, secondCommandString);
        ChubbyResponse chubbyResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, secondChubbyRequest, this.client);
        String actualMessageResponse = chubbyResponse.getMessage();

        List<String> secondLsResponse = this.chubbyNamespace.getLs(this.client, this.chubbyNamespace.getRoot(), 100).get();

//        this.closeCommand(chubbyResponse);
        assertTrue(firstLsResponse.contains("ls\\local\\process_fromCustom_exclusiveLockWrite_remove_permanent") && !secondLsResponse.contains("ls\\local\\process_fromCustom_exclusiveLockWrite_remove_permanent"));
    }

    @Test
    void process_fromCustom_exclusiveLockWrite_remove_ephemeral() throws ExecutionException, InterruptedException {
        //obtain shared lock on 'test' node
        String firstCommandString = "open /ls/local/process_fromCustom_exclusiveLockWrite_remove_ephemeral write ephemeral";
        ChubbyRequest firstChubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, firstCommandString);
        ChubbyResponse actualResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, firstChubbyRequest, this.client);

        List<String> firstLsResponse = this.chubbyNamespace.getLs(this.client, this.chubbyNamespace.getRoot(), 100).get();

        //remove
        String secondCommandString = "remove";
        ChubbyRequest secondChubbyRequest = new ChubbyRequest("test_client", actualResponse, secondCommandString);
        ChubbyResponse chubbyResponse1 = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, secondChubbyRequest, this.client);

        List<String> secondLsResponse = this.chubbyNamespace.getLs(this.client, this.chubbyNamespace.getRoot(), 100).get();

        assertTrue(firstLsResponse.contains("ls\\local\\process_fromCustom_exclusiveLockWrite_remove_ephemeral") && !secondLsResponse.contains("ls\\local\\process_fromCustom_exclusiveLockWrite_remove_ephemeral"));
    }

}
