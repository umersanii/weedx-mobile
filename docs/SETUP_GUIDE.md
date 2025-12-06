# WeedX Setup Guide

**Status**: 🚀 Deployed & Operational  
**URL**: `http://raspberrypi.mullet-bull.ts.net/weedx-backend/`

---

## Quick Start

```bash
# Deploy backend
bash scripts/deploy-backend.sh

# Setup MQTT
bash scripts/setup-mqtt.sh

# Test system
bash scripts/test-backend.sh
bash scripts/mqtt-publisher.sh batch
```

---

## Architecture

```
Robot/Script → MQTT → PHP Subscriber → MySQL → REST API → Android App
```

**Stack**: Kotlin + PHP + MySQL + MQTT + Apache on Raspberry Pi via Tailscale

---

## File Locations

⚠️ **CRITICAL**: Edit in source, deploy to production!

| Location | Purpose | Actions |
|----------|---------|---------|
| `xampp/htdocs/backend/` | Source (Git) | Edit here |
| `/var/www/html/weedx-backend/` | Production (Apache) | Never edit directly |

**Deploy after changes**: `bash scripts/deploy-backend.sh`

---

## MQTT Integration

### How It Works

1. **Robot publishes** → JSON to MQTT topics
2. **Subscriber listens** → `weedx-mqtt.service` (always running)
3. **Auto-save** → Data written to MySQL
4. **App reads** → REST API queries database

### Topics (6 Active)

| Topic | Data | Handler |
|-------|------|---------|
| `weedx/robot/status` | status, activity, user_id | `updateRobotStatus()` |
| `weedx/robot/location` | lat, lon, speed, heading | `updateRobotLocation()` |
| `weedx/robot/battery` | battery %, herbicide % | `updateBatteryLevel()` |
| `weedx/weed/detection` | weed data + optional image | `saveWeedDetection()` |
| `weedx/sensor/soil` | moisture, pH, NPK values | `saveSoilData()` |
| `weedx/alert` | type, severity, message | `saveAlert()` |

**Note**: Weather fetched via API, not MQTT.

### Publish Data

```bash
# Single command
scripts/mqtt-publisher.sh weed

# With options
scripts/mqtt-publisher.sh -u 2 status active "Scanning wheat field"

# Batch test
scripts/mqtt-publisher.sh batch

# Custom broker
scripts/mqtt-publisher.sh -h raspberrypi.mullet-bull.ts.net weed
```

### Monitor Processing

```bash
# Watch live logs
sudo journalctl -u weedx-mqtt -f

# Service status
sudo systemctl status weedx-mqtt

# Restart service
sudo systemctl restart weedx-mqtt
```

### Message Format Examples

**Robot Status**:
```json
{
  "user_id": 1,
  "status": "active",
  "activity": "Scanning field"
}
```

**Weed Detection**:
```json
{
  "user_id": 1,
  "weed_type": "Broadleaf Weed",
  "crop_type": "Wheat",
  "confidence": 92.5,
  "latitude": 31.5204,
  "longitude": 74.3587
}
```

**With Image**:
```json
{
  "user_id": 1,
  "weed_type": "Grass Weed",
  "crop_type": "Corn",
  "confidence": 88.3,
  "latitude": 31.5210,
  "longitude": 74.3590,
  "image_base64": "base64_encoded_data",
  "image_mime_type": "image/jpeg"
}
```

---

## Backend API

### Authentication

```bash
curl -X POST http://raspberrypi.mullet-bull.ts.net/weedx-backend/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@weedx.com","password":"admin123","firebaseToken":"test"}'
```

### Endpoints (51+)

| Module | Count | Examples |
|--------|-------|----------|
| Auth | 3 | `/auth/login`, `/auth/register` |
| Dashboard | 4 | `/landing`, `/robot/status` |
| Monitoring | 4 | `/monitoring/metrics`, `/monitoring/activity` |
| Weed Logs | 3 | `/weed-logs`, `/weed-logs/summary` |
| Environment | 5 | `/environment/weather`, `/environment/soil` |
| Reports | 5 | `/reports`, `/reports/trends` |
| Gallery | 5 | `/gallery`, `/gallery/upload` |
| Profile | 8 | `/profile`, `/profile/update` |
| Assistant | 2 | `/assistant/query` |

---

## Database

### Access

```bash
mysql -u root -p weedx
```

**Credentials**:
- Database: `weedx`
- User: `weedx_user`
- Password: `weedx_pass_2024`

### Tables (12)

`users`, `farms`, `user_settings`, `robot_status`, `robot_sessions`, `robot_activity_log`, `weed_detections`, `weather_data`, `weather_forecast`, `soil_data`, `alerts`, `chat_history`

### Quick Queries

```sql
-- View users
SELECT id, email FROM users;

-- Recent detections
SELECT * FROM weed_detections ORDER BY detected_at DESC LIMIT 10;

-- Robot status
SELECT * FROM robot_status;

-- Active alerts
SELECT * FROM alerts WHERE created_at > NOW() - INTERVAL 7 DAY;
```

### Reset Database

```bash
cd xampp/htdocs/backend/database
mysql -u root -p weedx < schema.sql
```

---

## Android App

### Update Base URL

Edit `app/src/main/java/com/example/weedx/utils/Constants.kt`:

```kotlin
// Tailscale (production)
const val BASE_URL = "http://raspberrypi.mullet-bull.ts.net/weedx-backend/"

// Emulator
const val BASE_URL = "http://10.0.2.2/weedx-backend/"

// Physical device (local network)
const val BASE_URL = "http://192.168.1.XXX/weedx-backend/"
```

### Build

```bash
./gradlew assembleDebug
./gradlew installDebug
```

### Test Credentials

```
Email: admin@weedx.com
Password: admin123
```

---

## Deployment

### Raspberry Pi Setup

```bash
# SSH to Pi
ssh pi@raspberrypi.mullet-bull.ts.net

# Configure Apache & MySQL
sudo bash scripts/configure-pi-apache.sh

# Deploy backend
cd weedx-mobile
bash scripts/deploy-to-pi.sh

# Import schema
mysql -u root -p weedx < /var/www/html/weedx-backend/database/schema.sql
```

### Enable Auto-start

```bash
sudo systemctl enable apache2
sudo systemctl enable mariadb
sudo systemctl enable mosquitto
sudo systemctl enable weedx-mqtt
```

---

## Troubleshooting

### Backend 404 Error

```bash
sudo systemctl status apache2
ls -la /var/www/html/weedx-backend/
sudo tail -f /var/log/apache2/error.log
```

### Database Connection Failed

```bash
sudo systemctl status mariadb
mysql -u weedx_user -pweedx_pass_2024 weedx -e "SELECT 1;"
```

### MQTT Not Processing

```bash
sudo systemctl status weedx-mqtt
sudo journalctl -u weedx-mqtt -n 50
sudo systemctl restart weedx-mqtt
```

### Pi Not Reachable

```bash
ssh pi@raspberrypi.mullet-bull.ts.net
tailscale status
sudo systemctl restart tailscaled
```

---

## Image Handling

Images stored as base64 in MySQL (`weed_detections.image_base64`).

### Upload Image

```bash
# Single upload
bash scripts/upload-image.sh path/to/image.jpg \
  --weed-type="Broadleaf Weed" \
  --confidence=95 \
  --user-id=1

# Batch upload directory
bash scripts/batch-upload-images.sh path/to/images/ \
  --weed-type="Grass Weed" \
  --confidence=85
```

### Supported Formats

JPEG, PNG, GIF, WebP (max 10MB, recommended 1-2MB)

---

## Implementation Status

| Component | Status |
|-----------|--------|
| Backend API | ✅ 100% |
| Database Schema | ✅ 100% |
| MQTT Integration | ✅ 100% |
| Android UI | ✅ 100% |
| Android Data Layer | ✅ 100% |
| Android Integration | 🔄 40% |
| Testing | ⏳ 0% |

---

## Common Commands

```bash
# Deploy backend
bash scripts/deploy-backend.sh

# Test backend
bash scripts/test-backend.sh

# Setup MQTT
bash scripts/setup-mqtt.sh

# Publish MQTT data
bash scripts/mqtt-publisher.sh batch

# Watch MQTT logs
sudo journalctl -u weedx-mqtt -f

# Restart services
sudo systemctl restart apache2
sudo systemctl restart weedx-mqtt

# View Apache logs
sudo tail -f /var/log/apache2/error.log

# Build Android
./gradlew assembleDebug
```

---

## Support

- Backend Source: `xampp/htdocs/backend/`
- Production: `/var/www/html/weedx-backend/`
- Scripts: `scripts/`
- Docs: `docs/`
