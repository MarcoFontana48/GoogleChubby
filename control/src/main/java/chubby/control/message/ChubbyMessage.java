package chubby.control.message;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public abstract class ChubbyMessage {
    @SerializedName("handle_absolute_path")
    protected final String handleAbsolutePath;
    @SerializedName("message")
    protected final String message;
    @SerializedName("local_date")
    private final LocalDate localDate;
    @SerializedName("local_time")
    private final LocalTime localTime;

    /**
     * Create a new ChubbyMessage.
     *
     * @param handleAbsolutePath  the absolute path of the handle
     * @param message  the message to be sent
     */
    public ChubbyMessage(Path handleAbsolutePath, @Nullable String message) {
        this.handleAbsolutePath = Objects.requireNonNullElse(handleAbsolutePath, "").toString();
        this.message = Objects.requireNonNullElse(message, "");
        this.localDate = LocalDate.now();
        this.localTime = LocalTime.now().truncatedTo(ChronoUnit.SECONDS);
    }

    public String getFormattedMessage() {
        return "[" + this.getLocalDate() + " " + this.getLocalTime() + "] chubby-message:" + this.handleAbsolutePath + "> " + this.message + "\n";
    }

    public String getHandleAbsolutePath() {
        return this.handleAbsolutePath;
    }

    public String getMessage() {
        return this.message;
    }

    public LocalDate getLocalDate() {
        return this.localDate;
    }

    public LocalTime getLocalTime() {
        return this.localTime;
    }
}