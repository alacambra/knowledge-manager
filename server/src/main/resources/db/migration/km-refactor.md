# Resource-Centric Knowledge Manager - Concept & Requirements

## Overview

This document outlines the refactoring of the current Knowledge Manager from a document-centric to a resource-centric architecture. The new model introduces the concept of "resources" that can point to content stored in MinIO, enabling flexible content aggregation and Knowledge Unit (KU) file generation.

## Current vs Target Architecture

### Current Architecture

```
Knowledge Unit → Documents (direct content storage)
```

### Target Architecture  

```
Knowledge Unit → Resources → Documents (processed content) ->  MinIO Content
```

## Core Concepts

### 1. Resources

Resources are semantically bound set of documents. MEtadata can be added there. E.g. Resources -> Backend -> documents:
META: stack: js, java17, quarkjus; enviroements: prod, ....
 -> source code: ./Backend/*.(java|pom|proeprties)
 -> quarkus: Rest
 -> jooq
 -> ...
 -> architecture definitions
 -> diomain documentation
 -> deployment flows
 ....

document_group:
 - groups one or more final docs
 - can be an url
 - a directory
 - a final dcoument pointer

document:
 - final document information
 - docuemnt metadata
 - versioning
 - text and vector indexing

### 2. Content Storage Strategy

#### MinIO Integration

- **All content persisted in MinIO**: No local file system storage
- **Relative URLs only**: Context provides MinIO server/bucket/project
- **Configuration-based**: MinIO connection details in Quarkus config
- **No direct integration modules**: Use MinIO REST API endpoints

#### Configuration Structure

```properties
# Quarkus application.properties
minio.prefix=${MINIO_PREFIX:http://localhost:9000/bucket/project}
```

### 3. Knowledge Unit (KU) File Generation

#### Output Format

- **Single unified text file** containing all processed content
- **Plain text format** for LLM consumption
- **Structured content** with clear resource boundaries and metadata

#### Future Enhancements (Not in Scope)

- Database storage of generated KU content
- Vector embeddings for similarity search
- Full-text search integration

## Database Schema Requirements

### 1. Schema Transformation

#### Current Schema

```sql
document (id, title, content, embedding, metadata, created_at, updated_at)
knowledge_unit (id, name, description, created_at, updated_at)
knowledge_unit_document (id, knowledge_unit_id, document_id, created_at)
```

#### Target Schema

```sql
-- Renamed and extended from 'document'
resource (
  id UUID PRIMARY KEY,
  type VARCHAR(20) NOT NULL,           -- 'POINTER' or 'DOCUMENT'
  title TEXT,                          -- Resource title
  source_path TEXT,                    -- MinIO relative path (for POINTER)
  content TEXT,                        -- Processed content (for DOCUMENT)
  filters JSONB,                       -- Inclusion/exclusion rules
  metadata JSONB,                      -- Additional metadata
  embedding vector(384),               -- For future vector search
  created_at TIMESTAMP,
  updated_at TIMESTAMP
)

-- Links resources to their processed documents
resource_document (
  id UUID PRIMARY KEY,
  resource_id UUID REFERENCES resource(id),
  document_id UUID REFERENCES resource(id), -- Points to DOCUMENT type resource
  processing_order INTEGER,             -- Order for KU generation
  created_at TIMESTAMP
)

-- Updated relationship table
knowledge_unit_resource (
  id UUID PRIMARY KEY,
  knowledge_unit_id UUID REFERENCES knowledge_unit(id),
  resource_id UUID REFERENCES resource(id),
  created_at TIMESTAMP,
  UNIQUE(knowledge_unit_id, resource_id)
)
```

### 2. Migration Strategy

- **V3__refactor_to_resources.sql**: Transform existing documents to resources
- **Preserve data**: Existing documents become DOCUMENT type resources
- **Update relationships**: Migrate knowledge_unit_document to knowledge_unit_resource

## Java Model Requirements

### 1. Core Entities

#### Resource Entity

```java
@Entity
@Table(name = "resource")
public class Resource {
    private UUID id;
    private ResourceType type;
    private String title;
    private String sourcePath;        // MinIO relative path
    private String content;           // Processed content
    private JSONB filters;           // Filter configuration
    private JSONB metadata;          // Additional metadata
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

#### ResourceType Enum

```java
public enum ResourceType {
    POINTER,    // Points to external content
    DOCUMENT    // Contains processed content
}
```

#### FilterConfiguration Class

```java
public class FilterConfiguration {
    private Set<String> includedFileTypes;    // e.g., {"pdf", "txt", "md"}
    private Set<String> excludedFileTypes;    // e.g., {"tmp", "log"}
    private Set<String> includedFolders;      // Folder path patterns
    private Set<String> excludedFolders;     // Folder exclusion patterns
    private boolean recursive;                // Enable recursive folder processing
}
```

### 2. Repository Updates

- **ResourceRepository**: Replace DocumentRepository
- **Resource-based queries**: Filter by type, source path, etc.
- **Relationship management**: Handle resource-to-document links

### 3. Service Layer

- **ResourceService**: Resource CRUD operations
- **ContentProcessingService**: Future MinIO content processing
- **KUGenerationService**: Future unified file generation

## Filtering System Design (Future Implementation)

### Filter Types

#### File Type Filters

- **Inclusion**: Only process specified file types
- **Exclusion**: Skip specified file types
- **Example**: `{"includedFileTypes": ["pdf", "docx", "txt"]}`

#### Folder Filters  

- **Inclusion**: Only process specified folder patterns
- **Exclusion**: Skip specified folder patterns
- **Recursive control**: Enable/disable recursive folder traversal
- **Example**: `{"includedFolders": ["/docs/*"], "excludedFolders": ["/temp/*"]}`

### Filter Application

1. **Resource level**: Each resource has its own filter configuration
2. **Processing time**: Filters applied when resolving POINTER resources
3. **Precedence**: Exclusion filters take precedence over inclusion filters

## REST API Requirements

### 1. Resource Management Endpoints

```
GET    /api/resources              - List all resources
POST   /api/resources              - Create new resource
GET    /api/resources/{id}         - Get resource details
PUT    /api/resources/{id}         - Update resource
DELETE /api/resources/{id}         - Delete resource
```

### 2. Knowledge Unit Endpoints (Updated)

```
POST   /api/knowledge-units/{id}/resources    - Add resource to KU
DELETE /api/knowledge-units/{id}/resources/{resourceId} - Remove resource from KU
GET    /api/knowledge-units/{id}/ku-file      - Generate and download KU file (future)
```

### 3. MinIO Integration Endpoints (Future)

```
GET    /api/minio/browse?path={path}          - Browse MinIO objects
POST   /api/resources/from-minio              - Create resource from MinIO selection
```

## Frontend Requirements (Preact)

### 1. Resource Management UI

- **Resource list view** with type indicators
- **Resource creation/edit forms** with filter configuration
- **MinIO browser integration** (future)

### 2. Knowledge Unit Management UI

- **Resource selection interface** for Knowledge Units
- **Filter configuration UI** for POINTER resources
- **KU file generation interface** (future)

### 3. File Browser Component (Future)

- **MinIO object browser** with folder navigation
- **Multi-select functionality** for resource creation
- **Filter preview** showing which files would be included/excluded

## Implementation Phases

### Phase 1: Core Refactoring ✅ Current Focus

- [ ] Database migration (V3__refactor_to_resources.sql)
- [ ] Java entity updates (Resource, ResourceType, etc.)
- [ ] Repository layer updates
- [ ] Basic REST API updates
- [ ] Frontend model updates

### Phase 2: MinIO Integration (Future)

- [ ] MinIO service layer
- [ ] Content fetching and processing
- [ ] Filter system implementation
- [ ] MinIO browser UI component

### Phase 3: KU Generation (Future)  

- [ ] Content unification service
- [ ] KU file generation
- [ ] Download functionality
- [ ] Database storage of generated KU content

### Phase 4: Advanced Features (Future)

- [ ] Vector embeddings for generated KU files
- [ ] Full-text search integration
- [ ] Advanced filtering options
- [ ] Batch processing capabilities

## Configuration Requirements

### Quarkus Application Properties

```properties
# MinIO Configuration
minio.prefix=${MINIO_PREFIX:http://localhost:9000/bucket/project}
minio.access-key=${MINIO_ACCESS_KEY:minioadmin}
minio.secret-key=${MINIO_SECRET_KEY:minioadmin}

# Resource Processing
resource.processing.batch-size=10
resource.processing.timeout=30s

# KU Generation
ku.generation.max-size=10MB
ku.generation.format=txt
```

## Success Criteria

### Phase 1 Completion Criteria

1. **Database migration successful**: All existing documents converted to resources
2. **API compatibility**: Existing Knowledge Unit functionality preserved
3. **Model consistency**: Resource types and relationships properly implemented
4. **Test coverage**: Unit and integration tests for new resource model

### Future Phase Success Criteria

1. **MinIO integration**: Seamless content fetching from MinIO
2. **Filter accuracy**: Filters correctly include/exclude content as specified
3. **KU generation**: Unified text files generated with proper structure
4. **Performance**: Acceptable processing times for typical content volumes

## Risk Considerations

### Technical Risks

- **Data migration complexity**: Existing documents must be preserved
- **MinIO connectivity**: Network issues during content fetching
- **Memory usage**: Large file processing may require streaming
- **Concurrency**: Multiple users processing resources simultaneously

### Mitigation Strategies

- **Incremental migration**: Phased rollout with rollback capability
- **Retry mechanisms**: Handle transient MinIO connection failures
- **Streaming processing**: Process large files in chunks
- **Queue system**: Background processing for resource resolution

## Conclusion

This refactoring transforms the Knowledge Manager into a more flexible, resource-centric system that can aggregate content from MinIO storage and generate unified Knowledge Unit files. The phased approach ensures system stability while enabling powerful new content management capabilities.
