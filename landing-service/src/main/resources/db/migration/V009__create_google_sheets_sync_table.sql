-- Create google_sheets_sync table
CREATE TABLE google_sheets_sync (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT,
    sheet_id VARCHAR(255) NOT NULL,
    last_sync_at TIMESTAMP,
    sync_status VARCHAR(50),
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sync_organizations FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE
);

-- Create index
CREATE INDEX idx_sync_organization ON google_sheets_sync(organization_id);
CREATE INDEX idx_sync_status ON google_sheets_sync(sync_status);

-- Add comment
COMMENT ON TABLE google_sheets_sync IS 'Google Sheets synchronization tracking';

