package chubby.control.message;

import chubby.control.handle.ChubbyHandleResponse;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public class ChubbyResponse extends ChubbyMessage {
    @SerializedName("username")
    private final String username;
    @SerializedName("chubby_current_handle_response")
    private final ChubbyHandleResponse chubbyCurrentHandleResponse;

    /**
     * Create a new ChubbyResponse.
     *
     * @param username  the username of the client that will receive the response
     * @param message  the message to be sent
     * @param chubbyHandleResponse  the handle response that will be sent
     */
    public ChubbyResponse(String username, @Nullable String message, @NotNull ChubbyHandleResponse chubbyHandleResponse) {
        super(Path.of(chubbyHandleResponse.getResponseHandleAbsolutePath()), message);
        this.username = username;
        this.chubbyCurrentHandleResponse = chubbyHandleResponse;
    }

    @Override
    public String getFormattedMessage() {
        return "[" + this.getLocalDate() + " " + this.getLocalTime() + "] chubby-response:(" + this.chubbyCurrentHandleResponse.getChubbyHandleType().toString().toLowerCase() +")" + this.handleAbsolutePath + "> " + this.message + "\n";
    }

    public ChubbyHandleResponse getChubbyCurrentHandleResponse() {
        return this.chubbyCurrentHandleResponse;
    }

    public String getUsername() {
        return this.username;
    }
}
