DELETE FROM flyway_schema_history;

INSERT INTO flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success) VALUES
(1, '001', 'create organizations table', 'SQL', 'V001__create_organizations_table.sql', NULL, 'klassifikator', NOW(), 100, TRUE),
(2, '002', 'create templates table', 'SQL', 'V002__create_templates_table.sql', NULL, 'klassifikator', NOW(), 100, TRUE),
(3, '003', 'create landings table', 'SQL', 'V003__create_landings_table.sql', NULL, 'klassifikator', NOW(), 100, TRUE),
(4, '004', 'create organization content table', 'SQL', 'V004__create_organization_content_table.sql', NULL, 'klassifikator', NOW(), 100, TRUE),
(5, '005', 'create content versions table', 'SQL', 'V005__create_content_versions_table.sql', NULL, 'klassifikator', NOW(), 100, TRUE),
(6, '006', 'create media files table', 'SQL', 'V006__create_media_files_table.sql', NULL, 'klassifikator', NOW(), 100, TRUE),
(7, '007', 'create products table', 'SQL', 'V007__create_products_table.sql', NULL, 'klassifikator', NOW(), 100, TRUE),
(8, '008', 'create promotions table', 'SQL', 'V008__create_promotions_table.sql', NULL, 'klassifikator', NOW(), 100, TRUE),
(9, '009', 'create google sheets sync table', 'SQL', 'V009__create_google_sheets_sync_table.sql', NULL, 'klassifikator', NOW(), 100, TRUE),
(10, '010', 'create seo data table', 'SQL', 'V010__create_seo_data_table.sql', NULL, 'klassifikator', NOW(), 100, TRUE),
(11, '011', 'create orders table', 'SQL', 'V011__create_orders_table.sql', NULL, 'klassifikator', NOW(), 100, TRUE),
(12, '012', 'update google sheets sync table', 'SQL', 'V012__update_google_sheets_sync_table.sql', NULL, 'klassifikator', NOW(), 100, TRUE),
(13, '013', 'update orders table add comment and landing id', 'SQL', 'V013__update_orders_table_add_comment_and_landing_id.sql', NULL, 'klassifikator', NOW(), 100, TRUE),
(14, '014', 'insert default templates', 'SQL', 'V014__insert_default_templates.sql', NULL, 'klassifikator', NOW(), 100, TRUE);

SELECT COUNT(*) as migrations_count FROM flyway_schema_history;

