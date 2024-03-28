package chubby.control.message;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public class ChubbyError extends ChubbyMessage {
    @SerializedName("error_absolute_path")
    private final String errorAbsolutePath;

    /**
     * Create a new ChubbyError.
     *
     * @param handleAbsolutePath  the absolute path of the handle
     * @param errorAbsolutePath  the absolute path of the error
     * @param message  the message to be sent
     */
    public ChubbyError(Path handleAbsolutePath, @NotNull Path errorAbsolutePath, @Nullable String message) {
        super(handleAbsolutePath, message);
        this.errorAbsolutePath = errorAbsolutePath.toString();
    }

    /**
     * Create a new ChubbyError.
     *
     * @param chubbyRequest  the request that will be sent
     * @param message  the message to be sent
     */
    public ChubbyError(@NotNull ChubbyRequest chubbyRequest, @Nullable String message) {
        super(Path.of(chubbyRequest.getHandleAbsolutePath()), message);
        this.errorAbsolutePath = chubbyRequest.getHandleAbsolutePath();
    }

    @Override
    public String getFormattedMessage() {
        return "[" + this.getLocalDate() + " " + this.getLocalTime() + "] chubby-error:" + this.errorAbsolutePath + "> " + this.message + "\n";
    }

    public String getErrorAbsolutePath() {
        return this.errorAbsolutePath;
    }
}
