package chubby.server;

import chubby.control.message.ChubbyRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChubbyRequestProcessorLsCommandTest extends ChubbyRequestProcessorTestInitializer {
    @Test
    void process_fromRoot_sharedLock_ls_empty() {
        String commandString = "ls";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "\n" +
                "ls";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_ls_blank() {
        String commandString = "ls ";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "\n" +
                "ls";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_ls_null() {
        String commandString = "ls null";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "invalid depth 'null'";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_ls_depth1() {
        String commandString = "ls 1";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "\n" +
                "ls";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_ls_depthLessThan1() {
        String commandString = "ls -99";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "\n" +
                "ls";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_ls_depth2() {
        String commandString = "ls 2";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = """

                ls
                ls\\local""";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_ls_uppercase_depth2() {
        String commandString = "LS 2";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "no matching command found: 'LS', for a list of possible commands digit 'list cmd', for a detailed explanation digit 'help'";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_ls_uppercaseLowercase_depth2() {
        String commandString = "Ls 2";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "no matching command found: 'Ls', for a list of possible commands digit 'list cmd', for a detailed explanation digit 'help'";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

}
