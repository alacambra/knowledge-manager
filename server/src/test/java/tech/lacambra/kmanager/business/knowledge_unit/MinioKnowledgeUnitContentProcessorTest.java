package tech.lacambra.kmanager.business.knowledge_unit;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class MinioKnowledgeUnitContentProcessorTest {

    @Inject
    MinioKnowledgeUnitContentProcessor processor;

    @Test
    void testProcessKnowledgeUnitUris_withTwoPDFs() throws Exception {
        // Create first PDF with known text
        byte[] pdf1 = createPDFWithText("First PDF Content", "This is the content of the first PDF document.");
        
        // Create second PDF with known text
        byte[] pdf2 = createPDFWithText("Second PDF Content", "This is the content of the second PDF document.");
        
        // Convert to InputStreams
        List<InputStream> pdfStreams = List.of(
            new ByteArrayInputStream(pdf1),
            new ByteArrayInputStream(pdf2)
        );

        // Process the PDFs
        byte[] result = processor.processKnowledgeUnitUris(pdfStreams);

        // Verify the result
        assertNotNull(result);
        assertTrue(result.length > 0);
        
        // Persist for manual verification
        persistTestPDF(result, "merged-pdfs-test.pdf");
        
        // Extract text from the merged PDF to verify content
        String extractedText = extractTextFromPDF(result);
        
        // Verify both PDF contents are present
        assertTrue(extractedText.contains("First PDF Content"), 
            "Merged PDF should contain text from first PDF. Contains: " + extractedText);
        assertTrue(extractedText.contains("Second PDF Content"), 
            "Merged PDF should contain text from second PDF");
        assertTrue(extractedText.contains("This is the content of the first PDF document."), 
            "Merged PDF should contain content from first PDF");
        assertTrue(extractedText.contains("This is the content of the second PDF document."), 
            "Merged PDF should contain content from second PDF");
    }

    @Test
    void testProcessKnowledgeUnitUris_withEmptyList() {
        List<InputStream> emptyStreams = List.of();

        assertThrows(Exception.class, () -> 
            processor.processKnowledgeUnitUris(emptyStreams)
        );
    }

    @Test
    void testProcessKnowledgeUnitUris_withSinglePDF() throws Exception {
        byte[] pdf = createPDFWithText("Single PDF", "This is a single PDF document for testing.");
        
        List<InputStream> pdfStreams = List.of(new ByteArrayInputStream(pdf));

        byte[] result = processor.processKnowledgeUnitUris(pdfStreams);

        assertNotNull(result);
        assertTrue(result.length > 0);
        
        persistTestPDF(result, "single-pdf-test.pdf");
        
        String extractedText = extractTextFromPDF(result);
        assertTrue(extractedText.contains("Single PDF"));
        assertTrue(extractedText.contains("This is a single PDF document for testing."));
    }

    @Test
    void testProcessKnowledgeUnitUris_withMultiplePDFs() throws Exception {
        // Create three PDFs with different content
        byte[] pdf1 = createPDFWithText("Document A", "Content from document A with important information.");
        byte[] pdf2 = createPDFWithText("Document B", "Content from document B with different data.");
        byte[] pdf3 = createPDFWithText("Document C", "Content from document C with final details.");
        
        List<InputStream> pdfStreams = List.of(
            new ByteArrayInputStream(pdf1),
            new ByteArrayInputStream(pdf2),
            new ByteArrayInputStream(pdf3)
        );

        byte[] result = processor.processKnowledgeUnitUris(pdfStreams);

        assertNotNull(result);
        assertTrue(result.length > 0);
        
        persistTestPDF(result, "multiple-pdfs-test.pdf");
        
        String extractedText = extractTextFromPDF(result);
        
        // Verify all three PDF contents are present
        assertTrue(extractedText.contains("Document A"));
        assertTrue(extractedText.contains("Document B"));
        assertTrue(extractedText.contains("Document C"));
        assertTrue(extractedText.contains("important information"));
        assertTrue(extractedText.contains("different data"));
        assertTrue(extractedText.contains("final details"));
    }

    private byte[] createPDFWithText(String title, String content) throws Exception {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // Add title
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText(title);
                contentStream.endText();

                // Add content
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 700);
                contentStream.showText(content);
                contentStream.endText();

                // Add some additional lines to make it more realistic
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 680);
                contentStream.showText("Generated at: " + java.time.LocalDateTime.now());
                contentStream.endText();
            }

            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }

    private String extractTextFromPDF(byte[] pdfData) throws Exception {
        try (PDDocument document = Loader.loadPDF(pdfData)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private void persistTestPDF(byte[] pdfData, String filename) throws Exception {
        Path testPdfDir = Path.of("target/testpdf");
        Files.createDirectories(testPdfDir);
        
        Path pdfFile = testPdfDir.resolve("processor-test-" + System.currentTimeMillis() + "__" + filename);
        Files.write(pdfFile, pdfData);
        
        assertTrue(Files.exists(pdfFile));
        assertTrue(Files.size(pdfFile) > 0);
        
        System.out.println("Test PDF saved to: " + pdfFile.toAbsolutePath());
    }
}