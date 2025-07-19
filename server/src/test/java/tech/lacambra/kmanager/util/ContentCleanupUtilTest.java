package tech.lacambra.kmanager.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ContentCleanupUtilTest {

    private final ContentCleanupUtil contentCleanupUtil = new ContentCleanupUtil();

    @Test
    void cleanDocumentContent_removesNullBytes() {
        String input = "Hello\u0000World";
        String result = contentCleanupUtil.cleanDocumentContent(input);
        assertEquals("HelloWorld\n", result);
    }

    @Test
    void cleanDocumentContent_removesControlCharacters() {
        String input = "Hello\u0001\u0002World\tTest\nLine";
        String result = contentCleanupUtil.cleanDocumentContent(input);
        assertEquals("HelloWorld\tTest\nLine\n", result);
    }

    @Test
    void cleanDocumentContent_normalizesLineEndings() {
        String input = "Line1\r\nLine2\rLine3\n";
        String result = contentCleanupUtil.cleanDocumentContent(input);
        assertEquals("Line1\nLine2\nLine3\n", result);
    }

    @Test
    void cleanDocumentContent_limitsConsecutiveBlankLines() {
        String input = "Line1\n\n\n\n\nLine2";
        String result = contentCleanupUtil.cleanDocumentContent(input);
        assertEquals("Line1\n\nLine2\n", result);
    }

    @Test
    void cleanDocumentContent_handlesNull() {
        String result = contentCleanupUtil.cleanDocumentContent(null);
        assertEquals("", result);
    }

    @Test
    void cleanDocumentContent_ensuresTrailingNewline() {
        String input = "Hello World";
        String result = contentCleanupUtil.cleanDocumentContent(input);
        assertEquals("Hello World\n", result);
    }

    @Test
    void cleanMetadataValue_removesNullBytes() {
        String input = "Test\u0000Value";
        String result = contentCleanupUtil.cleanMetadataValue(input);
        assertEquals("TestValue", result);
    }

    @Test
    void cleanMetadataValue_trimsWhitespace() {
        String input = "  Test Value  ";
        String result = contentCleanupUtil.cleanMetadataValue(input);
        assertEquals("Test Value", result);
    }

    @Test
    void cleanMetadataValue_handlesNull() {
        String result = contentCleanupUtil.cleanMetadataValue(null);
        assertEquals("", result);
    }

    @Test
    void normalizeLineEndings_convertsCRLF() {
        String input = "Line1\r\nLine2\r\nLine3";
        String result = contentCleanupUtil.normalizeLineEndings(input);
        assertEquals("Line1\nLine2\nLine3", result);
    }

    @Test
    void normalizeLineEndings_convertsCR() {
        String input = "Line1\rLine2\rLine3";
        String result = contentCleanupUtil.normalizeLineEndings(input);
        assertEquals("Line1\nLine2\nLine3", result);
    }

    @Test
    void normalizeLineEndings_handlesNull() {
        String result = contentCleanupUtil.normalizeLineEndings(null);
        assertEquals("", result);
    }
}