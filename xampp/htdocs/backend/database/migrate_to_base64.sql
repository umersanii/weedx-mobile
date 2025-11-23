-- Migration Script: Convert to Base64 Image Storage
-- This script updates existing database to support base64 image storage

USE weedx;

-- Backup existing data (optional but recommended)
-- CREATE TABLE weed_detections_backup AS SELECT * FROM weed_detections;

-- Add new columns to weed_detections if they don't exist
ALTER TABLE weed_detections 
ADD COLUMN IF NOT EXISTS user_id INT UNSIGNED NULL COMMENT 'User who owns this detection' AFTER id,
ADD COLUMN IF NOT EXISTS image_base64 LONGTEXT NULL COMMENT 'Base64 encoded image data' AFTER longitude,
ADD COLUMN IF NOT EXISTS image_mime_type VARCHAR(50) NULL COMMENT 'Image MIME type' AFTER image_base64;

-- Add foreign key constraint if it doesn't exist
SET @constraint_exists = (
    SELECT COUNT(*)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = 'weedx'
    AND TABLE_NAME = 'weed_detections'
    AND CONSTRAINT_NAME = 'weed_detections_ibfk_1'
);

SET @sql = IF(@constraint_exists = 0,
    'ALTER TABLE weed_detections ADD FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL',
    'SELECT "Foreign key already exists"'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add index for user_id if it doesn't exist
ALTER TABLE weed_detections ADD INDEX IF NOT EXISTS idx_user_id (user_id);

-- Create user_images table if it doesn't exist
CREATE TABLE IF NOT EXISTS user_images (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id INT UNSIGNED NOT NULL,
    title VARCHAR(255) NULL,
    description TEXT NULL,
    image_base64 LONGTEXT NOT NULL COMMENT 'Base64 encoded image data',
    image_mime_type VARCHAR(50) NOT NULL COMMENT 'Image MIME type',
    category VARCHAR(50) NULL COMMENT 'Image category (avatar, gallery, weed, etc)',
    metadata TEXT NULL COMMENT 'JSON metadata',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_category (category),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Optional: You can keep the old image_path column for backward compatibility
-- or remove it after migrating all data:
-- ALTER TABLE weed_detections DROP COLUMN image_path;

SELECT 'Migration completed successfully!' AS status;
SELECT 
    COUNT(*) as total_detections,
    COUNT(image_base64) as with_base64_images,
    COUNT(image_path) as with_file_paths
FROM weed_detections;
