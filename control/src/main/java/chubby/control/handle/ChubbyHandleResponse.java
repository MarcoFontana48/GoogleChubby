package chubby.control.handle;

import chubby.control.message.ChubbyRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Objects;

public class ChubbyHandleResponse {
    private final String responseHandleAbsolutePath;
    private final ChubbyHandleType chubbyHandleType;
    private final String lockId;
    private final String leaseId;
    private String fileContent = "";

    /**
     * Create a new ChubbyHandleResponse.
     *
     * @param requestedAbsolutePath  the absolute path of the handle
     * @param chubbyHandleType  the type of the handle
     * @param lockId  the lock id
     * @param leaseId  the lease id
     */
    public ChubbyHandleResponse(@NotNull Path requestedAbsolutePath, ChubbyHandleType chubbyHandleType, @Nullable String lockId, @Nullable String leaseId) {
        this.responseHandleAbsolutePath = requestedAbsolutePath.toString();
        this.chubbyHandleType = chubbyHandleType;
        this.leaseId = Objects.requireNonNullElse(leaseId, "");
        this.lockId = Objects.requireNonNullElse(lockId, "");
    }

    /**
     * Create a new ChubbyHandleResponse.
     *
     * @param chubbyRequest  the request that will be sent
     */
    public ChubbyHandleResponse(@NotNull ChubbyRequest chubbyRequest) {
        this.responseHandleAbsolutePath = chubbyRequest.getHandleAbsolutePath();
        this.chubbyHandleType = chubbyRequest.getChubbyCurrentHandleType();
        this.leaseId = Objects.requireNonNullElse(chubbyRequest.getLeaseId(), "");
        this.lockId = Objects.requireNonNullElse(chubbyRequest.getLockId(), "");
        this.fileContent = Objects.requireNonNullElse(chubbyRequest.getFileContent(), "");
    }

    public String getResponseHandleAbsolutePath() {
        return this.responseHandleAbsolutePath;
    }

    public ChubbyHandleType getChubbyHandleType() {
        return this.chubbyHandleType;
    }

    public String getLockId() {
        return this.lockId;
    }

    public String getLeaseId() {
        return this.leaseId;
    }

    public void setFileContent(String fileContent) {
        this.fileContent = fileContent;
    }

    public String getFileContent() {
        return this.fileContent;
    }
}
