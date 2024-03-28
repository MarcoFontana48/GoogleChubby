package chubby.server.node;

import chubby.utils.ChubbyUtils;
import chubby.control.handle.ChubbyHandleType;
import com.google.gson.annotations.SerializedName;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ChubbyNodeMetadata {
    @SerializedName("checksum")
    private long checksum;                                  //checksum associated with this node's file content
    @SerializedName("instance_number")
    private long instanceNumber;                            //increases by 1 for each parent node with the same name
    @SerializedName("content_generation_number")
    private long contentGenerationNumber;                   //increases by 1 each time the file content is modified
    @SerializedName("lock_generation_number")
    private long lockGenerationNumber;                      //increases by 1 each time a client gets a lock on this node if the node was free
    @SerializedName("lock_request_number")
    private long lockRequestNumber;                         //increases by 1 each time a client tries to exclusively lock this node
    @SerializedName("lock_client_map")
    private Map<String, ChubbyHandleType> lockClientMap;    //keeps track of the clients that have a lock (any type) on this node
    @SerializedName("acl_generation_number")
    private long aclGenerationNumber;                       //increases by 1 each time the acl is modified
    @SerializedName("child_node_number")
    private int childNodeNumber;                            //increases by 1 each time a child node is added
    @SerializedName("acl_names")
    private Map<ChubbyHandleType, String> aclNamesMap;
    @SerializedName("node_type")
    private final ChubbyNodeType chubbyNodeType;
    @SerializedName("node_attribute")
    private final ChubbyNodeAttribute chubbyNodeAttribute;

    /**
     * Create a new ChubbyNodeMetadata.
     *
     * @param absolutePath  the absolute path of the node
     * @param fileContent  the file content of the node (empty string if the node is a directory)
     * @param chubbyNodeAttribute  the attribute of the node
     */
    public ChubbyNodeMetadata(@NotNull Path absolutePath, @Nullable String fileContent, ChubbyNodeAttribute chubbyNodeAttribute) {
        Logger logger = LogManager.getLogger();

        logger.trace("requested metadata creation, arguments: 'absolutePath:{}', 'fileContent:{}', 'chubbyNodeAttribute:{}'", absolutePath, fileContent, chubbyNodeAttribute);

        this.updateInstanceNumber(absolutePath);

        this.contentGenerationNumber = Long.MIN_VALUE;
        logger.trace("added metadata: 'content_generation_number:{}'", this.contentGenerationNumber);

        this.lockGenerationNumber = Long.MIN_VALUE;
        logger.trace("added metadata: 'lock_generation_number:{}'", this.lockGenerationNumber);

        this.lockRequestNumber = Long.MIN_VALUE;
        logger.trace("added metadata: 'lock_request_number:{}'", this.lockRequestNumber);

        this.lockClientMap = new HashMap<>();
        logger.trace("added metadata: 'lock_client_map:{}'", this.lockRequestNumber);

        this.aclGenerationNumber = Long.MIN_VALUE;
        logger.trace("added metadata: 'acl_generation_number:{}'", this.aclGenerationNumber);

        this.childNodeNumber = 0;

        this.aclNamesMap = new HashMap<>();

        if (ChubbyUtils.isFile(absolutePath))  {
            this.chubbyNodeType = ChubbyNodeType.FILE;
        } else {
            this.chubbyNodeType = ChubbyNodeType.DIRECTORY;
        }
        logger.trace("added metadata: 'node_type:{}'", this.chubbyNodeType);

        if (this.chubbyNodeType.equals(ChubbyNodeType.DIRECTORY)) {
            this.checksum = "".hashCode();
        } else {
            this.checksum = Objects.requireNonNullElse(fileContent, "").hashCode();
        }
        logger.trace("added metadata: 'checksum:{}'", this.checksum);

        this.chubbyNodeAttribute = chubbyNodeAttribute;
        logger.trace("added metadata: 'node_attribute:{}'", this.chubbyNodeAttribute);
    }

    @Override
    public String toString() {
        return "ChubbyNodeMetadata {" +
                "\n\tchecksum = " + this.checksum +
                ", \n\tinstanceNumber = " + this.instanceNumber +
                ", \n\tcontentGenerationNumber = " + this.contentGenerationNumber +
                ", \n\tlockGenerationNumber = " + this.lockGenerationNumber +
                ", \n\tlockRequestNumber = " + this.lockRequestNumber +
                ", \n\tlockClientMap = " + this.lockClientMap +
                ", \n\taclGenerationNumber = " + this.aclGenerationNumber +
                ", \n\tchildNodeNumber = " + this.childNodeNumber +
                ", \n\taclNamesMap = " + this.aclNamesMap +
                ", \n\tchubbyNodeType = " + this.chubbyNodeType +
                ", \n\tchubbyNodeAttribute = " + this.chubbyNodeAttribute +
                "\n}";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || this.getClass() != obj.getClass()) return false;
        ChubbyNodeMetadata that = (ChubbyNodeMetadata) obj;
        return this.checksum == that.checksum &&
                this.instanceNumber == that.instanceNumber &&
                this.contentGenerationNumber == that.contentGenerationNumber &&
                this.lockGenerationNumber == that.lockGenerationNumber &&
                this.lockRequestNumber == that.lockRequestNumber &&
                Objects.equals(this.lockClientMap, that.lockClientMap) &&
                this.aclGenerationNumber == that.aclGenerationNumber &&
                this.childNodeNumber == that.childNodeNumber &&
                this.chubbyNodeType == that.chubbyNodeType &&
                this.chubbyNodeAttribute == that.chubbyNodeAttribute;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.checksum, this.instanceNumber, this.contentGenerationNumber, this.lockGenerationNumber, this.lockRequestNumber, this.lockClientMap, this.aclGenerationNumber, this.childNodeNumber, this.chubbyNodeType, this.chubbyNodeAttribute);
    }

    public void increaseContentGenerationNumber(String filecontent) {
        this.contentGenerationNumber++;
        this.updateChecksum(filecontent);
    }

    public void updateChecksum(String filecontent) {
        this.checksum = Objects.requireNonNullElse(filecontent, "").hashCode();
    }

    protected void updateInstanceNumber(@NotNull Path absolutePath) {
        Logger logger = LogManager.getLogger();

        logger.trace("requested instance number update, processing it...");

        //counts the occurrences of same-name directories to determine the value of 'instanceNumber'
        String farthestFromRoot;
        if (absolutePath.equals(Paths.get("\\"))) {
            farthestFromRoot = "\\";
        } else {
            farthestFromRoot = absolutePath.getFileName().toString();
        }

        long sameNameDirOccurrences = Arrays.stream(absolutePath.toString().split("\\\\"))
                .filter(s -> s.equals(farthestFromRoot))
                .count() - 1;

        if (sameNameDirOccurrences > 0) {
            this.instanceNumber = Long.MIN_VALUE + sameNameDirOccurrences;
            logger.trace("added metadata: 'instance_number:{}'", this.instanceNumber);
        } else {
            this.instanceNumber = Long.MIN_VALUE;
            logger.trace("added metadata: 'instance_number:{}'", this.instanceNumber);
        }
    }

    public void setChildNodeNumber(int num) {
        this.childNodeNumber = num;
    }

    public void increaseChildNodeNumberOnce() {
        this.childNodeNumber++;
    }

    public void increaseChildNodeNumberOf(int num) {
        this.childNodeNumber += num;
    }

    public void decreaseChildNodeNumberOnce() {
        this.childNodeNumber++;
    }

    public void decreaseChildNodeNumberOf(int num) {
        this.childNodeNumber -= num;
    }

    public void increaseLockGenerationNumber() {
        this.lockGenerationNumber++;
    }

    public void increaseLockRequestNumber() {
        this.lockRequestNumber++;
    }

    public void addClientLock(String username, ChubbyHandleType chubbyHandleType) {
        if (this.lockClientMap.isEmpty()) {
            this.lockGenerationNumber++;
        }
        this.lockClientMap.putIfAbsent(username, chubbyHandleType);
    }

    public boolean removeClientLock(String username, ChubbyHandleType chubbyHandleType) {
        return this.lockClientMap.remove(username, chubbyHandleType);
    }

    public int getLockClientMapSize() {
        return this.lockClientMap.size();
    }

    public boolean isNodeFree() {
        return this.lockClientMap.isEmpty();
    }

    public long getChecksum() {
        return this.checksum;
    }

    public long getInstanceNumber() {
        return this.instanceNumber;
    }

    public long getContentGenerationNumber() {
        return this.contentGenerationNumber;
    }

    public long getLockGenerationNumber() {
        return this.lockGenerationNumber;
    }

    public long getLockRequestNumber() {
        return this.lockRequestNumber;
    }

    public void increaseAclGenerationNumberOnce() {
        this.aclGenerationNumber++;
    }

    public long getAclGenerationNumber() {
        return this.aclGenerationNumber;
    }

    public int getChildNodeNumber() {
        return this.childNodeNumber;
    }

    public Map<ChubbyHandleType, String> getAclNamesMap() {
        return this.aclNamesMap;
    }

    public void setAclNamesMap(Map<ChubbyHandleType, String> aclNamesMap) {
        this.aclNamesMap = aclNamesMap;
    }

    public ChubbyNodeAttribute getChubbyNodeAttribute() {
        return this.chubbyNodeAttribute;
    }

    public ChubbyNodeType getChubbyNodeType() {
        return this.chubbyNodeType;
    }
}
