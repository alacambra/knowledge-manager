package tech.lacambra.kmanager.util;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ContentCleanupUtil {

    public String cleanDocumentContent(String rawContent) {
        if (rawContent == null) {
            return "";
        }
        
        String cleaned = rawContent;
        
        cleaned = removeNullBytes(cleaned);
        cleaned = removeControlCharacters(cleaned);
        cleaned = normalizeLineEndings(cleaned);
        cleaned = removeExcessiveBlankLines(cleaned);
        cleaned = trimAndEnsureNewlineEnding(cleaned);
        
        return cleaned;
    }

    public String cleanMetadataValue(String value) {
        if (value == null) {
            return "";
        }
        
        String cleaned = value;
        
        cleaned = removeNullBytes(cleaned);
        cleaned = removeControlCharacters(cleaned);
        cleaned = cleaned.trim();
        
        return cleaned;
    }

    public String normalizeLineEndings(String content) {
        if (content == null) {
            return "";
        }
        
        return content.replaceAll("\r\n", "\n").replaceAll("\r", "\n");
    }

    private String removeNullBytes(String content) {
        return content.replace("\u0000", "");
    }

    private String removeControlCharacters(String content) {
        return content.replaceAll("[\\p{Cntrl}&&[^\t\n\r]]", "");
    }

    private String removeExcessiveBlankLines(String content) {
        return content.replaceAll("\n{3,}", "\n\n");
    }

    private String trimAndEnsureNewlineEnding(String content) {
        String trimmed = content.trim();
        if (!trimmed.isEmpty() && !trimmed.endsWith("\n")) {
            trimmed += "\n";
        }
        return trimmed;
    }
}