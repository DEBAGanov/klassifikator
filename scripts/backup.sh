#!/bin/bash

# =============================================================================
# Database Backup Script
# =============================================================================

set -e

# Load environment variables
source .env

BACKUP_DIR="./backups"
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="${BACKUP_DIR}/klassifikator_${DATE}.sql"

mkdir -p "$BACKUP_DIR"

echo "📦 Creating database backup..."

docker exec klassifikator-postgres pg_dump -U "${DB_USER}" "${DB_NAME}" > "$BACKUP_FILE"

echo "✅ Backup created: $BACKUP_FILE"
echo "📊 Size: $(du -h "$BACKUP_FILE" | cut -f1)"

# Compress backup
gzip "$BACKUP_FILE"
echo "✅ Compressed: ${BACKUP_FILE}.gz"

# Keep only last 7 days of backups
find "$BACKUP_DIR" -name "*.sql.gz" -mtime +7 -delete
echo "🗑️  Old backups cleaned up"

