-- Create landings table
CREATE TABLE landings (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    domain VARCHAR(255) UNIQUE NOT NULL,
    subdomain VARCHAR(255) UNIQUE NOT NULL,
    template_id BIGINT,
    status VARCHAR(50) DEFAULT 'DRAFT',
    ssl_enabled BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMP,
    CONSTRAINT fk_landings_organizations FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    CONSTRAINT fk_landings_templates FOREIGN KEY (template_id) REFERENCES templates(id) ON DELETE SET NULL
);

-- Create indexes
CREATE INDEX idx_landings_domain ON landings(domain);
CREATE INDEX idx_landings_organization ON landings(organization_id);
CREATE INDEX idx_landings_status ON landings(status);

-- Add comment
COMMENT ON TABLE landings IS 'Landing pages for organizations';

