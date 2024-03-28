package chubby.server;

import chubby.control.handle.ChubbyEventType;
import chubby.control.handle.ChubbyHandleResponse;
import chubby.control.handle.ChubbyHandleType;
import chubby.control.message.ChubbyError;
import chubby.control.message.ChubbyMessage;
import chubby.control.message.ChubbyRequest;
import chubby.control.message.ChubbyResponse;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ChubbyRequestProcessorOpenCloseCommandsTest extends ChubbyRequestProcessorTestInitializer {
    @Test
    void process_fromRoot_sharedLock_open_empty_lessArgumentsThanNeeded() {
        String commandString = "open";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "expected at least 2 arguments after 'open' command";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_open_blank_lessArgumentsThanNeeded() {
        String commandString = "open ";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "expected at least 2 arguments after 'open' command";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_open_null_lessArgumentsThanNeeded() {
        String commandString = "open null";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "expected at least 2 arguments after 'open' command";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_open_custom_lessArgumentsThanNeeded() {
        String commandString = "open /ls/local/test";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "expected at least 2 arguments after 'open' command";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_open_exclusiveLock_toCustom() {
        //get an exclusive lock on test
        String firstCommandString = "open /ls/local/process_fromRoot_sharedLock_open_exclusiveLock_toCustom write";
        ChubbyRequest firstChubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, firstCommandString);
        ChubbyResponse actualResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, firstChubbyRequest, this.client);
        String actualMessageResponse = actualResponse.getMessage();
        String expectedMessageResponse = "successfully opened node";

//        this.closeCommand(actualResponse);

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_open_exclusiveLock_toCustom_uppercase() {
        //get an exclusive lock on test1
        String firstCommandString = "OPEN /ls/local/test WRITE";
        ChubbyRequest firstChubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, firstCommandString);
        ChubbyError actualResponse = (ChubbyError) this.chubbyRequestProcessor.process(this.chubbyNamespace, firstChubbyRequest, this.client);
        String actualMessageResponse = actualResponse.getMessage();
        String expectedMessageResponse = "no matching command found: 'OPEN', for a list of possible commands digit 'list cmd', for a detailed explanation digit 'help'";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_open_exclusiveLock_toCustom_uppercaseLowercase() {
        //get an exclusive lock on test1
        String firstCommandString = "OpEn /ls/local/test WrItE";
        ChubbyRequest firstChubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, firstCommandString);
        ChubbyError actualResponse = (ChubbyError) this.chubbyRequestProcessor.process(this.chubbyNamespace, firstChubbyRequest, this.client);
        String actualMessageResponse = actualResponse.getMessage();
        String expectedMessageResponse = "no matching command found: 'OpEn', for a list of possible commands digit 'list cmd', for a detailed explanation digit 'help'";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromCustom_exclusiveLock_open_exclusiveLock_toCustom() {
        //get an exclusive lock on test1 first
        String firstCommandString = "open /ls/local/process_fromCustom_exclusiveLock_open_exclusiveLock_toCustom write";
        ChubbyRequest firstChubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, firstCommandString);
        ChubbyResponse firstChubbyResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, firstChubbyRequest, this.client);

        //then try to get another exclusive lock (on test2) while having one on previous node (test1)
        String secondCommandString = "open /ls/local/test1/test2 write";
        ChubbyRequest secondChubbyRequest = new ChubbyRequest("test_client", firstChubbyResponse, secondCommandString);
        ChubbyError actualResponse = (ChubbyError) this.chubbyRequestProcessor.process(this.chubbyNamespace, secondChubbyRequest, this.client);
        String actualMessageResponse = actualResponse.getMessage();
        String expectedMessageResponse = "cannot execute 'open' cmd while having an exclusive lock on another node";

//        this.closeCommand(firstChubbyResponse);

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromNotRoot_sharedLock_open_sharedLock_toCustom() {
        String commandString = "open /ls/local/test read";
        ChubbyResponse lsChubbyResponse = new ChubbyResponse("test_client", null, new ChubbyHandleResponse(Path.of("/ls"), ChubbyHandleType.READ, null, null));
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", lsChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "cannot execute 'open' cmd while having a shared lock on another node that is not root";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromNotRoot_sharedLock_open_exclusiveLock_toCustom() {
        String commandString = "open /ls/local/test write";
        ChubbyResponse lsChubbyResponse = new ChubbyResponse("test_client", null, new ChubbyHandleResponse(Path.of("/ls"), ChubbyHandleType.READ, null, null));
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", lsChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "cannot execute 'open' cmd while having a shared lock on another node that is not root";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_open_sharedLock_customLockDelay_blank4thArgument_toCustom() {
        String commandString = "open /ls/local/test read ";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "successfully opened node";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_open_sharedLock_customLockDelay_null4thArgument_toCustom() {
        String commandString = "open /ls/local/test read null"; //it has to ignore the 4th argument
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "successfully opened node";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_open_wrongHandleType_toCustom() {
        String commandString = "open /ls/local/test notAValidHandleType";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "invalid argumentChubbyHandleType 'notAValidHandleType'";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_open_sharedLock_eventFILE_CONTENTS_MODIFIED_toCustom() {
        String commandString = "open /ls/local/test read FILE_CONTENTS_MODIFIED";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "successfully opened node";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_open_sharedLock_eventCHILD_NODE_ADDED_toCustom() {
        String commandString = "open /ls/local/test read CHILD_NODE_ADDED";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "successfully opened node";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_open_sharedLock_eventCHILD_NODE_REMOVED_toCustom() {
        String commandString = "open /ls/local/test read CHILD_NODE_REMOVED";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "successfully opened node";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_open_sharedLock_eventCHILD_NODE_MODIFIED_toCustom() {
        String commandString = "open /ls/local/test read CHILD_NODE_MODIFIED";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "successfully opened node";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_open_sharedLock_eventHANDLE_INVALID_toCustom() {
        String commandString = "open /ls/local/test read HANDLE_INVALID";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "successfully opened node";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_open_sharedLock_eventCONFLICTING_LOCK_toCustom() {
        String commandString = "open /ls/local/test read CONFLICTING_LOCK";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "successfully opened node";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_open_sharedLock_eventNONE_toCustom() {
        String commandString = "open /ls/local/test read NONE";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "successfully opened node";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_open_sharedLock_eventHandleInvalidUppercase_toCustom() {
        //get an exclusive lock on test1
        String firstCommandString = "open /ls/local/test read HANDLE_INVALID";
        ChubbyRequest firstChubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, firstCommandString);
        ChubbyResponse actualResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, firstChubbyRequest, this.client);
        String actualMessageResponse = actualResponse.getMessage();
        String expectedMessageResponse = "successfully opened node";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_open_sharedLock_eventHandleInvalidUppercaseLowercase_toCustom() {
        //get an exclusive lock on test1
        String firstCommandString = "open /ls/local/test read HaNdLe_InVaLiD";
        ChubbyRequest firstChubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, firstCommandString);
        ChubbyResponse actualResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, firstChubbyRequest, this.client);
        String actualMessageResponse = actualResponse.getMessage();
        String expectedMessageResponse = "successfully opened node";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_open_wrong4thOptionalArgument_toCustom() {
        String commandString = "open /ls/local/test read notAValidArgument";    //it has to ignore the invalid argument
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "successfully opened node";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_open_sharedLock_customLockDelay_ephemeral_toCustom() {
        String commandString = "open /ls/local/test read ephemeral";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "successfully opened node";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_open_sharedLock_customLockDelay_lessThanPermitted_toCustom() {
        String commandString = "open /ls/local/test read -99";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "successfully opened node";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_open_sharedLock_customLockDelay_moreThanPermitted_toCustom() {
        String commandString = "open /ls/local/test read 99";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "successfully opened node";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_open_sharedLock_customLockDelay_toCustom() {
        String commandString = "open /ls/local/test read 30";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "successfully opened node";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_open_sharedLock_permanent_customLockDelay_toCustom() {
        String commandString = "open /ls/local/test read permanent 30";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "successfully opened node";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_open_sharedLock_customLockDelay_permanent_toCustom() {
        String commandString = "open /ls/local/test read 30 permanent";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "successfully opened node";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_open_sharedLock_permanent_eventNotValid_toCustom() {
        String commandString = "open /ls/local/test read permanent notAValidEventSub";   
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "successfully opened node";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_open_sharedLock_eventNotValid_permanent_toCustom() {
        String commandString = "open /ls/local/test read notAValidEventSub permanent";   
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "successfully opened node";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_open_sharedLock_permanent_eventFILE_CONTENTS_MODIFIED_toCustom() {
        String commandString = "open /ls/local/test read permanent FILE_CONTENTS_MODIFIED";   
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "successfully opened node";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_open_sharedLock_permanent_eventCHILD_NODE_ADDED_toCustom() {
        String commandString = "open /ls/local/test read permanent CHILD_NODE_ADDED";   
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "successfully opened node";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_open_sharedLock_permanent_eventCHILD_NODE_REMOVED_toCustom() {
        String commandString = "open /ls/local/test read permanent CHILD_NODE_REMOVED";   
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "successfully opened node";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_open_sharedLock_permanent_eventCHILD_NODE_MODIFIED_toCustom() {
        String commandString = "open /ls/local/test read permanent CHILD_NODE_MODIFIED";   
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "successfully opened node";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_open_sharedLock_permanent_eventHANDLE_INVALID_toCustom() {
        String commandString = "open /ls/local/test read permanent HANDLE_INVALID";   
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "successfully opened node";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_open_sharedLock_permanent_eventCONFLICTING_LOCK_toCustom() {
        String commandString = "open /ls/local/test read permanent CONFLICTING_LOCK";   
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "successfully opened node";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_open_sharedLock_customLockDelay_eventNotValid_toCustom() {
        String commandString = "open /ls/local/test read 30 notAValidEventSub";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "successfully opened node";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_open_sharedLock_customLockDelay_eventFILE_CONTENTS_MODIFIED_toCustom() {
        String commandString = "open /ls/local/test read 30 FILE_CONTENTS_MODIFIED";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "successfully opened node";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_open_sharedLock_customLockDelay_eventCHILD_NODE_ADDED_toCustom() {
        String commandString = "open /ls/local/test read 30 CHILD_NODE_ADDED";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "successfully opened node";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_open_sharedLock_customLockDelay_eventCHILD_NODE_REMOVED_toCustom() {
        String commandString = "open /ls/local/test read 30 CHILD_NODE_REMOVED";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "successfully opened node";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_open_sharedLock_customLockDelay_eventCHILD_NODE_MODIFIED_toCustom() {
        String commandString = "open /ls/local/test read 30 CHILD_NODE_MODIFIED";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "successfully opened node";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_open_sharedLock_customLockDelay_eventHANDLE_INVALID_toCustom() {
        String commandString = "open /ls/local/test read 30 HANDLE_INVALID";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "successfully opened node";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_open_sharedLock_customLockDelay_eventCONFLICTING_LOCK_toCustom() {
        String commandString = "open /ls/local/test read 30 CONFLICTING_LOCK";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "successfully opened node";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_open_sharedLock_customLockDelay_eventNONE_toCustom() {
        String commandString = "open /ls/local/test read 30 NONE";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "successfully opened node";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_open_sharedLock_permanent_customLockDelay_eventNotValid_toCustom() {
        String commandString = "open /ls/local/test read permanent 30 notAValidEventSub";   
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "successfully opened node";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_open_sharedLock_permanent_customLockDelay_eventFILE_CONTENTS_MODIFIED_toCustom() {
        String commandString = "open /ls/local/test read permanent 30 FILE_CONTENTS_MODIFIED";   
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "successfully opened node";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_open_sharedLock_permanent_customLockDelay_eventCHILD_NODE_ADDED_toCustom() {
        String commandString = "open /ls/local/test read permanent 30 CHILD_NODE_ADDED";   
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "successfully opened node";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_open_sharedLock_permanent_customLockDelay_eventCHILD_NODE_REMOVED_toCustom() {
        String commandString = "open /ls/local/test read permanent 30 CHILD_NODE_REMOVED";   
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "successfully opened node";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_open_sharedLock_permanent_customLockDelay_eventCHILD_NODE_MODIFIED_toCustom() {
        String commandString = "open /ls/local/test read permanent 30 CHILD_NODE_MODIFIED";   
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "successfully opened node";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_open_sharedLock_permanent_customLockDelay_eventHANDLE_INVALID_toCustom() {
        String commandString = "open /ls/local/test read permanent 30 HANDLE_INVALID";   
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "successfully opened node";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_open_sharedLock_permanent_customLockDelay_eventCONFLICTING_LOCK_toCustom() {
        String commandString = "open /ls/local/test read permanent 30 CONFLICTING_LOCK";   
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "successfully opened node";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_open_sharedLock_permanent_customLockDelay_eventNONE_toCustom() {
        String commandString = "open /ls/local/test read permanent 30 NONE";   
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "successfully opened node";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_open_sharedLock_eventCONFLICTING_LOCK_customLockDelay_permanent__toCustom() {
        String commandString = "open /ls/local/test read CONFLICTING_LOCK 30 permanent";   
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "successfully opened node";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_open_sharedLock_customLockDelay_eventCONFLICTING_LOCK_permanent__toCustom() {
        String commandString = "open /ls/local/test read 30 CONFLICTING_LOCK permanent";   
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "successfully opened node";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_open_sharedLock_permanent_customLockDelay_eventALL_toCustom() {
        String commandString = "open /ls/local/test read permanent 30 " + Arrays.toString(ChubbyEventType.values()).replace("[", "").replace("]", "");   
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "successfully opened node";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_open_sharedLock_permanent_customLockDelay_eventALLPlusInvalidOnes_toCustom() {
        String commandString = "open /ls/local/test read permanent 30 notAValidEventSub" + Arrays.toString(ChubbyEventType.values()).replace("[", "").replace("]", "") + "notAValidEventSub";   
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "successfully opened node";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

//CLOSE

    @Test
    void process_fromRoot_sharedLock_close_empty() {
        String commandString = "close";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "cannot release shared lock from root node";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_close_blank() {
        String commandString = "close ";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "cannot release shared lock from root node";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_close_null() {
        String commandString = "close null";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "cannot release shared lock from root node";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_close_invalidArguments() {
        String commandString = "close those are not valid arguments";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();
        String expectedMessageResponse = "cannot release shared lock from root node";

        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromCustom_sharedLock_close() {
        //obtain shared lock on 'test' node
        String firstCommandString = "open /ls/local/test read";
        ChubbyRequest firstChubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, firstCommandString);
        ChubbyResponse actualResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, firstChubbyRequest, this.client);

        //close
        String secondCommandString = "close";
        ChubbyRequest secondChubbyRequest = new ChubbyRequest("test_client", actualResponse, secondCommandString);
        ChubbyResponse chubbyResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, secondChubbyRequest, this.client);
        String actualMessageResponse = chubbyResponse.getMessage();
        String expectedMessageResponse = "lock released, successfully acquired shared lock on root node";

//        this.exitCommand(chubbyResponse);
        assertEquals(expectedMessageResponse, actualMessageResponse);
    }

    @Test
    void process_fromCustom_sharedLock_close_ephemeral() throws ExecutionException, InterruptedException {
        //obtain shared lock on 'test' node
        String firstCommandString = "open /ls/local/process_fromCustom_sharedLock_close_ephemeral read ephemeral";
        ChubbyRequest firstChubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, firstCommandString);
        ChubbyResponse actualResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, firstChubbyRequest, this.client);

        List<String> firstLsResponse = this.chubbyNamespace.getLs(this.client, this.chubbyNamespace.getRoot(), 100).get();

        //close
        String secondCommandString = "close";
        ChubbyRequest secondChubbyRequest = new ChubbyRequest("test_client", actualResponse, secondCommandString);
        ChubbyMessage chubbyResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, secondChubbyRequest, this.client);
        String actualMessageResponse = chubbyResponse.getMessage();

        List<String> secondLsResponse = this.chubbyNamespace.getLs(this.client, this.chubbyNamespace.getRoot(), 100).get();

//        this.exitCommand(chubbyResponse);
        assertTrue(firstLsResponse.contains("ls\\local\\process_fromCustom_sharedLock_close_ephemeral") && !secondLsResponse.contains("ls\\local\\process_fromCustom_sharedLock_close_ephemeral"));
    }

    @Test
    void process_fromCustom_sharedLock_close_permanent() throws ExecutionException, InterruptedException {
        //obtain shared lock on 'test' node
        String firstCommandString = "open /ls/local/process_fromCustom_sharedLock_close_permanent read permanent";
        ChubbyRequest firstChubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, firstCommandString);
        ChubbyResponse actualResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, firstChubbyRequest, this.client);

        List<String> firstLsResponse = this.chubbyNamespace.getLs(this.client, this.chubbyNamespace.getRoot(), 100).get();

        //close
        String secondCommandString = "close";
        ChubbyRequest secondChubbyRequest = new ChubbyRequest("test_client", actualResponse, secondCommandString);
        ChubbyResponse chubbyResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, secondChubbyRequest, this.client);
        String actualMessageResponse = chubbyResponse.getMessage();

        List<String> secondLsResponse = this.chubbyNamespace.getLs(this.client, this.chubbyNamespace.getRoot(), 100).get();

//        this.exitCommand(chubbyResponse);
        assertTrue(firstLsResponse.contains("ls\\local\\process_fromCustom_sharedLock_close_permanent") && secondLsResponse.contains("ls\\local\\process_fromCustom_sharedLock_close_permanent"));
    }

}
