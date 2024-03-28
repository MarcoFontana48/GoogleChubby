package chubby.server;

import chubby.control.message.ChubbyRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ChubbyRequestProcessorHelpCommandTest extends ChubbyRequestProcessorTestInitializer {
    @Test
    void process_fromRoot_sharedLock_help_empty() {
        String commandString = "help";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();

        assertTrue(actualMessageResponse.startsWith("list of all commands available:"));
    }

    @Test
    void process_fromRoot_sharedLock_help_empty_uppercase() {
        String commandString = "HELP";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();

        assertFalse(actualMessageResponse.startsWith("list of all commands available:"));
    }

    @Test
    void process_fromRoot_sharedLock_help_empty_uppercaseLowercase() {
        String commandString = "HeLp";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();

        assertFalse(actualMessageResponse.startsWith("list of all commands available:"));
    }

    @Test
    void process_fromRoot_sharedLock_help_blank() {
        String commandString = "help ";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();

        assertTrue(actualMessageResponse.startsWith("list of all commands available:"));
    }

    @Test
    void process_fromRoot_sharedLock_help_null() {
        String commandString = "help null";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();

        assertTrue(actualMessageResponse.startsWith("list of all commands available:"));
    }

    @Test
    void process_fromRoot_sharedLock_help_custom() {
        String commandString = "help notAValidArgument";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();

        assertTrue(actualMessageResponse.startsWith("list of all commands available:"));
    }

}
