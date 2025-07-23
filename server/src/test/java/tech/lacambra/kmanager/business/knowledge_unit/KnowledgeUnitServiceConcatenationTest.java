package tech.lacambra.kmanager.business.knowledge_unit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tech.lacambra.kmanager.generated.jooq.tables.pojos.KnowledgeUnit;
import tech.lacambra.kmanager.resource.knowlege_manager.KnowledgeUnitWithDocumentGroupUrisResponse;
import tech.lacambra.kmanager.business.documents.DocumentRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KnowledgeUnitServiceConcatenationTest {

    @Mock
    private KnowledgeUnitRepository knowledgeUnitRepository;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private MinioKnowledgeUnitContentProcessor minioContentProcessor;

    @Mock
    private FileExportHelper fileExportHelper;

    private KnowledgeUnitService knowledgeUnitService;

    @BeforeEach
    void setUp() {
        knowledgeUnitService = new KnowledgeUnitService(
            knowledgeUnitRepository,
            documentRepository,
            minioContentProcessor,
            fileExportHelper
        );
    }

    @Test
    void generateConcatenatedText_success() {
        UUID kuId = UUID.randomUUID();
        
        KnowledgeUnit ku = new KnowledgeUnit();
        ku.setId(kuId);
        ku.setName("Test KU");
        ku.setDescription("Test Description");
        ku.setCreatedAt(LocalDateTime.now());
        ku.setUpdatedAt(LocalDateTime.now());

        List<String> documentGroupUris = List.of("test-bucket/path1", "test-bucket/path2");
        KnowledgeUnitWithDocumentGroupUrisResponse response = new KnowledgeUnitWithDocumentGroupUrisResponse(ku, documentGroupUris);

        when(knowledgeUnitRepository.findByIdWithDocumentGroupUris(kuId))
            .thenReturn(Optional.of(response));
        when(minioContentProcessor.processKnowledgeUnitToText(response))
            .thenReturn("MinIO concatenated content");

        String result = knowledgeUnitService.generateConcatenatedText(kuId);

        assertEquals("MinIO concatenated content", result);
        verify(knowledgeUnitRepository).findByIdWithDocumentGroupUris(kuId);
        verify(minioContentProcessor).processKnowledgeUnitToText(response);
    }

    @Test
    void generateConcatenatedText_knowledgeUnitNotFound() {
        UUID kuId = UUID.randomUUID();

        when(knowledgeUnitRepository.findByIdWithDocumentGroupUris(kuId))
            .thenReturn(Optional.empty());

        assertThrows(KnowledgeUnitNotFoundException.class, () -> {
            knowledgeUnitService.generateConcatenatedText(kuId);
        });

        verify(knowledgeUnitRepository).findByIdWithDocumentGroupUris(kuId);
        verifyNoInteractions(minioContentProcessor);
    }

    @Test
    void exportToFile_withCustomFilename() {
        UUID kuId = UUID.randomUUID();
        String customFilename = "custom-export.txt";
        
        KnowledgeUnit ku = new KnowledgeUnit();
        ku.setId(kuId);
        ku.setName("Test KU");

        KnowledgeUnitWithDocumentGroupUrisResponse response = new KnowledgeUnitWithDocumentGroupUrisResponse(ku, List.of());

        when(knowledgeUnitRepository.findByIdWithDocumentGroupUris(kuId))
            .thenReturn(Optional.of(response));
        when(minioContentProcessor.processKnowledgeUnitToText(response))
            .thenReturn("Export content");
        when(fileExportHelper.writeToFile("Export content", customFilename))
            .thenReturn(Path.of("exports/custom-export.txt"));

        Path result = knowledgeUnitService.exportToFile(kuId, customFilename);

        assertEquals(Path.of("exports/custom-export.txt"), result);
        verify(fileExportHelper).writeToFile("Export content", customFilename);
        verify(fileExportHelper, never()).generateDefaultFilename(any());
    }

    @Test
    void exportToFile_withDefaultFilename() {
        UUID kuId = UUID.randomUUID();
        
        KnowledgeUnit ku = new KnowledgeUnit();
        ku.setId(kuId);
        ku.setName("Test KU");

        KnowledgeUnitWithDocumentGroupUrisResponse response = new KnowledgeUnitWithDocumentGroupUrisResponse(ku, List.of());

        when(knowledgeUnitRepository.findByIdWithDocumentGroupUris(kuId))
            .thenReturn(Optional.of(response));
        when(minioContentProcessor.processKnowledgeUnitToText(response))
            .thenReturn("Export content");
        when(fileExportHelper.generateDefaultFilename("Test KU"))
            .thenReturn("Test_KU-20240719-120000.txt");
        when(fileExportHelper.writeToFile("Export content", "Test_KU-20240719-120000.txt"))
            .thenReturn(Path.of("exports/Test_KU-20240719-120000.txt"));

        Path result = knowledgeUnitService.exportToFile(kuId, null);

        assertEquals(Path.of("exports/Test_KU-20240719-120000.txt"), result);
        verify(fileExportHelper).generateDefaultFilename("Test KU");
        verify(fileExportHelper).writeToFile("Export content", "Test_KU-20240719-120000.txt");
    }
}