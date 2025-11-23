# Quick Start: Image Upload Utility

## Overview

Upload images to the WeedX database using simple terminal commands. Images are automatically converted to base64 and stored in the database.

## Prerequisites

- PHP installed
- MySQL database configured
- WeedX backend set up

## Basic Usage

### 1. Upload Single Image

```bash
cd /path/to/weedx-mobile/xampp/htdocs/backend

# Basic upload
php utils/image-upload.php data/images/weed1.jpg

# With metadata
php utils/image-upload.php data/images/weed1.jpg \
  --weed-type="Broadleaf Weed" \
  --crop-type="Wheat" \
  --confidence=95 \
  --latitude=31.5204 \
  --longitude=74.3587
```

### 2. Using Bash Wrapper (Easier)

```bash
cd /path/to/weedx-mobile

# Upload with options
./scripts/upload-image.sh xampp/htdocs/backend/data/images/weed1.jpg \
  --weed-type="Grass Weed" \
  --confidence=88
```

### 3. Batch Upload Directory

```bash
# Upload all images in a folder
./scripts/batch-upload-images.sh xampp/htdocs/backend/data/images/ \
  --weed-type="Broadleaf Weed" \
  --confidence=85 \
  --user-id=1
```

## Common Examples

### Example 1: Upload Weed Detection Photo

```bash
php utils/image-upload.php photos/weed_detection.jpg \
  --user-id=1 \
  --weed-type="Dandelion" \
  --crop-type="Corn" \
  --confidence=92 \
  --latitude=40.7128 \
  --longitude=-74.0060
```

**Output:**
```
ℹ️  INFO: Reading image: photos/weed_detection.jpg
ℹ️  INFO: MIME Type: image/jpeg
ℹ️  INFO: File Size: 245.67KB
ℹ️  INFO: Base64 Size: 327.23KB
ℹ️  INFO: Connecting to database...
ℹ️  INFO: Uploading to weed_detections table...
✅ SUCCESS: Image uploaded successfully!

Details:
  ID: 5
  Table: weed_detections
  User ID: 1
  MIME Type: image/jpeg
  Original Size: 245.67KB
  Base64 Size: 327.23KB
  Weed Type: Dandelion
  Confidence: 92%
```

### Example 2: Upload User Avatar

```bash
php utils/image-upload.php user_avatar.png \
  --table=user_images \
  --user-id=1 \
  --category=avatar \
  --title="John's Avatar" \
  --description="Profile picture"
```

### Example 3: Batch Upload from Robot Camera

```bash
# Upload all images captured today
./scripts/batch-upload-images.sh /robot/captures/2025-11-23/ \
  --weed-type="Mixed Weeds" \
  --confidence=85 \
  --user-id=1
```

**Output:**
```
Scanning directory: /robot/captures/2025-11-23/
Found 47 image(s)

Processing: capture_001.jpg
✅ SUCCESS: Image uploaded successfully!

Processing: capture_002.jpg
✅ SUCCESS: Image uploaded successfully!

...

==========================================
Upload Complete
Total: 47
Success: 47
Failed: 0
==========================================
```

## Command Reference

### Options for weed_detections table (default)

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `--user-id` | Integer | 1 | User who owns the detection |
| `--weed-type` | String | "Unknown" | Type of weed detected |
| `--crop-type` | String | null | Type of crop in field |
| `--confidence` | Number | 0 | Detection confidence (0-100) |
| `--latitude` | Number | 0 | GPS latitude coordinate |
| `--longitude` | Number | 0 | GPS longitude coordinate |

### Options for user_images table

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `--table` | String | weed_detections | Use "user_images" for this table |
| `--user-id` | Integer | 1 | User who owns the image |
| `--category` | String | "gallery" | Image category (avatar, gallery, weed) |
| `--title` | String | null | Image title |
| `--description` | String | null | Image description |

## Supported Image Formats

✅ JPEG/JPG (`image/jpeg`)
✅ PNG (`image/png`)
✅ GIF (`image/gif`)
✅ WebP (`image/webp`)

## File Size Limits

- **Maximum file size:** 10MB (before encoding)
- **Recommended size:** 1-2MB for best performance
- **Base64 overhead:** ~33% increase in size

## Tips & Tricks

### 1. Compress Images First

```bash
# Using ImageMagick
convert large_image.jpg -quality 80 -resize 1920x1080\> compressed.jpg

# Then upload
php utils/image-upload.php compressed.jpg --weed-type="Broadleaf"
```

### 2. Upload from Script/Cron Job

```bash
#!/bin/bash
# upload_daily_captures.sh

DATE=$(date +%Y-%m-%d)
CAPTURES_DIR="/robot/captures/$DATE"

if [ -d "$CAPTURES_DIR" ]; then
    /path/to/scripts/batch-upload-images.sh "$CAPTURES_DIR" \
        --weed-type="Daily Detection" \
        --user-id=1 \
        --confidence=80
fi
```

### 3. Check Database After Upload

```bash
# Count uploaded images
mysql -u root -p weedx -e "SELECT COUNT(*) FROM weed_detections WHERE image_base64 IS NOT NULL"

# View recent uploads
mysql -u root -p weedx -e "SELECT id, weed_type, confidence, detected_at FROM weed_detections ORDER BY detected_at DESC LIMIT 10"
```

### 4. Export Images Back to Files

```bash
cd xampp/htdocs/backend

# Create export directory
mkdir -p exports

# Run export script (create this if needed)
mysql -u root -p weedx -e "
SELECT 
    CONCAT('weed_', id, '.jpg') as filename,
    image_base64
FROM weed_detections 
WHERE image_base64 IS NOT NULL
INTO OUTFILE '/tmp/images_export.csv'
FIELDS TERMINATED BY ','
ENCLOSED BY '\"'
LINES TERMINATED BY '\n'
"
```

## Troubleshooting

### Error: "Image file not found"

**Solution:** Use absolute path or verify file exists:
```bash
ls -la data/images/weed.jpg  # Check file exists
php utils/image-upload.php "$(pwd)/data/images/weed.jpg"  # Use absolute path
```

### Error: "Failed to connect to database"

**Solution:** Check database configuration:
```bash
# Test MySQL connection
mysql -u root -p weedx -e "SELECT 1"

# Verify config/database.php settings
```

### Error: "File too large"

**Solution:** Compress image first:
```bash
# Compress to under 10MB
convert large.jpg -quality 70 -resize 2000x2000\> compressed.jpg
```

### Error: "Invalid file type"

**Solution:** Convert to supported format:
```bash
# Convert to JPEG
convert image.bmp output.jpg

# Convert to PNG
convert image.tiff output.png
```

## Performance Notes

- Uploading 50 images (1MB each): ~30-60 seconds
- Database grows by ~33% more than original file size
- Consider batch uploads during off-peak hours
- Monitor MySQL `max_allowed_packet` setting for large images

## Next Steps

1. **Verify uploads:** Check images in gallery API endpoint
2. **Test in Android:** Fetch and display base64 images
3. **Set up automation:** Create cron jobs for regular uploads
4. **Monitor storage:** Track database size growth

## Help & Support

For detailed documentation, see:
- [BASE64_IMAGES.md](../xampp/htdocs/backend/BASE64_IMAGES.md) - Complete guide
- [api_endpoints.md](api_endpoints.md) - API reference
- [BACKEND_SUMMARY.md](BACKEND_SUMMARY.md) - Backend overview

For help with the upload utility:
```bash
php utils/image-upload.php --help
./scripts/upload-image.sh
./scripts/batch-upload-images.sh
```
