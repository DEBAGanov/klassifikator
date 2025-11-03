-- Create content_versions table for versioning
CREATE TABLE content_versions (
    id BIGSERIAL PRIMARY KEY,
    organization_content_id BIGINT NOT NULL,
    version INT NOT NULL,
    content_data JSONB,
    changed_by VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_versions_content FOREIGN KEY (organization_content_id) REFERENCES organization_content(id) ON DELETE CASCADE
);

-- Create index
CREATE INDEX idx_versions_content ON content_versions(organization_content_id);
CREATE INDEX idx_versions_version ON content_versions(version);

-- Add comment
COMMENT ON TABLE content_versions IS 'Version history of organization content';

