-- Create organization_content table
CREATE TABLE organization_content (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    title VARCHAR(255),
    meta_description TEXT,
    h1 VARCHAR(255),
    about_text TEXT,
    content_data JSONB,
    version INT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_content_organizations FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE
);

-- Create index
CREATE INDEX idx_content_organization ON organization_content(organization_id);

-- Add comment
COMMENT ON TABLE organization_content IS 'Content for organization landings';

