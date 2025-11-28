-- Update Gallery Images SQL Script
-- This script clears base64 encoded images and sets proper file paths
-- Run this after deploying the backend to fix image display issues

USE weedx;

-- Clear existing base64 encoded sample images
-- The sample data in schema.sql uses base64 strings which won't display properly
UPDATE weed_detections 
SET image_path = NULL 
WHERE image_path LIKE 'data:image%';

-- Option 1: Set NULL for entries without actual images (cleaner approach)
-- The app will show placeholder icons for missing images

-- Option 2: Insert sample detections with placeholder paths
-- Note: You'll need to place actual sample images in the uploads/gallery/ folder
-- Or upload them via the API endpoints

-- To add sample image files, either:
-- 1. Upload images through the POST /gallery API endpoint
-- 2. Manually copy image files to /uploads/gallery/ and run:
/*
UPDATE weed_detections SET image_path = '/uploads/gallery/sample_broadleaf_1.jpg' WHERE id = 1;
UPDATE weed_detections SET image_path = '/uploads/gallery/sample_grass_1.jpg' WHERE id = 2;
UPDATE weed_detections SET image_path = '/uploads/gallery/sample_broadleaf_2.jpg' WHERE id = 3;
UPDATE weed_detections SET image_path = '/uploads/gallery/sample_sedge_1.jpg' WHERE id = 4;
UPDATE weed_detections SET image_path = '/uploads/gallery/sample_broadleaf_3.jpg' WHERE id = 5;
UPDATE weed_detections SET image_path = '/uploads/gallery/sample_grass_2.jpg' WHERE id = 6;
*/

COMMIT;
