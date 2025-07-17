CREATE EXTENSION IF NOT EXISTS vector;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE document (
 id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
 title TEXT NOT NULL,
 content TEXT NOT NULL,
 embedding vector(384),
 metadata JSONB,
 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create index for vector similarity search
CREATE INDEX ON document USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);

-- Create index for metadata queries
CREATE INDEX ON document USING GIN (metadata);

-- Create index for text search
CREATE INDEX ON document USING GIN (to_tsvector('english', title || ' ' || content));

-- Function for updating timestamps
CREATE OR REPLACE FUNCTION update_updated_at_column() RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Helper function to create updated_at triggers
CREATE OR REPLACE FUNCTION create_updated_at_trigger(table_name TEXT) RETURNS VOID AS $$
BEGIN
    EXECUTE format(
        'CREATE TRIGGER update_%I_updated_at
            BEFORE UPDATE ON %I
            FOR EACH ROW
            EXECUTE FUNCTION update_updated_at_column()',
        table_name,
        table_name
    );
END;
$$ language 'plpgsql';

-- Knowledge units table
CREATE TABLE knowledge_unit (
 id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
 name VARCHAR(255) NOT NULL,
 description TEXT,
 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create triggers using the helper function
SELECT
 create_updated_at_trigger('document');

SELECT
 create_updated_at_trigger('knowledge_unit');

CREATE TABLE knowledge_unit_document (
 id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
 knowledge_unit_id UUID NOT NULL REFERENCES knowledge_unit(id) ON DELETE CASCADE,
 document_id UUID NOT NULL REFERENCES document(id) ON DELETE CASCADE,
 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 UNIQUE(knowledge_unit_id, document_id)
);