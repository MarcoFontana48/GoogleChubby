package chubby.server.node;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ChubbyNodeValueTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void check_null_fileContent_dir() {
        Path path = Path.of("/prova");
        String fileContent = null;

        ChubbyNodeMetadata chubbyNodeMetadata = new ChubbyNodeMetadata(path, fileContent, ChubbyNodeAttribute.PERMANENT);
        ChubbyNodeValue actualChubbyNodeValue = new ChubbyNodeValue(fileContent, chubbyNodeMetadata);

        String actual = actualChubbyNodeValue.getFilecontent();
        String expected = "";

        assertEquals(expected, actual);
    }

    @Test
    void check_empty_fileContent_dir() {
        Path path = Path.of("/prova");
        String fileContent = "";

        ChubbyNodeMetadata chubbyNodeMetadata = new ChubbyNodeMetadata(path, fileContent, ChubbyNodeAttribute.PERMANENT);
        ChubbyNodeValue actualChubbyNodeValue = new ChubbyNodeValue(fileContent, chubbyNodeMetadata);

        String actual = actualChubbyNodeValue.getFilecontent();
        String expected = "";

        assertEquals(expected, actual);
    }

    @Test
    void check_null_fileContent_file() {
        Path path = Path.of("/prova.txt");
        String fileContent = null;

        ChubbyNodeMetadata chubbyNodeMetadata = new ChubbyNodeMetadata(path, fileContent, ChubbyNodeAttribute.PERMANENT);
        ChubbyNodeValue actualChubbyNodeValue = new ChubbyNodeValue(fileContent, chubbyNodeMetadata);

        String actual = actualChubbyNodeValue.getFilecontent();
        String expected = "";

        assertEquals(expected, actual);
    }

    @Test
    void check_empty_fileContent_file() {
        Path path = Path.of("/prova.txt");
        String fileContent = "";

        ChubbyNodeMetadata chubbyNodeMetadata = new ChubbyNodeMetadata(path, fileContent, ChubbyNodeAttribute.PERMANENT);
        ChubbyNodeValue actualChubbyNodeValue = new ChubbyNodeValue(fileContent, chubbyNodeMetadata);

        String actual = actualChubbyNodeValue.getFilecontent();
        String expected = "";

        assertEquals(expected, actual);
    }

    @Test
    void check_notEmpty_fileContent_dir() {
        Path path = Path.of("/prova");
        String fileContent = "hello world!";

        ChubbyNodeMetadata chubbyNodeMetadata = new ChubbyNodeMetadata(path, fileContent, ChubbyNodeAttribute.PERMANENT);
        ChubbyNodeValue actualChubbyNodeValue = new ChubbyNodeValue(fileContent, chubbyNodeMetadata);

        String actual = actualChubbyNodeValue.getFilecontent();
        String expected = "";

        assertEquals(expected, actual);
    }

    @Test
    void check_notEmpty_fileContent_file() {
        Path path = Path.of("/prova.txt");
        String fileContent = "hello world!";

        ChubbyNodeMetadata chubbyNodeMetadata = new ChubbyNodeMetadata(path, fileContent, ChubbyNodeAttribute.PERMANENT);
        ChubbyNodeValue actualChubbyNodeValue = new ChubbyNodeValue(fileContent, chubbyNodeMetadata);

        String actual = actualChubbyNodeValue.getFilecontent();
        String expected = "hello world!";

        assertEquals(expected, actual);
    }

    @Test
    void check_notEmpty_updated_fileContent_file() {
        Path path = Path.of("/prova.txt");
        String fileContent = "";
        String fileContentUpdate = "hello world!";

        ChubbyNodeMetadata chubbyNodeMetadata = new ChubbyNodeMetadata(path, fileContent, ChubbyNodeAttribute.PERMANENT);
        ChubbyNodeValue actualChubbyNodeValue = new ChubbyNodeValue(fileContent, chubbyNodeMetadata);

        actualChubbyNodeValue.setFilecontent(fileContentUpdate);

        String actual = actualChubbyNodeValue.getFilecontent();
        String expected = fileContentUpdate;

        assertEquals(expected, actual);
    }

    @Test
    void check_notEmpty_updated_fileContent_checksum_file() {
        Path path = Path.of("/prova.txt");
        String fileContent = "";
        String fileContentUpdate = "hello world!";

        ChubbyNodeMetadata chubbyNodeMetadata = new ChubbyNodeMetadata(path, fileContent, ChubbyNodeAttribute.PERMANENT);
        ChubbyNodeValue actualChubbyNodeValue = new ChubbyNodeValue(fileContent, chubbyNodeMetadata);

        actualChubbyNodeValue.setFilecontent(fileContentUpdate);

        long actual = actualChubbyNodeValue.getMetadata().getChecksum();
        long expected = -217287203;

        assertEquals(expected, actual);
    }

    @Test
    void check_notEmpty_notUpdated_fileContent_contentGenNumber_file() {
        Path path = Path.of("/prova.txt");
        String fileContent = "hello world!";

        ChubbyNodeMetadata chubbyNodeMetadata = new ChubbyNodeMetadata(path, fileContent, ChubbyNodeAttribute.PERMANENT);
        ChubbyNodeValue actualChubbyNodeValue = new ChubbyNodeValue(fileContent, chubbyNodeMetadata);

        long actual = actualChubbyNodeValue.getMetadata().getContentGenerationNumber();
        long expected = Long.MIN_VALUE;

        assertEquals(expected, actual);
    }

    @Test
    void check_notEmpty_updated_fileContent_contentGenNumber_file() {
        Path path = Path.of("/prova.txt");
        String fileContent = "";
        String fileContentUpdate = "hello world!";

        ChubbyNodeMetadata chubbyNodeMetadata = new ChubbyNodeMetadata(path, fileContent, ChubbyNodeAttribute.PERMANENT);
        ChubbyNodeValue actualChubbyNodeValue = new ChubbyNodeValue(fileContent, chubbyNodeMetadata);

        actualChubbyNodeValue.setFilecontent(fileContentUpdate);

        long actual = actualChubbyNodeValue.getMetadata().getContentGenerationNumber();
        long expected = Long.MIN_VALUE + 1;

        assertEquals(expected, actual);
    }
}