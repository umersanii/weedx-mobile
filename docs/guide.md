# WeedX Complete Guide

This document consolidates all guides for the WeedX project including backend setup, deployment, database management, image handling, implementation status, and use case flows.

---

## Table of Contents

1. [Backend Summary](#backend-summary)
2. [Tailscale Deployment](#tailscale-deployment)
3. [Database Guide](#database-guide)
4. [Image Handling](#image-handling)
5. [Image Upload Guide](#image-upload-guide)
6. [Implementation Status](#implementation-status)
7. [Use Case Flows](#use-case-flows)

---

# Backend Summary

**Status**: 🚀 **DEPLOYED & OPERATIONAL**  
**Date**: November 28, 2025  
**URL**: `http://raspberrypi.mullet-bull.ts.net/weedx-backend/`  
**Location**: `/var/www/html/weedx-backend/` on Raspberry Pi

## What Was Built

A complete PHP REST API backend for the WeedX precision farming system with:

### Core Infrastructure
- **Router**: `index.php` - Handles all API routing
- **Database Config**: MySQL connection with PDO
- **Authentication**: JWT token-based auth system
- **Response Helper**: Standardized JSON responses
- **CORS Support**: Cross-origin requests enabled

### API Endpoints (51+ endpoints)

| Module | Endpoints | Description |
|--------|-----------|-------------|
| Authentication | 3 | Login, logout, refresh token |
| Dashboard/Landing | 4 | Overview, robot status, summary, alerts |
| Monitoring | 4 | Metrics, activity, location |
| Weed Logs | 3 | Summary, detections (paginated) |
| Environment | 5 | Weather, soil, recommendations |
| Reports | 5 | Widgets, trends, export |
| Gallery | 5 | List, upload, delete images |
| Profile | 8 | User info, farm, settings, avatar |
| Assistant | 2 | Chatbot query and history |

### Database Schema (12 tables)

1. **users** - User accounts and authentication
2. **farms** - Farm information
3. **user_settings** - User preferences
4. **robot_status** - Real-time robot status
5. **robot_sessions** - Robot operation sessions
6. **robot_activity_log** - Activity timeline
7. **weed_detections** - Weed detection records
8. **weather_data** - Weather sensor data
9. **weather_forecast** - Weather predictions
10. **soil_data** - Soil sensor readings
11. **alerts** - System alerts/notifications
12. **chat_history** - Assistant conversation history

### Sample Data Included

- Demo user: `admin@weedx.com` / `admin123`
- Robot status with 85% battery
- 4 weed detections
- 7-day weather forecast
- Current soil data
- 3 system alerts
- Activity log entries

## File Structure

```
backend/
├── api/                          # 50+ endpoint handlers
│   ├── auth/                    # 3 files
│   ├── robot/                   # 1 file
│   ├── monitoring/              # 4 files
│   ├── weed-logs/              # 3 files
│   ├── environment/            # 5 files
│   ├── reports/                # 5 files
│   ├── gallery/                # 4 files
│   ├── profile/                # 8 files
│   ├── assistant/              # 2 files
│   └── landing.php             # 1 file
├── config/
│   ├── database.php            # MySQL connection
│   └── firebase.php            # Firebase config
├── database/
│   └── schema.sql              # Complete DB schema
├── mqtt/
│   └── subscriber.php          # MQTT listener
├── utils/
│   ├── response.php            # Response helper
│   ├── auth.php                # JWT authentication
│   └── logger.php              # API logging
├── logs/                        # API logs directory
├── .htaccess                    # Apache routing
├── index.php                    # Main router
└── README.md                   # Documentation
```

## How to Use

### Install Backend

```bash
# Copy to web server
cp -r backend /path/to/htdocs/weedx-backend/

# Import database
mysql -u root -p < backend/database/schema.sql
```

### Test Backend

```bash
# Test endpoint
curl http://localhost/weedx-backend/robot/status

# Login
curl -X POST http://localhost/weedx-backend/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@weedx.com","password":"admin123","firebaseToken":"test"}'
```

### Connect Android App

Update `app/src/main/java/com/example/weedx/utils/Constants.kt`:

```kotlin
// For emulator
const val BASE_URL = "http://10.0.2.2/weedx-backend/"

// For physical device (use your PC's IP)
const val BASE_URL = "http://192.168.1.XXX/weedx-backend/"
```

---

# Tailscale Deployment

This section explains how to deploy the WeedX backend on Raspberry Pi and access it via Tailscale.

## Prerequisites

### On Raspberry Pi
- Raspberry Pi (running Raspberry Pi OS)
- Tailscale installed and running
- SSH access enabled

### On Android Phone
- Tailscale installed and running
- Connected to same Tailscale network

## Tailscale Network

- **Tailscale Suffix**: `mullet-bull.ts.net`
- **Pi Hostname**: `raspberrypi.mullet-bull.ts.net`
- **Backend URL**: `http://raspberrypi.mullet-bull.ts.net/weedx-backend/`

## Deployment Steps

### Step 1: Configure Apache on Pi

```bash
# SSH into Pi
ssh pi@raspberrypi.mullet-bull.ts.net

# Run configuration script
sudo bash ~/configure-pi-apache.sh
```

This script installs Apache, MariaDB, PHP, enables required modules, and configures the database.

### Step 2: Deploy Backend

```bash
cd ~/weedx-mobile
./scripts/deploy-to-pi.sh
```

### Step 3: Import Database Schema

```bash
ssh pi@raspberrypi.mullet-bull.ts.net
mysql -u root weedx < /var/www/html/weedx-backend/database/schema.sql
```

### Step 4: Test Backend

```bash
./scripts/test-tailscale-backend.sh
```

### Step 5: Build Android App

```bash
./gradlew assembleDebug
```

## Troubleshooting

### Pi not reachable via Tailscale

```bash
ssh pi@raspberrypi.mullet-bull.ts.net
tailscale status
sudo systemctl restart tailscaled
```

### Backend returns 404

```bash
sudo systemctl status apache2
ls -la /var/www/html/weedx-backend/
sudo tail -f /var/log/apache2/error.log
```

### Database connection errors

```bash
sudo systemctl status mariadb
mysql -u weedx_user -pweedx_pass_2024 weedx -e "SELECT 1;"
```

## Running Backend 24/7

```bash
sudo systemctl enable apache2
sudo systemctl enable mariadb
```

---

# Database Guide

## Access Database

```bash
mysql -u root -p
# Enter password: root
```

## View Database

```sql
USE weedx;
SHOW TABLES;
DESCRIBE table_name;
SELECT * FROM table_name LIMIT 10;
```

## Common Queries

### View Users
```sql
SELECT id, username, email, role FROM users;
```

### View Robot Status
```sql
SELECT * FROM robot_status ORDER BY timestamp DESC LIMIT 1;
```

### View Recent Weed Detections
```sql
SELECT * FROM weed_detections ORDER BY detection_time DESC LIMIT 10;
```

### View Active Alerts
```sql
SELECT * FROM alerts WHERE status = 'active' ORDER BY created_at DESC;
```

## Modify Data

### Update User
```sql
UPDATE users SET email = 'new@email.com' WHERE username = 'demo';
```

### Insert Test Data
```sql
INSERT INTO weed_detections (user_id, weed_type, confidence, location_lat, location_lon) 
VALUES (1, 'Dandelion', 0.95, 31.5204, 74.3587);
```

### Delete Records
```sql
DELETE FROM alerts WHERE created_at < DATE_SUB(NOW(), INTERVAL 30 DAY);
```

## Reset Database

```bash
cd /home/umersani/weedx-mobile/xampp/htdocs/backend/database
mysql -u root -p < schema.sql
```

## Database Credentials

- **Database**: `weedx`
- **User**: `weedx_user`
- **Password**: `weedx_pass_2024`
- **Root Password**: `root`

---

# Image Handling

## Overview

Images in WeedX are stored as base64-encoded strings in the MySQL database. This provides better data portability and simplified backups.

## Database Schema

### weed_detections table

```sql
CREATE TABLE weed_detections (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id INT UNSIGNED NULL,
    weed_type VARCHAR(100) NOT NULL,
    crop_type VARCHAR(100) NULL,
    confidence DECIMAL(5, 2) DEFAULT 0,
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    image_base64 LONGTEXT NULL,
    image_mime_type VARCHAR(50) NULL,
    treated BOOLEAN DEFAULT FALSE,
    detected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## API Endpoints

### List Images
**GET** `/gallery?limit=50&offset=0`

### Upload Image
**POST** `/gallery`

**Form Data:**
- `image` (file) - Required
- `weed_type` (string)
- `confidence` (number)

## Android Integration

```kotlin
import android.util.Base64
import android.graphics.BitmapFactory
import coil.load

// Decode and display
val imageBytes = Base64.decode(imageBase64, Base64.DEFAULT)
val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
imageView.setImageBitmap(bitmap)

// Or use data URI with Coil
val dataUri = "data:${mimeType};base64,${imageBase64}"
imageView.load(dataUri)
```

## Supported Formats

- JPEG/JPG (`image/jpeg`)
- PNG (`image/png`)
- GIF (`image/gif`)
- WebP (`image/webp`)

## Performance Considerations

- **Max file size:** 10MB (before encoding)
- **Recommended:** 1-2MB for optimal performance
- **MySQL setting:** `max_allowed_packet = 64M`

---

# Image Upload Guide

## Basic Usage

### Upload Single Image

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

### Using Bash Wrapper

```bash
./scripts/upload-image.sh xampp/htdocs/backend/data/images/weed1.jpg \
  --weed-type="Grass Weed" \
  --confidence=88
```

### Batch Upload Directory

```bash
./scripts/batch-upload-images.sh xampp/htdocs/backend/data/images/ \
  --weed-type="Broadleaf Weed" \
  --confidence=85 \
  --user-id=1
```

## Command Reference

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `--user-id` | Integer | 1 | User who owns the detection |
| `--weed-type` | String | "Unknown" | Type of weed detected |
| `--crop-type` | String | null | Type of crop in field |
| `--confidence` | Number | 0 | Detection confidence (0-100) |
| `--latitude` | Number | 0 | GPS latitude coordinate |
| `--longitude` | Number | 0 | GPS longitude coordinate |

## Tips

### Compress Images First

```bash
convert large_image.jpg -quality 80 -resize 1920x1080\> compressed.jpg
php utils/image-upload.php compressed.jpg --weed-type="Broadleaf"
```

### Check Database After Upload

```bash
mysql -u root -p weedx -e "SELECT COUNT(*) FROM weed_detections WHERE image_base64 IS NOT NULL"
```

---

# Implementation Status

**Last Updated**: November 28, 2025

## Overall Progress: 75% Complete

| Component | Status | Progress |
|-----------|--------|----------|
| Backend Infrastructure | ✅ Complete | 100% |
| Backend API Endpoints | ✅ Complete | 100% |
| Android Project Setup | ✅ Complete | 100% |
| Android UI/Activities | ✅ Complete | 100% |
| Android Data Layer | ✅ Complete | 100% |
| Android Integration | 🔄 In Progress | 40% |
| Testing | ⏳ Pending | 0% |

## Completed - Backend

- Backend deployed on Raspberry Pi
- Apache 2.4.65 with mod_rewrite enabled
- MariaDB with dedicated user
- All 51+ API endpoints implemented & tested
- JWT authentication working
- CORS headers configured

## Completed - Android

### Project Setup
- Gradle dependencies configured (Retrofit, Hilt, Firebase, Room, Coil)
- ViewBinding enabled
- Hilt dependency injection setup

### UI/Activities (100%)
- SplashActivity, LoginActivity, SignUpActivity
- DashboardActivity, WeedLogsActivity, WeedLogDetailActivity
- MonitoringActivity, EnvironmentActivity
- ReportsActivity, ReportDetailActivity
- GalleryActivity, ProfileActivity
- AssistantActivity, SettingsActivity

### Data Layer (100%)
- All 9 API Services implemented
- All 9 Repositories implemented
- All Response Models created
- Auth interceptor for JWT tokens

## In Progress

- Build remaining ViewModels (Monitoring, WeedLogs, Environment, Reports, Profile)
- Integrate ViewModels with Activities
- Add error handling and retry logic

## Pending

- Firebase Configuration (FCM notifications)
- Robot Integration (MQTT)
- Unit/Integration Tests
- Production Deployment

## Test Credentials

```
Email: admin@weedx.com
Password: admin123
```

## Development Commands

### Backend
```bash
./scripts/deploy-backend.sh
./scripts/test-backend.sh
sudo tail -f /var/log/httpd/error_log
```

### Android
```bash
./gradlew assembleDebug
./gradlew installDebug
./gradlew test
```

---

# Use Case Flows

## 1. User Login

1. User opens app → Login screen displayed
2. User enters email and password
3. User taps "Login" button
4. App calls Firebase Auth login
5. App calls backend `/auth/login` with Firebase token
6. Backend validates token, returns JWT + user info
7. App stores token in SharedPreferences
8. App navigates to Dashboard screen

## 2. User Registration

1. User taps "Sign Up" tab
2. **Step 1**: User enters name, email, phone, password
3. **Step 2**: User enters farm name, location, size, crops
4. **Step 3**: User configures notifications, theme, language
5. User taps "Create Account"
6. App calls `POST /auth/register`
7. Backend creates user, farm, settings records
8. App stores JWT and navigates to Dashboard

## 3. View Dashboard

1. User sees Dashboard (home screen)
2. App calls `/landing` API
3. Backend returns robot status, today's summary, alerts
4. App displays data in cards/widgets

## 4. Check Robot Status

1. User taps "Robot Status" card
2. App calls `/robot/status` API
3. App shows map with robot pin + metrics (battery, speed, location)

## 5. View Weed Logs

1. User taps "Weed Logs" from nav
2. App calls `/weed-logs` API
3. App displays pie chart + scrollable list of detections
4. User taps detection → sees full-screen image + details

## 6. Monitor Live Data

1. User taps "Live Monitoring"
2. App calls `/monitoring/metrics` every 5 seconds
3. App updates UI dynamically with battery, herbicide, coverage

## 7. Check Weather & Soil

1. User taps "Weather"
2. App calls `/environment` API
3. App displays weather cards, 7-day forecast, soil metrics

## 8. Generate Reports

1. User taps "Reports"
2. App calls `/reports` API
3. App displays interactive charts
4. User taps "Export" → selects PDF or CSV

## 9. Browse Image Gallery

1. User taps "Gallery"
2. App calls `/gallery` API
3. App displays grid of images
4. User taps image → full-screen view

## 10. Ask Assistant

1. User taps "Assistant"
2. User types question
3. App calls `POST /assistant/query`
4. App displays assistant response

## 11. Manage Profile

1. User taps "Profile"
2. App calls `/profile` API
3. User edits name, farm info, settings
4. App calls `PUT /profile` to save changes

## Common Patterns

### Loading States
- Show skeleton loaders or progress indicators
- Disable user actions during loading

### Error Handling
- Display user-friendly error messages
- Offer retry options
- Cache data when possible

### Authentication
- All API calls include auth token via AuthInterceptor
- Token refresh handled automatically
- Session timeout redirects to login
