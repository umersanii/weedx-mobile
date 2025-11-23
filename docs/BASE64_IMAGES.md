# Base64 Image Storage System

## Overview

Images in WeedX are stored as base64-encoded strings directly in the MySQL database. This approach provides better data portability, simplified backups, and user-specific image management.

## Quick Start

### Upload Images via Terminal

```bash
# Upload single image
php xampp/htdocs/backend/utils/image-upload.php data/images/weed.jpg \
  --weed-type="Broadleaf Weed" \
  --confidence=95 \
  --user-id=1

# Batch upload directory
./scripts/batch-upload-images.sh xampp/htdocs/backend/data/images/ \
  --weed-type="Sample Weed" \
  --confidence=90 \
  --user-id=1
```

### Upload via API

```bash
curl -X POST http://localhost/weedx-backend/gallery \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "image=@weed.jpg" \
  -F "weed_type=Broadleaf Weed" \
  -F "confidence=95"
```

## Database Schema

### Modified Tables

#### weed_detections

```sql
CREATE TABLE weed_detections (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id INT UNSIGNED NULL,
    weed_type VARCHAR(100) NOT NULL,
    crop_type VARCHAR(100) NULL,
    confidence DECIMAL(5, 2) DEFAULT 0,
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    image_base64 LONGTEXT NULL,              -- Base64 encoded image
    image_mime_type VARCHAR(50) NULL,         -- MIME type
    treated BOOLEAN DEFAULT FALSE,
    detected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);
```

#### user_images (New)

```sql
CREATE TABLE user_images (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id INT UNSIGNED NOT NULL,
    title VARCHAR(255) NULL,
    description TEXT NULL,
    image_base64 LONGTEXT NOT NULL,
    image_mime_type VARCHAR(50) NOT NULL,
    category VARCHAR(50) NULL,
    metadata TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

## Migration

### Update Existing Database

```bash
mysql -u weedx_user -pweedx_pass_2024 weedx < xampp/htdocs/backend/database/migrate_to_base64.sql
```

### Fresh Installation

```bash
mysql -u root -p < xampp/htdocs/backend/database/schema.sql
```

## API Endpoints

### List Images

**GET** `/gallery?limit=50&offset=0`

Returns images with base64 data for the authenticated user.

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 5,
      "image_base64": "iVBORw0KGgoAAAANS...",
      "image_mime_type": "image/png",
      "weed_type": "Sample Weed",
      "confidence": 90.0,
      "location": {
        "latitude": 31.5204,
        "longitude": 74.3587
      },
      "captured_at": "2025-11-23 10:30:00"
    }
  ]
}
```

### View Single Image

**GET** `/gallery/:id`

### Upload Image

**POST** `/gallery`

**Form Data:**
- `image` (file) - Required
- `weed_type` (string) - Default: "Unknown"
- `crop_type` (string) - Optional
- `confidence` (number) - Default: 0
- `latitude` (number) - Default: 0
- `longitude` (number) - Default: 0

## Terminal Upload Utility

### Options

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `--user-id` | Integer | 1 | User who owns the image |
| `--weed-type` | String | "Unknown" | Type of weed |
| `--crop-type` | String | null | Type of crop |
| `--confidence` | Number | 0 | Detection confidence (0-100) |
| `--latitude` | Number | 0 | GPS latitude |
| `--longitude` | Number | 0 | GPS longitude |
| `--table` | String | weed_detections | Target table |
| `--category` | String | gallery | Image category |
| `--title` | String | null | Image title |
| `--description` | String | null | Description |

### Examples

**Basic upload:**
```bash
php utils/image-upload.php photo.jpg --weed-type="Dandelion"
```

**With GPS coordinates:**
```bash
php utils/image-upload.php photo.jpg \
  --weed-type="Grass Weed" \
  --latitude=31.5204 \
  --longitude=74.3587 \
  --confidence=92
```

**Upload to user_images table:**
```bash
php utils/image-upload.php avatar.png \
  --table=user_images \
  --category=avatar \
  --user-id=1
```

**Batch upload:**
```bash
./scripts/batch-upload-images.sh /path/to/images/ \
  --weed-type="Mixed Weeds" \
  --confidence=85
```

## Android Integration

### Display Base64 Images

```kotlin
import android.util.Base64
import android.graphics.BitmapFactory
import coil.load

// Method 1: Decode and display
val imageBytes = Base64.decode(imageBase64, Base64.DEFAULT)
val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
imageView.setImageBitmap(bitmap)

// Method 2: Use data URI with Coil
val dataUri = "data:${mimeType};base64,${imageBase64}"
imageView.load(dataUri) {
    crossfade(true)
    placeholder(R.drawable.placeholder)
    error(R.drawable.error_image)
}
```

### Response Models

```kotlin
data class GalleryImage(
    val id: Int,
    val image_base64: String,
    val image_mime_type: String,
    val weed_type: String,
    val confidence: Float,
    val location: Location,
    val captured_at: String
)

data class Location(
    val latitude: Double,
    val longitude: Double
)
```

## Performance Considerations

### Storage Size
- Original image: 100KB
- Base64 encoded: ~133KB (+33% overhead)
- LONGTEXT max: 4GB per field

### Limits
- **Max file size:** 10MB (before encoding)
- **Recommended:** 1-2MB for optimal performance
- **MySQL setting:** `max_allowed_packet = 64M`

### Optimization Tips

1. **Compress images before upload:**
   ```bash
   convert input.jpg -quality 80 -resize 1920x1080\> output.jpg
   ```

2. **Use pagination:**
   ```php
   $limit = min($_GET['limit'] ?? 50, 100);
   ```

3. **Configure MySQL:**
   ```ini
   # my.cnf
   max_allowed_packet = 64M
   query_cache_size = 256M
   ```

## Supported Formats

✅ JPEG/JPG (`image/jpeg`)
✅ PNG (`image/png`)
✅ GIF (`image/gif`)
✅ WebP (`image/webp`)

## Database Maintenance

### Check Storage Size

```sql
SELECT 
    table_name,
    ROUND((data_length + index_length) / 1024 / 1024, 2) AS 'Size (MB)'
FROM information_schema.tables
WHERE table_schema = 'weedx'
ORDER BY (data_length + index_length) DESC;
```

### Count Images

```sql
SELECT 
    COUNT(*) as total_images,
    ROUND(SUM(LENGTH(image_base64)) / 1024 / 1024, 2) as total_size_mb
FROM weed_detections
WHERE image_base64 IS NOT NULL;
```

### Images by User

```sql
SELECT 
    u.name,
    COUNT(wd.id) as image_count,
    ROUND(SUM(LENGTH(wd.image_base64)) / 1024 / 1024, 2) as size_mb
FROM users u
LEFT JOIN weed_detections wd ON u.id = wd.user_id
WHERE wd.image_base64 IS NOT NULL
GROUP BY u.id;
```

## Backup & Recovery

### Full Backup

```bash
mysqldump -u root -p weedx > weedx_backup_$(date +%Y%m%d).sql
```

### Restore

```bash
mysql -u root -p weedx < weedx_backup_20251123.sql
```

## Troubleshooting

### Error: "MySQL packet too large"

```ini
# my.cnf
[mysqld]
max_allowed_packet = 64M
```

### Error: "Out of memory"

```ini
# php.ini
memory_limit = 512M
```

### Error: "Failed to decode base64"

Check that base64 string is valid:
```kotlin
try {
    val decoded = Base64.decode(base64String, Base64.DEFAULT)
} catch (e: IllegalArgumentException) {
    Log.e("Image", "Invalid base64: ${e.message}")
}
```

## Advantages

✅ **Portability** - Database backups include all images
✅ **Atomicity** - Single transaction for data + image
✅ **Simplicity** - No file system management
✅ **Security** - Database-level access control
✅ **User-specific** - Easy filtering by user_id

## Disadvantages

❌ **Storage overhead** - 33% larger than binary
❌ **Memory usage** - Entire image loaded into memory
❌ **Query performance** - Large TEXT fields can slow queries
❌ **Backup size** - Larger database dumps

## When to Use Base64 Storage

**Use when:**
- Dataset is manageable (<10,000 images)
- Images are small-medium (<5MB)
- Portability is critical
- Simplified architecture is desired

**Use file storage when:**
- Large collections (>10,000 images)
- Very large images (>10MB)
- CDN integration required
- High-performance queries needed

## Scripts Location

- **Upload utility:** `xampp/htdocs/backend/utils/image-upload.php`
- **Bash wrapper:** `scripts/upload-image.sh`
- **Batch upload:** `scripts/batch-upload-images.sh`
- **Migration SQL:** `xampp/htdocs/backend/database/migrate_to_base64.sql`

## Related Documentation

- [API Endpoints](api_endpoints.md) - Complete API reference
- [Image Upload Guide](IMAGE_UPLOAD_GUIDE.md) - Detailed upload instructions
- [Backend Summary](BACKEND_SUMMARY.md) - Backend overview

## Summary

The base64 image storage system provides an integrated, portable solution for image management in WeedX. Use the terminal utilities for easy uploads and follow performance recommendations for optimal operation.
