package chubby.server;

import chubby.control.message.ChubbyMessage;
import chubby.control.handle.ChubbyHandleResponse;
import chubby.control.handle.ChubbyHandleType;
import chubby.control.message.ChubbyError;
import chubby.control.handle.ChubbyEventType;
import chubby.control.message.ChubbyRequest;
import chubby.control.message.ChubbyResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

class ChubbyRequestProcessorInvalidCommandTest extends ChubbyRequestProcessorTestInitializer {

// NOT A VALID COMMAND

    @Test
    void process_fromRoot_sharedLock_empty() {
        String commandString = "";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "no matching command found: '', for a list of possible commands digit 'list cmd', for a detailed explanation digit 'help'";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_blank() {
        String commandString = " ";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "no matching command found: '', for a list of possible commands digit 'list cmd', for a detailed explanation digit 'help'";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_null() {
        String commandString = "null";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "no matching command found: 'null', for a list of possible commands digit 'list cmd', for a detailed explanation digit 'help'";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_custom() {
        String commandString = "this is not a valid command";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "no matching command found: 'this', for a list of possible commands digit 'list cmd', for a detailed explanation digit 'help'";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }
}