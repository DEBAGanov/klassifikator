-- Create seo_data table
CREATE TABLE seo_data (
    id BIGSERIAL PRIMARY KEY,
    landing_id BIGINT NOT NULL,
    title VARCHAR(255),
    meta_description TEXT,
    meta_keywords TEXT,
    og_title VARCHAR(255),
    og_description TEXT,
    og_image VARCHAR(500),
    schema_markup JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_seo_landings FOREIGN KEY (landing_id) REFERENCES landings(id) ON DELETE CASCADE
);

-- Create index
CREATE INDEX idx_seo_landing ON seo_data(landing_id);

-- Add comment
COMMENT ON TABLE seo_data IS 'SEO metadata for landings';

