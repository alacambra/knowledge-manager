package tech.lacambra.kmanager.documents;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.enterprise.context.ApplicationScoped;

import org.jooq.DSLContext;
import org.jooq.JSONB;
import tech.lacambra.kmanager.ai.EmbeddingService;
import tech.lacambra.kmanager.generated.jooq.tables.pojos.Documents;
import tech.lacambra.kmanager.generated.jooq.tables.records.DocumentsRecord;
import static tech.lacambra.kmanager.generated.jooq.tables.Documents.*;

@ApplicationScoped
@Transactional
public class DocumentRepository {

    private static final Logger LOGGER = Logger.getLogger(DocumentRepository.class.getName());
    
    private final DSLContext dsl;
    private final EmbeddingService embeddingService;
    private final Random random = new Random();
    
    // Sample texts for random document generation
    private static final String[] SAMPLE_TITLES = {
        "Introduction to Machine Learning",
        "Understanding Vector Databases",
        "Advanced RAG Techniques",
        "Building Scalable AI Applications",
        "Natural Language Processing Fundamentals",
        "Deep Learning for Text Analysis",
        "Semantic Search Implementation",
        "AI Ethics and Responsible Development",
        "Transformer Architecture Explained",
        "Vector Embeddings in Practice"
    };
    
    private static final String[] SAMPLE_CONTENTS = {
        "Machine learning is a subset of artificial intelligence that enables computers to learn and improve from experience without being explicitly programmed. It involves algorithms that can identify patterns in data and make predictions or decisions based on those patterns.",
        "Vector databases are specialized database systems designed to store, index, and query high-dimensional vector data efficiently. They are particularly useful for applications involving similarity search, recommendation systems, and machine learning.",
        "Retrieval-Augmented Generation (RAG) is a technique that combines the power of large language models with external knowledge retrieval. This approach allows AI systems to access up-to-date information and provide more accurate responses.",
        "Building scalable AI applications requires careful consideration of architecture, data pipelines, model deployment, and monitoring. Key factors include choosing the right infrastructure, implementing proper caching strategies, and ensuring model performance.",
        "Natural Language Processing (NLP) is a branch of AI that focuses on the interaction between computers and human language. It encompasses various tasks such as text classification, named entity recognition, sentiment analysis, and language translation.",
        "Deep learning has revolutionized text analysis by enabling models to understand context, semantics, and complex relationships within textual data. Techniques like attention mechanisms and transformer architectures have significantly improved performance.",
        "Semantic search goes beyond keyword matching to understand the meaning and context of queries. It uses techniques like vector embeddings and similarity measures to find relevant content based on semantic similarity rather than exact word matches.",
        "AI ethics is a critical consideration in the development and deployment of artificial intelligence systems. It involves ensuring fairness, transparency, accountability, and privacy protection while minimizing potential harms and biases.",
        "The Transformer architecture, introduced in the 'Attention is All You Need' paper, has become the foundation for many modern NLP models. It relies entirely on attention mechanisms and has enabled the development of large language models like GPT and BERT.",
        "Vector embeddings are numerical representations of data that capture semantic meaning in high-dimensional space. They enable machines to understand and process complex relationships between different pieces of information in a computationally efficient manner."
    };

    @Inject
    public DocumentRepository(DSLContext dsl, EmbeddingService embeddingService) {
        this.dsl = dsl;
        this.embeddingService = embeddingService;
    }

    public List<Documents> getAllDocuments() {
        return dsl.selectFrom(DOCUMENTS)
                .fetchInto(Documents.class);
    }
    
    /**
     * Creates a random document with the given text content and generates embeddings for it.
     * 
     * @param customText Optional custom text content. If null, uses a random sample text.
     * @return The created document
     */
    public Documents createRandomDocumentWithEmbedding(String customText) {
        String title = SAMPLE_TITLES[random.nextInt(SAMPLE_TITLES.length)];
        String content = customText != null ? customText : SAMPLE_CONTENTS[random.nextInt(SAMPLE_CONTENTS.length)];
        
        // Add some randomization to the title if using custom text
        if (customText != null) {
            title = "Custom Document #" + (random.nextInt(1000) + 1);
        }
        
        LOGGER.info("Creating random document with title: " + title);
        
        try {
            // Generate embedding for the content using your real ONNX service
            float[] embedding = embeddingService.generateEmbedding(content);
            
            // Create metadata
            JSONB metadata = JSONB.valueOf("{"
                + "\"source\": \"random_generation\","
                + "\"embedding_model\": \"onnx_huggingface\","
                + "\"content_length\": " + content.length() + ","
                + "\"embedding_dimension\": " + embedding.length + ","
                + "\"created_by\": \"helloai_endpoint\""
                + "}");
            
            // Insert document into database
            DocumentsRecord record = dsl.insertInto(DOCUMENTS)
                .set(DOCUMENTS.TITLE, title)
                .set(DOCUMENTS.CONTENT, content)
                .set(DOCUMENTS.EMBEDDING, convertEmbeddingToSqlArray(embedding))
                .set(DOCUMENTS.METADATA, metadata)
                .returning()
                .fetchOne();
            
            if (record == null) {
                throw new RuntimeException("Failed to insert document");
            }
            
            LOGGER.info("Document created successfully with ID: " + record.getId() + 
                       ", embedding dimension: " + embedding.length);
            
            // Convert record to POJO
            Documents document = new Documents();
            document.setId(record.getId());
            document.setTitle(record.getTitle());
            document.setContent(record.getContent());
            document.setEmbedding(record.getEmbedding());
            document.setMetadata(record.getMetadata());
            document.setCreatedAt(record.getCreatedAt());
            document.setUpdatedAt(record.getUpdatedAt());
            
            return document;
            
        } catch (Exception e) {
            LOGGER.severe("Error creating document: " + e.getMessage());
            throw new RuntimeException("Failed to create document with embedding", e);
        }
    }
    
    /**
     * Creates a random document with a randomly selected sample text.
     * 
     * @return The created document
     */
    public Documents createRandomDocumentWithEmbedding() {
        return createRandomDocumentWithEmbedding(null);
    }
    
    /**
     * Converts float array embedding to SQL array format for PostgreSQL.
     * This is a simplified implementation - in a real scenario, you might need
     * to use a proper PostgreSQL array type or vector type binding.
     */
    private String convertEmbeddingToSqlArray(float[] embedding) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(embedding[i]);
        }
        sb.append("]");
        return sb.toString();
    }
    
    /**
     * Find documents by ID.
     * 
     * @param id Document ID
     * @return Document if found, null otherwise
     */
    public Documents findById(Long id) {
        return dsl.selectFrom(DOCUMENTS)
                .where(DOCUMENTS.ID.eq(id))
                .fetchOneInto(Documents.class);
    }
    
    /**
     * Get the total count of documents.
     * 
     * @return Total number of documents
     */
    public int getDocumentCount() {
        return dsl.selectCount()
                .from(DOCUMENTS)
                .fetchOne(0, int.class);
    }
}