package chubby.server.node;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChubbyNodeMetadataTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void check_instanceNumber_single_dir() {
        Path path = Path.of("/prova");
        ChubbyNodeMetadata actualChubbyNodeMetadata = new ChubbyNodeMetadata(path, null, ChubbyNodeAttribute.PERMANENT);
        long actualInstanceNumber = actualChubbyNodeMetadata.getInstanceNumber();
        long expectedInstanceNumber = Long.MIN_VALUE;

        assertEquals(expectedInstanceNumber, actualInstanceNumber);
    }

    @Test
    void check_instanceNumber_double_sameName_dir() {
        Path path = Path.of("/prova/prova");
        ChubbyNodeMetadata actualChubbyNodeMetadata = new ChubbyNodeMetadata(path, null, ChubbyNodeAttribute.PERMANENT);
        long actualInstanceNumber = actualChubbyNodeMetadata.getInstanceNumber();
        long expectedInstanceNumber = Long.MIN_VALUE + 1;

        assertEquals(expectedInstanceNumber, actualInstanceNumber);
    }

    @Test
    void check_instanceNumber_triple_sameName_dir() {
        Path path = Path.of("/prova/prova/prova");
        ChubbyNodeMetadata actualChubbyNodeMetadata = new ChubbyNodeMetadata(path, null, ChubbyNodeAttribute.PERMANENT);
        long actualInstanceNumber = actualChubbyNodeMetadata.getInstanceNumber();
        long expectedInstanceNumber = Long.MIN_VALUE + 2;

        assertEquals(expectedInstanceNumber, actualInstanceNumber);
    }

    @Test
    void check_instanceNumber_double_sameName_DirAndFile() {
        Path path = Path.of("/prova/prova.txt");
        ChubbyNodeMetadata actualChubbyNodeMetadata = new ChubbyNodeMetadata(path, null, ChubbyNodeAttribute.PERMANENT);
        long actualInstanceNumber = actualChubbyNodeMetadata.getInstanceNumber();
        long expectedInstanceNumber = Long.MIN_VALUE;

        assertEquals(expectedInstanceNumber, actualInstanceNumber);
    }

    @Test
    void check_checksum_null_dir() {
        Path path = Path.of("/prova");
        ChubbyNodeMetadata actualChubbyNodeMetadata = new ChubbyNodeMetadata(path, null, ChubbyNodeAttribute.PERMANENT);
        long actual = actualChubbyNodeMetadata.getChecksum();
        long expected = 0;

        assertEquals(expected, actual);
    }

    @Test
    void check_checksum_null_file() {
        Path path = Path.of("/prova.txt");
        ChubbyNodeMetadata actualChubbyNodeMetadata = new ChubbyNodeMetadata(path, null, ChubbyNodeAttribute.PERMANENT);
        long actual = actualChubbyNodeMetadata.getChecksum();
        long expected = 0;

        assertEquals(expected, actual);
    }

    @Test
    void check_checksum_empty_dir() {
        Path path = Path.of("/prova");
        ChubbyNodeMetadata actualChubbyNodeMetadata = new ChubbyNodeMetadata(path, "", ChubbyNodeAttribute.PERMANENT);
        long actual = actualChubbyNodeMetadata.getChecksum();
        long expected = 0;

        assertEquals(expected, actual);
    }

    @Test
    void check_checksum_empty_file() {
        Path path = Path.of("/prova.txt");
        ChubbyNodeMetadata actualChubbyNodeMetadata = new ChubbyNodeMetadata(path, "", ChubbyNodeAttribute.PERMANENT);
        long actual = actualChubbyNodeMetadata.getChecksum();
        long expected = 0;

        assertEquals(expected, actual);
    }

    @Test
    void check_checksum_notEmpty_file() {
        Path path = Path.of("/prova.txt");
        ChubbyNodeMetadata actualChubbyNodeMetadata = new ChubbyNodeMetadata(path, "hello world!", ChubbyNodeAttribute.PERMANENT);
        long actual = actualChubbyNodeMetadata.getChecksum();
        long expected = -217287203;

        assertEquals(expected, actual);
    }

    @Test
    void check_checksum_notEmpty_dir() {
        Path path = Path.of("/prova");
        //if file content is not empty and node is dir, it has to be emptied
        ChubbyNodeMetadata actualChubbyNodeMetadata = new ChubbyNodeMetadata(path, "hello world!", ChubbyNodeAttribute.PERMANENT);
        long actual = actualChubbyNodeMetadata.getChecksum();
        long expected = 0;

        assertEquals(expected, actual);
    }

    @Test
    void check_lockGenerationNumber_initValue_file() {
        Path path = Path.of("/prova.txt");
        ChubbyNodeMetadata actualChubbyNodeMetadata = new ChubbyNodeMetadata(path, "hello world!", ChubbyNodeAttribute.PERMANENT);
        long actual = actualChubbyNodeMetadata.getLockGenerationNumber();
        long expected = Long.MIN_VALUE;

        assertEquals(expected, actual);
    }

    @Test
    void check_lockRequestNumber_initValue_file() {
        Path path = Path.of("/prova.txt");
        ChubbyNodeMetadata actualChubbyNodeMetadata = new ChubbyNodeMetadata(path, "hello world!", ChubbyNodeAttribute.PERMANENT);
        long actual = actualChubbyNodeMetadata.getLockRequestNumber();
        long expected = Long.MIN_VALUE;

        assertEquals(expected, actual);
    }

    @Test
    void check_lockClientMap_initValue_file() {
        Path path = Path.of("/prova.txt");
        ChubbyNodeMetadata actualChubbyNodeMetadata = new ChubbyNodeMetadata(path, "hello world!", ChubbyNodeAttribute.PERMANENT);
        long actual = actualChubbyNodeMetadata.getLockClientMapSize();
        long expected = 0;

        assertEquals(expected, actual);
    }

    @Test
    void check_lockAclGenerationNumber_initValue_file() {
        Path path = Path.of("/prova.txt");
        ChubbyNodeMetadata actualChubbyNodeMetadata = new ChubbyNodeMetadata(path, "hello world!", ChubbyNodeAttribute.PERMANENT);
        long actual = actualChubbyNodeMetadata.getAclGenerationNumber();
        long expected = Long.MIN_VALUE;

        assertEquals(expected, actual);
    }

    @Test
    void check_childNodeNumber_initValue_file() {
        Path path = Path.of("/prova.txt");
        ChubbyNodeMetadata actualChubbyNodeMetadata = new ChubbyNodeMetadata(path, "hello world!", ChubbyNodeAttribute.PERMANENT);
        long actual = actualChubbyNodeMetadata.getChildNodeNumber();
        long expected = 0;

        assertEquals(expected, actual);
    }

    @Test
    void check_nodeType_initValue_file() {
        Path path = Path.of("/prova.txt");
        ChubbyNodeMetadata actualChubbyNodeMetadata = new ChubbyNodeMetadata(path, "hello world!", ChubbyNodeAttribute.PERMANENT);
        ChubbyNodeType actual = actualChubbyNodeMetadata.getChubbyNodeType();
        ChubbyNodeType expected = ChubbyNodeType.FILE;

        assertEquals(expected, actual);
    }

    @Test
    void check_nodeType_initValue_dir() {
        Path path = Path.of("/prova");
        //if a directory has file content not empty, it has to be emptied!
        ChubbyNodeMetadata actualChubbyNodeMetadata = new ChubbyNodeMetadata(path, "hello world!", ChubbyNodeAttribute.PERMANENT);
        ChubbyNodeType actual = actualChubbyNodeMetadata.getChubbyNodeType();
        ChubbyNodeType expected = ChubbyNodeType.DIRECTORY;

        assertEquals(expected, actual);
    }

    @Test
    void check_nodeAttribute_initValue_permanent() {
        Path path = Path.of("/prova");
        //if a directory has file content not empty, it has to be emptied!
        ChubbyNodeMetadata actualChubbyNodeMetadata = new ChubbyNodeMetadata(path, "hello world!", ChubbyNodeAttribute.PERMANENT);
        ChubbyNodeAttribute actual = actualChubbyNodeMetadata.getChubbyNodeAttribute();
        ChubbyNodeAttribute expected = ChubbyNodeAttribute.PERMANENT;

        assertEquals(expected, actual);
    }

    @Test
    void check_nodeAttribute_initValue_ephemeral() {
        Path path = Path.of("/prova");
        //if a directory has file content not empty, it has to be emptied!
        ChubbyNodeMetadata actualChubbyNodeMetadata = new ChubbyNodeMetadata(path, "hello world!", ChubbyNodeAttribute.EPHEMERAL);
        ChubbyNodeAttribute actual = actualChubbyNodeMetadata.getChubbyNodeAttribute();
        ChubbyNodeAttribute expected = ChubbyNodeAttribute.EPHEMERAL;

        assertEquals(expected, actual);
    }
}