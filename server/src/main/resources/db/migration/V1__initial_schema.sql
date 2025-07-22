CREATE EXTENSION IF NOT EXISTS vector;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Knowledge units table
CREATE TABLE knowledge_unit (
 id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
 name TEXT NOT NULL,
 description TEXT,
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Knowledge unit resources table
CREATE TABLE knowledge_unit_resource (
 id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
 name TEXT NOT NULL,
 description TEXT,
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Document groups table
CREATE TABLE document_group (
 id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
 uri TEXT NOT NULL,
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Documents table
CREATE TABLE document (
 id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
 title TEXT NOT NULL,
 file_name TEXT NOT NULL,
 uri TEXT,
 content TEXT NOT NULL,
 embedding vector(384),
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- N:M relationship: Knowledge Unit contains one or more Resources
CREATE TABLE knowledge_unit_contained_resource (
 id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
 knowledge_unit_id UUID NOT NULL REFERENCES knowledge_unit(id) ON DELETE CASCADE,
 knowledge_unit_resource_id UUID NOT NULL REFERENCES knowledge_unit_resource(id) ON DELETE CASCADE,
 is_default BOOLEAN NOT NULL DEFAULT FALSE,
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 UNIQUE(knowledge_unit_id, knowledge_unit_resource_id)
);

-- N:M relationship: Resource is composed from one or more Document Groups
CREATE TABLE resource_contained_document_group (
 id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
 knowledge_unit_resource_id UUID NOT NULL REFERENCES knowledge_unit_resource(id) ON DELETE CASCADE,
 document_group_id UUID NOT NULL REFERENCES document_group(id) ON DELETE CASCADE,
 is_default BOOLEAN NOT NULL DEFAULT FALSE,
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 UNIQUE(knowledge_unit_resource_id, document_group_id)
);

-- N:M relationship: Document Group contains one or more Documents
CREATE TABLE document_group_document (
 id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
 document_group_id UUID NOT NULL REFERENCES document_group(id) ON DELETE CASCADE,
 document_id UUID NOT NULL REFERENCES document(id) ON DELETE CASCADE,
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 UNIQUE(document_group_id, document_id)
);

-- Indexes for vector similarity search
CREATE INDEX ON document USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);

-- Indexes for text search
CREATE INDEX ON document USING GIN (to_tsvector('english', title || ' ' || content));

-- Indexes for foreign key relationships (performance)
CREATE INDEX ON knowledge_unit_contained_resource (knowledge_unit_id);

CREATE INDEX ON knowledge_unit_contained_resource (knowledge_unit_resource_id);

CREATE INDEX ON resource_contained_document_group (knowledge_unit_resource_id);

CREATE INDEX ON resource_contained_document_group (document_group_id);

CREATE INDEX ON document_group_document (document_group_id);

CREATE INDEX ON document_group_document (document_id);

-- Function for updating timestamps
CREATE FUNCTION update_updated_at_column() RETURNS TRIGGER AS $$
BEGIN 
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Helper function to create updated_at triggers
CREATE FUNCTION create_updated_at_trigger(table_name TEXT) RETURNS VOID AS $$
BEGIN 
    EXECUTE format(
        'CREATE TRIGGER update_%I_updated_at BEFORE UPDATE ON %I FOR EACH ROW EXECUTE FUNCTION update_updated_at_column()',
        table_name,
        table_name
    );
END;
$$ language 'plpgsql';

-- Create triggers using the helper function
SELECT create_updated_at_trigger('knowledge_unit');
SELECT create_updated_at_trigger('knowledge_unit_resource');
SELECT create_updated_at_trigger('document_group');
SELECT create_updated_at_trigger('document');
SELECT create_updated_at_trigger('knowledge_unit_contained_resource');
SELECT create_updated_at_trigger('resource_contained_document_group');
SELECT create_updated_at_trigger('document_group_document');


-- Add EXCLUDE constraints to ensure only one default per KU/Resource
ALTER TABLE knowledge_unit_contained_resource 
ADD CONSTRAINT uk_knowledge_unit_default_resource 
EXCLUDE (knowledge_unit_id WITH =) WHERE (is_default = true);

ALTER TABLE resource_contained_document_group 
ADD CONSTRAINT uk_resource_default_document_group 
EXCLUDE (knowledge_unit_resource_id WITH =) WHERE (is_default = true);