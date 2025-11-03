-- Create promotions table
CREATE TABLE promotions (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    image_id BIGINT,
    start_date DATE,
    end_date DATE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_promotions_organizations FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    CONSTRAINT fk_promotions_media FOREIGN KEY (image_id) REFERENCES media_files(id) ON DELETE SET NULL
);

-- Create indexes
CREATE INDEX idx_promotions_organization ON promotions(organization_id);
CREATE INDEX idx_promotions_dates ON promotions(start_date, end_date);
CREATE INDEX idx_promotions_active ON promotions(is_active);

-- Add comment
COMMENT ON TABLE promotions IS 'Promotions/special offers for organizations';

