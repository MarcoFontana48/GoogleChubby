package chubby.server;

import chubby.control.message.ChubbyError;
import chubby.control.message.ChubbyMessage;
import chubby.control.message.ChubbyRequest;
import chubby.control.message.ChubbyResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChubbyRequestProcessorWriteCommandTest extends ChubbyRequestProcessorTestInitializer {
    @Test
    void process_fromCustom_directory_readLock_write_wrongArgument() {
        //obtain shared lock on 'test' node
        String firstCommandString = "open /ls/local/process_fromCustom_directory_readLock_write_filecontent_empty read permanent";
        ChubbyRequest firstChubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, firstCommandString);
        ChubbyResponse actualResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, firstChubbyRequest, this.client);

        //write
        String secondCommandString = "write notAValidArgument";
        ChubbyRequest secondChubbyRequest = new ChubbyRequest("test_client", actualResponse, secondCommandString);
        ChubbyError chubbyResponse = (ChubbyError) this.chubbyRequestProcessor.process(this.chubbyNamespace, secondChubbyRequest, this.client);
        String actualMessageResponse = chubbyResponse.getMessage();

        assertEquals("wrong argument, expected 'filecontent' or 'acl'", actualMessageResponse);
    }

    @Test
    void process_fromCustom_directory_readLock_write_empty() {
        //obtain shared lock on 'test' node
        String firstCommandString = "open /ls/local/process_fromCustom_directory_readLock_write_filecontent_empty read permanent";
        ChubbyRequest firstChubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, firstCommandString);
        ChubbyResponse actualResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, firstChubbyRequest, this.client);

        //write
        String secondCommandString = "write";
        ChubbyRequest secondChubbyRequest = new ChubbyRequest("test_client", actualResponse, secondCommandString);
        ChubbyError chubbyResponse = (ChubbyError) this.chubbyRequestProcessor.process(this.chubbyNamespace, secondChubbyRequest, this.client);
        String actualMessageResponse = chubbyResponse.getMessage();

        assertEquals("missing argument, expected 'filecontent' or 'acl'", actualMessageResponse);
    }

    @Test
    void process_fromCustom_directory_readLock_write_blank() {
        //obtain shared lock on 'test' node
        String firstCommandString = "open /ls/local/process_fromCustom_directory_readLock_write_filecontent_empty read permanent";
        ChubbyRequest firstChubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, firstCommandString);
        ChubbyResponse actualResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, firstChubbyRequest, this.client);

        //write
        String secondCommandString = "write ";
        ChubbyRequest secondChubbyRequest = new ChubbyRequest("test_client", actualResponse, secondCommandString);
        ChubbyError chubbyResponse = (ChubbyError) this.chubbyRequestProcessor.process(this.chubbyNamespace, secondChubbyRequest, this.client);
        String actualMessageResponse = chubbyResponse.getMessage();

        assertEquals("missing argument, expected 'filecontent' or 'acl'", actualMessageResponse);
    }

    @Test
    void process_fromCustom_directory_readLock_write_null() {
        //obtain shared lock on 'test' node
        String firstCommandString = "open /ls/local/process_fromCustom_directory_readLock_write_filecontent_empty read permanent";
        ChubbyRequest firstChubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, firstCommandString);
        ChubbyResponse actualResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, firstChubbyRequest, this.client);

        //write
        String secondCommandString = "write null";
        ChubbyRequest secondChubbyRequest = new ChubbyRequest("test_client", actualResponse, secondCommandString);
        ChubbyError chubbyResponse = (ChubbyError) this.chubbyRequestProcessor.process(this.chubbyNamespace, secondChubbyRequest, this.client);
        String actualMessageResponse = chubbyResponse.getMessage();

        assertEquals("wrong argument, expected 'filecontent' or 'acl'", actualMessageResponse);
    }

    @Test
    void process_fromCustom_directory_readLock_write_filecontent_empty() {
        //obtain shared lock on 'test' node
        String firstCommandString = "open /ls/local/process_fromCustom_directory_readLock_write_filecontent_empty read permanent";
        ChubbyRequest firstChubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, firstCommandString);
        ChubbyResponse actualResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, firstChubbyRequest, this.client);

        //write
        String secondCommandString = "write filecontent";
        ChubbyRequest secondChubbyRequest = new ChubbyRequest("test_client", actualResponse, secondCommandString);
        ChubbyError chubbyResponse = (ChubbyError) this.chubbyRequestProcessor.process(this.chubbyNamespace, secondChubbyRequest, this.client);
        String actualMessageResponse = chubbyResponse.getMessage();

        assertEquals(actualMessageResponse, "cannot do write operation into directory nodes");
    }

    @Test
    void process_fromCustom_file_readLock_write_filecontent_empty() {
        //obtain shared lock on 'test' node
        String firstCommandString = "open /ls/local/process_fromCustom_file_readLock_write_filecontent_empty.txt read permanent";
        ChubbyRequest firstChubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, firstCommandString);
        ChubbyResponse actualResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, firstChubbyRequest, this.client);

        //write
        String secondCommandString = "write filecontent";
        ChubbyRequest secondChubbyRequest = new ChubbyRequest("test_client", actualResponse, secondCommandString);
        ChubbyError chubbyResponse = (ChubbyError) this.chubbyRequestProcessor.process(this.chubbyNamespace, secondChubbyRequest, this.client);
        String actualMessageResponse = chubbyResponse.getMessage();

        assertEquals(actualMessageResponse, "cannot do write operation with current handle");
    }

    @Test
    void process_fromCustom_file_writeLock_write_filecontent_empty() {
        //obtain shared lock on 'test' node
        String firstCommandString = "open /ls/local/process_fromCustom_file_writeLock_write_filecontent_empty.txt write permanent";
        ChubbyRequest firstChubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, firstCommandString);
        ChubbyResponse actualResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, firstChubbyRequest, this.client);

        //write
        String secondCommandString = "write filecontent";
        ChubbyRequest secondChubbyRequest = new ChubbyRequest("test_client", actualResponse, secondCommandString);
        ChubbyResponse chubbyResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, secondChubbyRequest, this.client);
        String actualMessageResponse = chubbyResponse.getMessage();

//        this.closeCommand(chubbyResponse);
        assertEquals(actualMessageResponse, "node content updated successfully");
    }

    @Test
    void process_fromCustom_file_writeLock_write_filecontent_blank() {
        //obtain shared lock on 'test' node
        String firstCommandString = "open /ls/local/process_fromCustom_file_writeLock_write_filecontent_blank.txt write permanent";
        ChubbyRequest firstChubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, firstCommandString);
        ChubbyResponse actualResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, firstChubbyRequest, this.client);

        //write
        String secondCommandString = "write filecontent ";
        ChubbyRequest secondChubbyRequest = new ChubbyRequest("test_client", actualResponse, secondCommandString);
        ChubbyResponse chubbyResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, secondChubbyRequest, this.client);
        String actualMessageResponse = chubbyResponse.getMessage();

//        this.closeCommand(chubbyResponse);
        assertEquals(actualMessageResponse, "node content updated successfully");
    }

    @Test
    void process_fromCustom_file_writeLock_write_filecontent_null() {
        //obtain shared lock on 'test' node
        String firstCommandString = "open /ls/local/process_fromCustom_file_writeLock_write_filecontent_null.txt write permanent";
        ChubbyRequest firstChubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, firstCommandString);
        ChubbyResponse actualResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, firstChubbyRequest, this.client);

        //write
        String secondCommandString = "write filecontent null";
        ChubbyRequest secondChubbyRequest = new ChubbyRequest("test_client", actualResponse, secondCommandString);
        ChubbyResponse chubbyResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, secondChubbyRequest, this.client);
        String actualMessageResponse = chubbyResponse.getMessage();

//        this.closeCommand(chubbyResponse);
        assertEquals(actualMessageResponse, "node content updated successfully");
    }

    @Test
    void process_fromCustom_file_writeLock_write_filecontent_custom() {
        //obtain shared lock on 'test' node
        String firstCommandString = "open /ls/local/process_fromCustom_file_writeLock_write_filecontent_custom.txt write permanent";
        ChubbyRequest firstChubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, firstCommandString);
        ChubbyResponse actualResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, firstChubbyRequest, this.client);

        //write
        String secondCommandString = "write filecontent hello world!";
        ChubbyRequest secondChubbyRequest = new ChubbyRequest("test_client", actualResponse, secondCommandString);
        ChubbyResponse chubbyResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, secondChubbyRequest, this.client);
        String actualMessageResponse = chubbyResponse.getMessage();

//        this.closeCommand(chubbyResponse);
        assertEquals(actualMessageResponse, "node content updated successfully");
    }

    @Test
    void process_fromCustom_file_readLock_write_changeAcl_custom() {
        //obtain shared lock on 'test' node
        String firstCommandString = "open /ls/local/process_fromCustom_file_readLock_write_changeAcl_custom.txt read permanent";
        ChubbyRequest firstChubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, firstCommandString);
        ChubbyResponse actualResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, firstChubbyRequest, this.client);

        //write
        String secondCommandString = "write acl read newReadACLName!";
        ChubbyRequest secondChubbyRequest = new ChubbyRequest("test_client", actualResponse, secondCommandString);
        ChubbyError chubbyResponse = (ChubbyError) this.chubbyRequestProcessor.process(this.chubbyNamespace, secondChubbyRequest, this.client);
        String actualMessageResponse = chubbyResponse.getMessage();

        assertEquals("it's not possible to change acl names of current node with 'READ' handle type, acquire 'CHANGE_ACL' handle type before proceeding", actualMessageResponse);
    }

    @Test
    void process_fromCustom_file_writeLock_write_changeAcl_custom() {
        //obtain shared lock on 'test' node
        String firstCommandString = "open /ls/local/process_fromCustom_file_writeLock_write_changeAcl_custom.txt write permanent";
        ChubbyRequest firstChubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, firstCommandString);
        ChubbyResponse actualResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, firstChubbyRequest, this.client);

        //write
        String secondCommandString = "write acl read newReadACLName!";
        ChubbyRequest secondChubbyRequest = new ChubbyRequest("test_client", actualResponse, secondCommandString);
        ChubbyError chubbyResponse = (ChubbyError) this.chubbyRequestProcessor.process(this.chubbyNamespace, secondChubbyRequest, this.client);
        String actualMessageResponse = chubbyResponse.getMessage();

        assertEquals("it's not possible to change acl names of current node with 'WRITE' handle type, acquire 'CHANGE_ACL' handle type before proceeding", actualMessageResponse);
    }

    @Test
    void process_fromCustom_file_changeAclLock_write_changeAcl_read_custom() {
        //obtain shared lock on 'test' node
        String firstCommandString = "open /ls/local/process_fromCustom_file_changeAclLock_write_changeAcl_read_custom.txt change_acl permanent";
        ChubbyRequest firstChubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, firstCommandString);
        ChubbyResponse actualResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, firstChubbyRequest, this.client);

        //write
        String secondCommandString = "write acl read process_fromCustom_file_changeAclLock_write_changeAcl_read_custom";
        ChubbyRequest secondChubbyRequest = new ChubbyRequest("test_client", actualResponse, secondCommandString);
        ChubbyMessage chubbyResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, secondChubbyRequest, this.client);
        String actualMessageResponse = chubbyResponse.getMessage();

        assertEquals("node content updated successfully", actualMessageResponse);
    }

    @Test
    void process_fromCustom_file_changeAclLock_write_changeAcl_write_custom() {
        //obtain shared lock on 'test' node
        String firstCommandString = "open /ls/local/process_fromCustom_file_changeAclLock_write_changeAcl_write_custom.txt change_acl permanent";
        ChubbyRequest firstChubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, firstCommandString);
        ChubbyResponse actualResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, firstChubbyRequest, this.client);

        //write
        String secondCommandString = "write acl write process_fromCustom_file_changeAclLock_write_changeAcl_write_custom";
        ChubbyRequest secondChubbyRequest = new ChubbyRequest("test_client", actualResponse, secondCommandString);
        ChubbyResponse chubbyResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, secondChubbyRequest, this.client);
        String actualMessageResponse = chubbyResponse.getMessage();

        assertEquals("node content updated successfully", actualMessageResponse);
    }

    @Test
    void process_fromCustom_file_changeAclLock_write_changeAcl_changeAcl_custom() {
        //obtain shared lock on 'test' node
        String firstCommandString = "open /ls/local/process_fromCustom_file_changeAclLock_write_changeAcl_changeAcl_custom000.txt change_acl permanent";
        ChubbyRequest firstChubbyRequest = new ChubbyRequest("test_client000", this.rootChubbyResponse, firstCommandString);
        ChubbyResponse actualResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, firstChubbyRequest, this.client);

        //write
        String secondCommandString = "write acl change_acl process_fromCustom_file_changeAclLock_write_changeAcl_changeAcl_custom000";
        ChubbyRequest secondChubbyRequest = new ChubbyRequest("test_client000", actualResponse, secondCommandString);
        ChubbyResponse chubbyResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, secondChubbyRequest, this.client);
        String actualMessageResponse = chubbyResponse.getMessage();

        assertEquals("node content updated successfully", actualMessageResponse);
    }

    @Test
    void process_fromCustom_file_changeAclLock_write_changeAcl_changeAcl_uppercaseLowercase_custom() {
        //obtain shared lock on 'test' node
        String firstCommandString = "open /ls/local/process_fromCustom_file_changeAclLock_write_changeAcl_changeAcl_custom.txt change_acl permanent";
        ChubbyRequest firstChubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, firstCommandString);
        ChubbyResponse actualResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, firstChubbyRequest, this.client);

        //write
        String secondCommandString = "write AcL change_acl process_fromCustom_file_changeAclLock_write_changeAcl_changeAcl_custom";
        ChubbyRequest secondChubbyRequest = new ChubbyRequest("test_client", actualResponse, secondCommandString);
        ChubbyError chubbyResponse = (ChubbyError) this.chubbyRequestProcessor.process(this.chubbyNamespace, secondChubbyRequest, this.client);
        String actualMessageResponse = chubbyResponse.getMessage();

        assertEquals("wrong argument, expected 'filecontent' or 'acl'", actualMessageResponse);
    }

    @Test
    void process_fromCustom_file_changeAclLock_write_changeAcl_read_notEnoughArguments_custom() {
        //obtain shared lock on 'test' node
        String firstCommandString = "open /ls/local/process_fromCustom_file_changeAclLock_write_changeAcl_read_notEnoughArguments_custom0.txt change_acl permanent";
        ChubbyRequest firstChubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, firstCommandString);
        ChubbyResponse actualResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, firstChubbyRequest, this.client);

        //write
        String secondCommandString = "write acl read";
        ChubbyRequest secondChubbyRequest = new ChubbyRequest("test_client", actualResponse, secondCommandString);
        ChubbyError chubbyResponse = (ChubbyError) this.chubbyRequestProcessor.process(this.chubbyNamespace, secondChubbyRequest, this.client);
        String actualMessageResponse = chubbyResponse.getMessage();

        assertEquals("expected exactly 3 arguments for 'write acl' command, syntax is: 'write acl *aclType* *newCustomName*", actualMessageResponse);
    }

    @Test
    void process_fromCustom_file_changeAclLock_write_changeAcl_read_tooManyArguments_custom() {
        //obtain shared lock on 'test' node
        String firstCommandString = "open /ls/local/process_fromCustom_file_changeAclLock_write_changeAcl_read_tooManyArguments_custom0.txt change_acl permanent";
        ChubbyRequest firstChubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, firstCommandString);
        ChubbyResponse actualResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, firstChubbyRequest, this.client);

        //write
        String secondCommandString = "write acl read process_fromCustom_file_changeAclLock_write_changeAcl_read_tooManyArguments_custom0 other arguments";
        ChubbyRequest secondChubbyRequest = new ChubbyRequest("test_client", actualResponse, secondCommandString);
        ChubbyError chubbyResponse = (ChubbyError) this.chubbyRequestProcessor.process(this.chubbyNamespace, secondChubbyRequest, this.client);
        String actualMessageResponse = chubbyResponse.getMessage();

        assertEquals("expected exactly 3 arguments for 'write acl' command, syntax is: 'write acl *aclType* *newCustomName*", actualMessageResponse);
    }

    @Test
    void process_fromCustom_file_changeAclLock_write_addOneClientAcl_read_fromDefaultAclName_custom() {
        //obtain shared lock on 'test' node
        String firstCommandString = "open /ls/local/process_fromCustom_file_changeAclLock_write_addOneClientAcl_read_fromDefaultAclName_custom change_acl permanent";
        ChubbyRequest firstChubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, firstCommandString);
        ChubbyResponse actualResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, firstChubbyRequest, this.client);

        //write
        String secondCommandString = "write add_client read test_client2";
        ChubbyRequest secondChubbyRequest = new ChubbyRequest("test_client", actualResponse, secondCommandString);
        ChubbyError chubbyResponse = (ChubbyError) this.chubbyRequestProcessor.process(this.chubbyNamespace, secondChubbyRequest, this.client);
        String actualMessageResponse = chubbyResponse.getMessage();

        assertEquals("java.lang.RuntimeException: chubby.utils.exceptions.ChubbyACLException: cannot add client to default acl node name", actualMessageResponse);
    }

    @Test
    void process_fromCustom_file_changeAclLock_write_addEmptyClient_read_fromModifiedAclName_custom() {
        //obtain shared lock on 'test' node
        String firstCommandString = "open /ls/local/process_fromCustom_file_changeAclLock_write_addEmptyClient_read_fromModifiedAclName_custom00 change_acl permanent";
        ChubbyRequest firstChubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, firstCommandString);
        ChubbyResponse firstActualResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, firstChubbyRequest, this.client);

        //write new acl read name
        String secondCommandString = "write acl read read_nname";
        ChubbyRequest secondChubbyRequest = new ChubbyRequest("test_client", firstActualResponse, secondCommandString);
        ChubbyResponse secondActualResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, secondChubbyRequest, this.client);

        //write
        String thirdCommandString = "write add_client read";
        ChubbyRequest thirdChubbyRequest = new ChubbyRequest("test_client", secondActualResponse, thirdCommandString);
        ChubbyError chubbyResponse = (ChubbyError) this.chubbyRequestProcessor.process(this.chubbyNamespace, thirdChubbyRequest, this.client);
        String actualMessageResponse = chubbyResponse.getMessage();

        assertEquals("expected >= 3 arguments for 'write acl' command, syntax is: 'write add_client *aclType* *clientName1 clientName2 ...*", actualMessageResponse);
    }

    @Test
    void process_fromCustom_file_changeAclLock_write_addBlankClient_read_fromModifiedAclName_custom() {
        //obtain shared lock on 'test' node
        String firstCommandString = "open /ls/local/process_fromCustom_file_changeAclLock_write_addBlankClient_read_fromModifiedAclName_custom change_acl permanent";
        ChubbyRequest firstChubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, firstCommandString);
        ChubbyResponse firstActualResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, firstChubbyRequest, this.client);

        //write new acl read name
        String secondCommandString = "write acl read rrr_name";
        ChubbyRequest secondChubbyRequest = new ChubbyRequest("test_client", firstActualResponse, secondCommandString);
        ChubbyResponse secondActualResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, secondChubbyRequest, this.client);

        //write
        String thirdCommandString = "write add_client read ";
        ChubbyRequest thirdChubbyRequest = new ChubbyRequest("test_client", secondActualResponse, thirdCommandString);
        ChubbyError chubbyResponse = (ChubbyError) this.chubbyRequestProcessor.process(this.chubbyNamespace, thirdChubbyRequest, this.client);
        String actualMessageResponse = chubbyResponse.getMessage();

        assertEquals("expected >= 3 arguments for 'write acl' command, syntax is: 'write add_client *aclType* *clientName1 clientName2 ...*", actualMessageResponse);
    }

    @Test
    void process_fromCustom_file_changeAclLock_write_addNullClient_read_fromModifiedAclName_custom() {
        //obtain shared lock on 'test' node
        String firstCommandString = "open /ls/local/process_fromCustom_file_changeAclLock_write_addNullClient_read_fromModifiedAclName_custom0 change_acl permanent";
        ChubbyRequest firstChubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, firstCommandString);
        ChubbyResponse firstActualResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, firstChubbyRequest, this.client);

        //write new acl read name
        String secondCommandString = "write acl read nnn_name";
        ChubbyRequest secondChubbyRequest = new ChubbyRequest("test_client", firstActualResponse, secondCommandString);
        ChubbyResponse secondActualResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, secondChubbyRequest, this.client);

        //write
        String thirdCommandString = "write add_client read null";    //this 'null' will be interpreted as a client's name
        ChubbyRequest thirdChubbyRequest = new ChubbyRequest("test_client", secondActualResponse, thirdCommandString);
        ChubbyMessage chubbyResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, thirdChubbyRequest, this.client);
        String actualMessageResponse = chubbyResponse.getMessage();

        assertEquals("client added successfully", actualMessageResponse);
    }

    @Test
    void process_fromCustom_file_changeAclLock_write_addOneClientAcl_read_fromModifiedAclName_custom() {
        //obtain shared lock on 'test' node
        String firstCommandString = "open /ls/local/process_fromCustom_file_changeAclLock_write_addOneClientAcl_read_fromModifiedAclName_custom0 change_acl permanent";
        ChubbyRequest firstChubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, firstCommandString);
        ChubbyResponse firstActualResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, firstChubbyRequest, this.client);

        //write new acl read name
        String secondCommandString = "write acl read newReadAclName";
        ChubbyRequest secondChubbyRequest = new ChubbyRequest("test_client", firstActualResponse, secondCommandString);
        ChubbyResponse secondActualResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, secondChubbyRequest, this.client);

        //write
        String thirdCommandString = "write add_client read test_client2";
        ChubbyRequest thirdChubbyRequest = new ChubbyRequest("test_client", secondActualResponse, thirdCommandString);
        ChubbyMessage chubbyResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, thirdChubbyRequest, this.client);
        String actualMessageResponse = chubbyResponse.getMessage();

        assertEquals("client added successfully", actualMessageResponse);
    }

    @Test
    void process_fromCustom_file_changeAclLock_write_addOneClientAcl_write_fromModifiedAclName_custom() {
        //obtain shared lock on 'test' node
        String firstCommandString = "open /ls/local/process_fromCustom_file_changeAclLock_write_addOneClientAcl_write_fromModifiedAclName_custom000 change_acl permanent";
        ChubbyRequest firstChubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, firstCommandString);
        ChubbyResponse firstActualResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, firstChubbyRequest, this.client);

        //write new acl read name
        String secondCommandString = "write acl write newWriteName";
        ChubbyRequest secondChubbyRequest = new ChubbyRequest("test_client", firstActualResponse, secondCommandString);
        ChubbyResponse secondActualResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, secondChubbyRequest, this.client);

        //write
        String thirdCommandString = "write add_client write test_client2";
        ChubbyRequest thirdChubbyRequest = new ChubbyRequest("test_client", secondActualResponse, thirdCommandString);
        ChubbyMessage chubbyResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, thirdChubbyRequest, this.client);
        String actualMessageResponse = chubbyResponse.getMessage();

        assertEquals("client added successfully", actualMessageResponse);
    }

    @Test
    void process_fromCustom_file_changeAclLock_write_addOneClientAcl_changeAcl_fromModifiedAclName_custom() {
        //obtain shared lock on 'test' node
        String firstCommandString = "open /ls/local/process_fromCustom_file_changeAclLock_write_addOneClientAcl_changeAcl_fromModifiedAclName_custom change_acl permanent";
        ChubbyRequest firstChubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, firstCommandString);
        ChubbyResponse firstActualResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, firstChubbyRequest, this.client);

        //write new acl read name
        String secondCommandString = "write acl change_acl newChangeAclName";
        ChubbyRequest secondChubbyRequest = new ChubbyRequest("test_client", firstActualResponse, secondCommandString);
        ChubbyResponse secondActualResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, secondChubbyRequest, this.client);

        //write
        String thirdCommandString = "write add_client change_acl test_client2";
        ChubbyRequest thirdChubbyRequest = new ChubbyRequest("test_client", secondActualResponse, thirdCommandString);
        ChubbyMessage chubbyResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, thirdChubbyRequest, this.client);
        String actualMessageResponse = chubbyResponse.getMessage();

        assertEquals("client added successfully", actualMessageResponse);
    }

    @Test
    void process_fromCustom_file_changeAclLock_write_addMultipleClientAcl_read_fromModifiedAclName_custom() {
        //obtain shared lock on 'test' node
        String firstCommandString = "open /ls/local/process_fromCustom_file_changeAclLock_write_addMultipleClientAcl_read_fromModifiedAclName_custom change_acl permanent";
        ChubbyRequest firstChubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, firstCommandString);
        ChubbyResponse firstActualResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, firstChubbyRequest, this.client);

        //write new acl read name
        String secondCommandString = "write acl read readAclNameTest";
        ChubbyRequest secondChubbyRequest = new ChubbyRequest("test_client", firstActualResponse, secondCommandString);
        ChubbyResponse secondActualResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, secondChubbyRequest, this.client);

        //write
        String thirdCommandString = "write add_client read test_client2 test_client3 test_client4";
        ChubbyRequest thirdChubbyRequest = new ChubbyRequest("test_client", secondActualResponse, thirdCommandString);
        ChubbyMessage chubbyResponse = this.chubbyRequestProcessor.process(this.chubbyNamespace, thirdChubbyRequest, this.client);
        String actualMessageResponse = chubbyResponse.getMessage();

        assertEquals("client added successfully", actualMessageResponse);
    }

    @Test
    void process_fromCustom_file_changeAclLock_write_addMultipleClientAcl_write_fromModifiedAclName_custom() {
        //obtain shared lock on 'test' node
        String firstCommandString = "open /ls/local/process_fromCustom_file_changeAclLock_write_addMultipleClientAcl_write_fromModifiedAclName_custom change_acl permanent";
        ChubbyRequest firstChubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, firstCommandString);
        ChubbyResponse firstActualResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, firstChubbyRequest, this.client);

        //write new acl read name
        String secondCommandString = "write acl write writeAclNameTest";
        ChubbyRequest secondChubbyRequest = new ChubbyRequest("test_client", firstActualResponse, secondCommandString);
        ChubbyResponse secondActualResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, secondChubbyRequest, this.client);

        //write
        String thirdCommandString = "write add_client write test_client2 test_client3 test_client4";
        ChubbyRequest thirdChubbyRequest = new ChubbyRequest("test_client", secondActualResponse, thirdCommandString);
        ChubbyResponse chubbyResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, thirdChubbyRequest, this.client);
        String actualMessageResponse = chubbyResponse.getMessage();

        assertEquals("client added successfully", actualMessageResponse);
    }

    @Test
    void process_fromCustom_file_changeAclLock_write_addMultipleClientAcl_changeAcl_fromModifiedAclName_custom() {
        //obtain shared lock on 'test' node
        String firstCommandString = "open /ls/local/process_fromCustom_file_changeAclLock_write_addMultipleClientAcl_changeAcl_fromModifiedAclName_custom change_acl permanent";
        ChubbyRequest firstChubbyRequest = new ChubbyRequest("test_client", this.rootChubbyResponse, firstCommandString);
        ChubbyResponse firstActualResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, firstChubbyRequest, this.client);

        //write new acl read name
        String secondCommandString = "write acl change_acl changeAclNameTest";
        ChubbyRequest secondChubbyRequest = new ChubbyRequest("test_client", firstActualResponse, secondCommandString);
        ChubbyResponse secondActualResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, secondChubbyRequest, this.client);

        //write
        String thirdCommandString = "write add_client change_acl test_client2 test_client3 test_client4";
        ChubbyRequest thirdChubbyRequest = new ChubbyRequest("test_client", secondActualResponse, thirdCommandString);
        ChubbyResponse chubbyResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, thirdChubbyRequest, this.client);
        String actualMessageResponse = chubbyResponse.getMessage();

        assertEquals("client added successfully", actualMessageResponse);
    }

    @Test
    void process_fromCustom_file_changeAclLock_write_accessFileWithoutACLPermission() {
        //obtain shared lock on 'test' node
        String firstCommandString = "open /ls/local/kkk change_acl permanent";
        ChubbyRequest firstChubbyRequest = new ChubbyRequest("test_client1", this.rootChubbyResponse, firstCommandString);
        ChubbyResponse firstActualResponse = (ChubbyResponse) this.chubbyRequestProcessor.process(this.chubbyNamespace, firstChubbyRequest, this.client);

        //write new acl read name
        String secondCommandString = "write acl read ggg";
        ChubbyRequest secondChubbyRequest = new ChubbyRequest("test_client1", firstActualResponse, secondCommandString);
        this.chubbyRequestProcessor.process(this.chubbyNamespace, secondChubbyRequest, this.client);

        //CLIENT 2
        //try to open node with write permission
        String thirdCommandString = "open /ls/local/kkk read permanent";
        ChubbyRequest thirdChubbyRequest = new ChubbyRequest("test_client2", this.rootChubbyResponse, thirdCommandString);
        ChubbyError chubbyResponse = (ChubbyError) this.chubbyRequestProcessor.process(this.chubbyNamespace, thirdChubbyRequest, this.client);
        String actualMessageResponse = chubbyResponse.getMessage();

        assertEquals("user 'test_client2' not permitted to 'read' on node '\\ls\\local\\kkk'", actualMessageResponse);
    }

}
