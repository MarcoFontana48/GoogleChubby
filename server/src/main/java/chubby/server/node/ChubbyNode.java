package chubby.server.node;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public class ChubbyNode {
    private final Path absolutePath;
    private final ChubbyNodeValue chubbyNodeValue;

    /**
     * Create a new ChubbyNode.
     *
     * @param absolutePath  the absolute path of the node that will be store into etcd kv store as key
     * @param fileContent  the file content of the node (it will be automatically assigned an empty string if the node
     *                     is a directory, ignoring this parameter in that case)
     * @param chubbyNodeAttribute  the attribute of the node
     */
    public ChubbyNode(@NotNull Path absolutePath, @Nullable String fileContent, ChubbyNodeAttribute chubbyNodeAttribute) {
        this.absolutePath = absolutePath.normalize();

        ChubbyNodeMetadata chubbyNodeMetadata = new ChubbyNodeMetadata(this.absolutePath, fileContent, chubbyNodeAttribute);
        this.chubbyNodeValue = new ChubbyNodeValue(fileContent, chubbyNodeMetadata);
    }

    /**
     * Create a new ChubbyNode.
     *
     * @param absolutePath  the absolute path of the node that will be store into etcd kv store as key
     * @param chubbyNodeValue  the value of the node that will be stored into etcd kv store as value
     */
    public ChubbyNode(@NotNull Path absolutePath, @NotNull ChubbyNodeValue chubbyNodeValue) {
        this.absolutePath = absolutePath.normalize();
        this.chubbyNodeValue = chubbyNodeValue;
    }

    @Override
    public String toString() {
        return "ChubbyNode{" +
                "absolutePath=" + this.absolutePath +
                ", chubbyNodeValue=" + this.chubbyNodeValue.toString() +
                '}';
    }

    public Path getAbsolutePath() {
        return this.absolutePath;
    }

    public ChubbyNodeValue getNodeValue() {
        return this.chubbyNodeValue;
    }
}
