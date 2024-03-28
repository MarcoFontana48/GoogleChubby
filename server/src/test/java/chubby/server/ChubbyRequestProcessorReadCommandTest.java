package chubby.server;

import chubby.control.handle.ChubbyHandleType;
import chubby.control.message.ChubbyRequest;
import chubby.control.message.ChubbyResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChubbyRequestProcessorReadCommandTest extends ChubbyRequestProcessorTestInitializer {
    @Test
    void process_fromRoot_sharedLock_read_empty() {
        String commandString = "read";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();

        assertEquals("expecting one argument after 'read' command, possible arguments: 'filecontent', 'acl'", actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_read_blank() {
        String commandString = "read ";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();

        assertEquals("expecting one argument after 'read' command, possible arguments: 'filecontent', 'acl'", actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_read_null() {
        String commandString = "read null";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();

        assertEquals("wrong argument, possible arguments: 'filecontent', 'acl'", actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_read_wrongArgument() {
        String commandString = "read wrongArgument";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();

        assertEquals("wrong argument, possible arguments: 'filecontent', 'acl'", actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_read_uppercase_filecontent() {
        String commandString = "READ filecontent";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();

        assertEquals("no matching command found: 'READ', for a list of possible commands digit 'list cmd', for a detailed explanation digit 'help'", actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_read_uppercaseLowercase_filecontent() {
        String commandString = "ReAd filecontent";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();

        assertEquals("no matching command found: 'ReAd', for a list of possible commands digit 'list cmd', for a detailed explanation digit 'help'", actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_read_filecontent_uppercase() {
        String commandString = "read FILECONTENT";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();

        assertEquals("wrong argument, possible arguments: 'filecontent', 'acl'", actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_read_filecontent_uppercaseLowercase() {
        String commandString = "read FiLeCoNtEnT";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();

        assertEquals("wrong argument, possible arguments: 'filecontent', 'acl'", actualMessageResponse);
    }

    @Test
    void process_fromRoot_sharedLock_read_filecontent() {
        String commandString = "read filecontent";
        ChubbyRequest chubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, commandString);

        String actualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, chubbyRequest, this.client).getMessage();

        assertEquals("cannot retrieve 'filecontent' from directory node", actualMessageResponse);
    }

    @Test
    void process_fromCustom_file_writeLock_read_filecontent() {
        //get an exclusive lock on testfile
        String firstCommandString = "open /ls/local/testfile0.txt write";
        ChubbyRequest firstChubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, firstCommandString);
        ChubbyResponse firstActualResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, firstChubbyRequest, this.client);

        //write filecontent
        String secondCommandString = "write filecontent hello world!";
        ChubbyRequest secondChubbyRequest = new ChubbyRequest("test_client", firstActualResponse, secondCommandString);
        ChubbyResponse secondActualResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, secondChubbyRequest, this.client);

        //read filecontent
        String thirdCommandString = "read filecontent";
        ChubbyRequest thirdChubbyRequest = new ChubbyRequest("test_client", secondActualResponse, thirdCommandString);
        String thirdActualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, thirdChubbyRequest, this.client).getMessage();

        assertEquals("hello world!", thirdActualMessageResponse);
    }

    @Test
    void process_fromCustom_file_writeLock_read_acl() {
        //get an exclusive lock on testfile
        String firstCommandString = "open /ls/local/process_fromCustom_file_writeLock_read_acl.txt write";
        ChubbyRequest firstChubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, firstCommandString);
        ChubbyResponse firstActualResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, firstChubbyRequest, this.client);

        //read acl
        String secondCommandString = "read acl";
        ChubbyRequest secondChubbyRequest = new ChubbyRequest("test_client", firstActualResponse, secondCommandString);
        String secondActualMessageResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, secondChubbyRequest, this.client).getMessage();

        String expected = "{READ=read, WRITE=write, CHANGE_ACL=change_acl}";
        String actual = this.chubbyRequestProcessor.process(this.chubbyNamespace, secondChubbyRequest, this.client).getMessage();

        Gson gson = new Gson();
        Type type = new TypeToken<HashMap<ChubbyHandleType, String>>() {
        }.getType();
        HashMap<ChubbyHandleType, String> expectedMap = gson.fromJson(expected, type);
        HashMap<ChubbyHandleType, String> actualMap = gson.fromJson(actual, type);

        assertEquals(expectedMap, actualMap);
    }

}
