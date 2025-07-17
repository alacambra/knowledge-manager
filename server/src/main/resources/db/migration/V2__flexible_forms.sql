CREATE TABLE document_templates (
 id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
 name text NOT NULL,
 category text,
 form_schema JSONB NOT NULL,
 created_at TIMESTAMP DEFAULT NOW() NOT NULL,
 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

SELECT
 create_updated_at_trigger('document_templates');