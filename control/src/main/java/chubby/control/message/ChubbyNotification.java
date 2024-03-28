package chubby.control.message;

import com.google.gson.annotations.SerializedName;
import io.etcd.jetcd.ByteSequence;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public class ChubbyNotification extends ChubbyMessage {
    @SerializedName("requested_absolute_path")
    private final String requestedAbsolutePath;

    /**
     * Create a new ChubbyNotification.
     *
     * @param handleAbsolutePath  the absolute path of the handle
     * @param requestedAbsolutePath  the absolute path of the requested file
     * @param message  the message to be sent
     */
    public ChubbyNotification(Path handleAbsolutePath, @Nullable ByteSequence requestedAbsolutePath, @Nullable String message) {
        super(handleAbsolutePath, message);
        this.requestedAbsolutePath = (requestedAbsolutePath != null) ? requestedAbsolutePath.toString() : "";
    }

    @Override
    public String getFormattedMessage() {
        return "[" + this.getLocalDate() + " " + this.getLocalTime() + "] chubby-notification:" + this.requestedAbsolutePath + "> " + this.message + "\n";
    }

    public String getRequestedAbsolutePath() {
        return this.requestedAbsolutePath;
    }
}
