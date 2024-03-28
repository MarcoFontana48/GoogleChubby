package chubby.server;

import chubby.control.message.ChubbyRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChubbyRequestProcessorListCommandTest extends ChubbyRequestProcessorTestInitializer {
    @Test
    void process_fromRoot_sharedLock_list_cmd() {
        String commandString = "list cmd";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = """
                commands that can be used from this handle:
                - echo [msg]
                - *close
                - **open absolutePath handle_type [node_attribute] [lock_delay] [event_sub1 event_sub2 ...]
                - read filecontent
                - read acl
                - node data
                - node metadata
                - ls [depth]
                - list event
                - list defnode
                - list cmd
                - help
                *only while having a handle not on root node
                **only while having a handle on root node""";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_list_cmd_uppercase() {
        String commandString = "LIST CMD";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "no matching command found: 'LIST', for a list of possible commands digit 'list cmd', for a detailed explanation digit 'help'";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_list_cmd_uppercaseLowercase() {
        String commandString = "LiSt CmD";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "no matching command found: 'LiSt', for a list of possible commands digit 'list cmd', for a detailed explanation digit 'help'";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_list_event() {
        String commandString = "list event";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = """

                - FILE_CONTENTS_MODIFIED
                - CHILD_NODE_ADDED
                - CHILD_NODE_REMOVED
                - CHILD_NODE_MODIFIED
                - HANDLE_INVALID
                - CONFLICTING_LOCK
                - NONE
                """;

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_list_event_uppercase() {
        String commandString = "LIST EVENT";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "no matching command found: 'LIST', for a list of possible commands digit 'list cmd', for a detailed explanation digit 'help'";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_list_event_uppercaseLowercase() {
        String commandString = "LiSt EvEnT";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "no matching command found: 'LiSt', for a list of possible commands digit 'list cmd', for a detailed explanation digit 'help'";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_list_defnode() {
        String commandString = "list defnode";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = """

                - \\
                - \\ls
                - \\ls\\local
                - \\ls\\local\\acl
                - \\ls\\local\\acl\\write.txt
                - \\ls\\local\\acl\\read.txt
                - \\ls\\local\\acl\\change_acl.txt""";

        assertEquals(expectedMessageResponse.trim(), actualMessageResponse.trim());
    }

    @Test
    void process_fromRoot_sharedLock_list_defnode_uppercase() {
        String commandString = "LIST DEFNODE";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "no matching command found: 'LIST', for a list of possible commands digit 'list cmd', for a detailed explanation digit 'help'";

        assertEquals(expectedMessageResponse.trim(), actualMessageResponse.trim());
    }

    @Test
    void process_fromRoot_sharedLock_list_defnode_uppercaseLowercase() {
        String commandString = "LiSt DeFnOdE";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "no matching command found: 'LiSt', for a list of possible commands digit 'list cmd', for a detailed explanation digit 'help'";

        assertEquals(expectedMessageResponse.trim(), actualMessageResponse.trim());
    }

    @Test
    void process_fromRoot_sharedLock_list_emptyArgument() {
        String commandString = "list";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "expected at least 1 argument to 'list' cmd, possible arguments are 'event', 'defnode', 'cmd'";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_list_blankArgument() {
        String commandString = "list ";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "expected at least 1 argument to 'list' cmd, possible arguments are 'event', 'defnode', 'cmd'";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_list_nullArgument() {
        String commandString = "list null";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "no matching argument found, possible arguments are 'event', 'defnode', 'cmd'";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_list_wrongArgument() {
        String commandString = "list notAValidArgument";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "no matching argument found, possible arguments are 'event', 'defnode', 'cmd'";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

}
