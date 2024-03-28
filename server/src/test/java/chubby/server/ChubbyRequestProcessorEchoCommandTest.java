package chubby.server;

import chubby.control.message.ChubbyRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChubbyRequestProcessorEchoCommandTest extends ChubbyRequestProcessorTestInitializer {
    @Test
    void process_fromRoot_sharedLock_echo_empty() {
        String commandString = "echo";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_echo_blank() {
        String commandString = "echo ";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_echo_null() {
        String commandString = "echo null";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "null";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_echo_custom() {
        String commandString = "echo hello world!";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "hello world!";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_echo_uppercase_custom() {
        String commandString = "ECHO hello world!";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "no matching command found: 'ECHO', for a list of possible commands digit 'list cmd', for a detailed explanation digit 'help'";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_echo_uppercaseLowercase_custom() {
        String commandString = "EcHo hello world!";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "no matching command found: 'EcHo', for a list of possible commands digit 'list cmd', for a detailed explanation digit 'help'";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

}
