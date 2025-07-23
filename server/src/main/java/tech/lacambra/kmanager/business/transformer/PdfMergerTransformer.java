package tech.lacambra.kmanager.business.transformer;

import jakarta.enterprise.context.ApplicationScoped;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@ApplicationScoped
public class PdfMergerTransformer implements Transformer<byte[]> {

    private static final Logger logger = LoggerFactory.getLogger(PdfMergerTransformer.class);

    @Override
    public byte[] transform(List<InputStream> inputStreams) {
        if (inputStreams == null || inputStreams.isEmpty()) {
            throw new IllegalArgumentException("Input streams cannot be null or empty");
        }

        logger.info("Starting PDF merge operation with {} input files", inputStreams.size());

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             PDDocument mergedDocument = new PDDocument()) {

            // Keep all source documents open until after we save
            List<PDDocument> sourceDocuments = new java.util.ArrayList<>();
            
            try {
                // Load all documents first
                for (int i = 0; i < inputStreams.size(); i++) {
                    InputStream inputStream = inputStreams.get(i);
                    
                    try {
                        PDDocument document = Loader.loadPDF(inputStream.readAllBytes());
                        sourceDocuments.add(document);
                        logger.info("Loaded PDF document {} (pages: {})", i + 1, document.getNumberOfPages());
                    } catch (IOException e) {
                        logger.error("Failed to load PDF document {}: {}", i + 1, e.getMessage());
                        throw new TransformerException("Failed to load PDF document " + (i + 1), e);
                    }
                }
                
                // Import pages from all documents
                for (int i = 0; i < sourceDocuments.size(); i++) {
                    PDDocument document = sourceDocuments.get(i);
                    logger.info("Adding PDF document {} to merger (pages: {})", i + 1, document.getNumberOfPages());
                    
                    for (int pageIndex = 0; pageIndex < document.getNumberOfPages(); pageIndex++) {
                        mergedDocument.importPage(document.getPage(pageIndex));
                    }
                }

                // Save the merged document while source documents are still open
                mergedDocument.save(outputStream);
                logger.info("PDF merge completed successfully. Output size: {} bytes", outputStream.size());
                
            } finally {
                // Close all source documents
                for (PDDocument doc : sourceDocuments) {
                    try {
                        doc.close();
                    } catch (IOException e) {
                        logger.warn("Failed to close source document: {}", e.getMessage());
                    }
                }
            }
            
            return outputStream.toByteArray();

        } catch (IOException e) {
            logger.error("PDF merge operation failed: {}", e.getMessage());
            throw new TransformerException("Failed to merge PDF documents", e);
        }
    }

    @Override
    public String getSupportedInputType() {
        return "application/pdf";
    }

    @Override
    public String getOutputType() {
        return "application/pdf";
    }
}