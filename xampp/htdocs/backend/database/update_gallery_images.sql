-- Update existing weed detections with base64 encoded placeholder images
-- These are 1x1 pixel PNG images in different colors for testing

UPDATE weed_detections SET image_path = 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==' WHERE id = 1;
UPDATE weed_detections SET image_path = 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==' WHERE id = 2;
UPDATE weed_detections SET image_path = 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8CwHwAFBQIAX8jx0gAAAABJRU5ErkJggg==' WHERE id = 3;
UPDATE weed_detections SET image_path = 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+P+/HgAFhAJ/wlseKgAAAABJRU5ErkJggg==' WHERE id = 4;

-- Insert additional sample data if doesn't exist
INSERT INTO weed_detections (weed_type, crop_type, confidence, latitude, longitude, image_path, detected_at)
SELECT 'Broadleaf Weed', 'Wheat', 91.2, 31.5200, 74.3585, 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==', DATE_SUB(NOW(), INTERVAL 5 HOUR)
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM weed_detections WHERE id = 5);

INSERT INTO weed_detections (weed_type, crop_type, confidence, latitude, longitude, image_path, detected_at)
SELECT 'Grass Weed', 'Corn', 87.6, 31.5208, 74.3592, 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPj/HwADBwIAMCbHYQAAAABJRU5ErkJggg==', DATE_SUB(NOW(), INTERVAL 7 HOUR)
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM weed_detections WHERE id = 6);
