-- Update google_sheets_sync table to match entity
ALTER TABLE google_sheets_sync 
    ADD COLUMN IF NOT EXISTS spreadsheet_id VARCHAR(100),
    ADD COLUMN IF NOT EXISTS sheet_name VARCHAR(100),
    ADD COLUMN IF NOT EXISTS sync_interval_minutes INTEGER,
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT true,
    ADD COLUMN IF NOT EXISTS last_sync_status VARCHAR(255),
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

-- Migrate data from old columns to new columns
UPDATE google_sheets_sync 
SET spreadsheet_id = sheet_id,
    last_sync_status = sync_status
WHERE spreadsheet_id IS NULL;

-- Drop old columns
ALTER TABLE google_sheets_sync 
    DROP COLUMN IF EXISTS sheet_id,
    DROP COLUMN IF EXISTS sync_status,
    DROP COLUMN IF EXISTS error_message;

-- Add unique constraint
ALTER TABLE google_sheets_sync 
    ADD CONSTRAINT uq_google_sheets_sync_organization UNIQUE (organization_id);

-- Update comment
COMMENT ON TABLE google_sheets_sync IS 'Google Sheets synchronization configuration and tracking';

