package chubby.server.node;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ChubbyNodeValue {
    @SerializedName("file_content")
    private String filecontent;
    @SerializedName("metadata")
    private ChubbyNodeMetadata metadata;

    /**
     * Create a new ChubbyNodeValue.
     *
     * @param filecontent  the file content of the node (it will be automatically assigned an empty string if the node
     *                     is a directory, ignoring this parameter in that case)
     * @param metadata  the metadata of the node
     */
    public ChubbyNodeValue(@Nullable String filecontent, @NotNull ChubbyNodeMetadata metadata) {
        if (metadata.getChubbyNodeType().equals(ChubbyNodeType.DIRECTORY)) {
            this.filecontent = "";
        } else {
            this.filecontent = Objects.requireNonNullElse(filecontent, "");
        }
        this.metadata = metadata;
    }

    /**
     * Create a new ChubbyNodeValue.
     *
     * @param metadata  the metadata of the node
     */
    public ChubbyNodeValue(@NotNull ChubbyNodeMetadata metadata) {
        this.filecontent = "";
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        return "ChubbyNodeValue {" +
                "\n\tfilecontent = '" + this.filecontent + '\'' +
                ", \n\tmetadata = " + this.metadata.toString() +
                "\n}";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        ChubbyNodeValue that = (ChubbyNodeValue) obj;
        return Objects.equals(this.filecontent, that.filecontent) &&
                Objects.equals(this.metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.filecontent, this.metadata);
    }

    public void setFilecontent(String filecontent) {
        this.filecontent = filecontent;
        this.metadata.increaseContentGenerationNumber(filecontent);
    }

    public String getFilecontent() {
        return this.filecontent;
    }

    public ChubbyNodeMetadata getMetadata() {
        return this.metadata;
    }
}
