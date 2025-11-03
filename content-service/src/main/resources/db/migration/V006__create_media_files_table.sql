-- Create media_files table
CREATE TABLE media_files (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    filename VARCHAR(255) NOT NULL,
    original_filename VARCHAR(255),
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT,
    mime_type VARCHAR(100),
    category VARCHAR(50),
    width INT,
    height INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_media_organizations FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_media_organization ON media_files(organization_id);
CREATE INDEX idx_media_category ON media_files(category);

-- Add comment
COMMENT ON TABLE media_files IS 'Media files stored in S3';

