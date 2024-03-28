package chubby.control.message;

import chubby.control.handle.ChubbyHandleType;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Paths;
import java.util.Arrays;

//chubby req == chubby library
public class ChubbyRequest extends ChubbyMessage {
    @SerializedName("username")
    private final String username;
    @SerializedName("chubby_current_handle_type")
    private final ChubbyHandleType chubbyCurrentHandleType;
    @SerializedName("lock_id")
    private final String lockId;
    @SerializedName("lease_id")
    private final String leaseId;
    @SerializedName("command")
    private final String command;
    @SerializedName("args")
    private final String[] args;
    @SerializedName("filecontent")
    private String fileContent;

    /**
     * Create a new ChubbyRequest.
     *
     * @param username  the username of the client that will receive the response
     * @param chubbyResp  the response that will be sent
     * @param messageStr  the message to be sent
     */
    public ChubbyRequest(String username, @NotNull ChubbyResponse chubbyResp, @NotNull String messageStr) {
        super(Paths.get(chubbyResp.getHandleAbsolutePath()), messageStr);
        this.username = username;
        this.chubbyCurrentHandleType = chubbyResp.getChubbyCurrentHandleResponse().getChubbyHandleType();
        this.lockId = chubbyResp.getChubbyCurrentHandleResponse().getLockId();
        this.leaseId = chubbyResp.getChubbyCurrentHandleResponse().getLeaseId();
        this.fileContent = chubbyResp.getChubbyCurrentHandleResponse().getFileContent();

        //splits message by blank spaces or by " pairs
        String[] words = messageStr.split("\\s+(?=([^\"]*\"[^\"]*\")*[^\"]*$)");

        if (words.length == 0) {
            this.command = "";
            this.args = new String[]{""};
        } else {
            this.command = words[0];
            this.args = Arrays.copyOfRange(words, 1, words.length);
        }

    }

    @Override
    public String toString() {
        return "ChubbyRequest{" +
                "username='" + this.username + '\'' +
                ", absolutePath='" + this.handleAbsolutePath + '\'' +
                ", command='" + this.command + '\'' +
                ", args=" + Arrays.toString(this.args) +
                "}\n";
    }

    @Override
    public String getFormattedMessage() {
        return "[" + this.getLocalDate() + " " + this.getLocalTime() + "] " + this.username + "-request:(" + this.chubbyCurrentHandleType.toString().toLowerCase() +")" + this.handleAbsolutePath + "< " + this.message;
    }

    public String getUsername() {
        return this.username;
    }

    public ChubbyHandleType getChubbyCurrentHandleType() {
        return this.chubbyCurrentHandleType;
    }

    public String getLockId() {
        return this.lockId;
    }

    public String getLeaseId() {
        return this.leaseId;
    }

    public String getCommand() {
        return this.command;
    }

    public String[] getArgs() {
        return this.args.clone();
    }

    public String getFileContent() {
        return this.fileContent;
    }

    public void setFileContent(String fileContent) {
        this.fileContent = fileContent;
    }
}
