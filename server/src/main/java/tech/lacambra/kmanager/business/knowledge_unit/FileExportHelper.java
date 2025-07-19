package tech.lacambra.kmanager.business.knowledge_unit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FileExportHelper {

    private static final String EXPORT_DIRECTORY = "exports";
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    public Path writeToFile(String content, String filename) {
        try {
            Path exportDir = Paths.get(EXPORT_DIRECTORY);
            Files.createDirectories(exportDir);
            
            String sanitizedFilename = sanitizeFilename(filename);
            Path filePath = exportDir.resolve(sanitizedFilename);
            
            Files.writeString(filePath, content);
            
            return filePath;
        } catch (IOException e) {
            throw new ContentProcessingException("Failed to write content to file: " + filename, e);
        }
    }

    public String generateDefaultFilename(String knowledgeUnitName) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String sanitizedName = sanitizeFilename(knowledgeUnitName);
        return sanitizedName + "-" + timestamp + ".txt";
    }

    public String sanitizeFilename(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return "knowledge-unit";
        }
        
        String sanitized = removeInvalidCharacters(filename.trim());
        
        sanitized = ensureNonEmptyFilename(sanitized);
        
        sanitized = limitFilenameLength(sanitized);
        
        return sanitized;
    }

    private String removeInvalidCharacters(String filename) {
        return filename.replaceAll("[\\\\/:*?\"<>|]", "_")
                .replaceAll("\\s+", "_")
                .replaceAll("_{2,}", "_")
                .replaceAll("^_+|_+$", "");
    }

    private String ensureNonEmptyFilename(String filename) {
        return filename.isEmpty() ? "knowledge-unit" : filename;
    }

    private String limitFilenameLength(String filename) {
        return filename.length() > 100 ? filename.substring(0, 100) : filename;
    }
}