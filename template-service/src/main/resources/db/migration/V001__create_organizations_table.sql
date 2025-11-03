-- Create organizations table
CREATE TABLE organizations (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(100),
    type VARCHAR(100),
    address TEXT,
    phone VARCHAR(50),
    website VARCHAR(255),
    working_hours TEXT,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    google_sheet_id VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create index for faster lookups
CREATE INDEX idx_organizations_status ON organizations(status);
CREATE INDEX idx_organizations_category ON organizations(category);

-- Add comment
COMMENT ON TABLE organizations IS 'Organizations/businesses for which landings are created';

