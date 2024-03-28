package chubby.server;

import chubby.control.message.ChubbyRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChubbyRequestProcessorCurrHandleCommandTest extends ChubbyRequestProcessorTestInitializer {
    @Test
    void process_fromRoot_sharedLock_currHandle_empty() {
        String commandString = "curr_handle";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "lock type: 'READ' on path: '\\'";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_currHandle_blank() {
        String commandString = "curr_handle ";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "lock type: 'READ' on path: '\\'";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_currHandle_null() {
        String commandString = "curr_handle null";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "lock type: 'READ' on path: '\\'";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_currHandle_custom() {
        String commandString = "curr_handle notAValidArgument";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "lock type: 'READ' on path: '\\'";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_currHandle_uppercase_empty() {
        String commandString = "CURR_HANDLE";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "no matching command found: 'CURR_HANDLE', for a list of possible commands digit 'list cmd', for a detailed explanation digit 'help'";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_currHandle_uppercaseLowercase_empty() {
        String commandString = "CuRr_HaNdLe";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "no matching command found: 'CuRr_HaNdLe', for a list of possible commands digit 'list cmd', for a detailed explanation digit 'help'";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

}
