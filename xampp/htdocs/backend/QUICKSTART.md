# WeedX Backend - Quick Start Guide

## 🎯 Prerequisites

Install these before proceeding:

1. **XAMPP** (recommended for Windows) - https://www.apachefriends.org/
   - Includes PHP, MySQL, and Apache
   - Alternative: LAMP (Linux), MAMP (macOS), or WAMP

2. **PHP 7.4+** (included in XAMPP)

3. **MySQL 5.7+** (included in XAMPP)

## 🚀 Setup Steps (5 minutes)

### Step 1: Start Services

**XAMPP:**
1. Open XAMPP Control Panel
2. Start **Apache**
3. Start **MySQL**

### Step 2: Configure Database

1. Open phpMyAdmin: `http://localhost/phpmyadmin`
2. Click "SQL" tab
3. Copy and paste entire content of `backend/database/schema.sql`
4. Click "Go" to execute

**Or use command line:**
```bash
mysql -u root -p < backend/database/schema.sql
# Press Enter when asked for password (default: empty)
```

### Step 3: Update Database Credentials

Open `backend/config/database.php` and verify settings:

```php
private $host = 'localhost';
private $db_name = 'weedx';
private $username = 'root';
private $password = '';  // Empty for default XAMPP
```

### Step 4: Test Backend

Open browser and visit:
```
http://localhost/weedx-backend/robot/status
```

You should see JSON response like:
```json
{
  "success": false,
  "message": "Authorization token required"
}
```

This means the backend is working! (401 is expected without token)

### Step 5: Test Login

**Using cURL (Git Bash on Windows):**
```bash
curl -X POST http://localhost/weedx-backend/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"admin@weedx.com\",\"password\":\"admin123\",\"firebaseToken\":\"test\"}"
```

**Using PowerShell:**
```powershell
Invoke-RestMethod -Uri "http://localhost/weedx-backend/auth/login" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"email":"admin@weedx.com","password":"admin123","firebaseToken":"test"}'
```

**Using Postman:**
1. POST to `http://localhost/weedx-backend/auth/login`
2. Body (raw JSON):
```json
{
  "email": "admin@weedx.com",
  "password": "admin123",
  "firebaseToken": "test"
}
```

You should get a response with a JWT token!

### Step 6: Connect Android App

Update Android app's `Constants.kt`:

```kotlin
// For emulator
const val BASE_URL = "http://10.0.2.2/weedx-backend/"

// For physical device (find your PC's IP first)
const val BASE_URL = "http://192.168.X.X/weedx-backend/"
```

**Find your PC's IP:**
- Windows: `ipconfig` (look for IPv4 Address)
- Linux/Mac: `ifconfig` or `ip addr`

## 📱 Backend Structure

```
backend/
├── api/                  # All API endpoints
│   ├── auth/            # Login, logout, refresh
│   ├── robot/           # Robot status
│   ├── monitoring/      # Live monitoring
│   ├── weed-logs/       # Weed detections
│   ├── environment/     # Weather & soil
│   ├── reports/         # Analytics
│   ├── gallery/         # Images
│   ├── profile/         # User profile
│   └── assistant/       # AI chatbot
├── config/              # Database & Firebase
├── database/            # SQL schema
├── mqtt/                # MQTT subscriber (optional)
├── utils/               # Helpers (auth, response)
├── .htaccess            # URL routing
└── index.php            # Main router
```

## ✅ What's Included

**Authentication:**
- ✅ Login with JWT tokens
- ✅ Token refresh
- ✅ Logout

**Dashboard/Landing:**
- ✅ Robot status (battery, location, speed)
- ✅ Today's summary (weeds, area, herbicide)
- ✅ Recent alerts

**Monitoring:**
- ✅ Real-time metrics
- ✅ Activity timeline
- ✅ Robot location

**Weed Logs:**
- ✅ Weed detection history
- ✅ Summary by type
- ✅ Filter by weed type

**Environment:**
- ✅ Current weather
- ✅ 7-day forecast
- ✅ Soil data (moisture, pH, NPK)
- ✅ Farming recommendations

**Reports:**
- ✅ Statistics widgets
- ✅ Weed detection trends
- ✅ Distribution by crop
- ✅ Export (PDF/CSV placeholder)

**Gallery:**
- ✅ List weed images
- ✅ Upload images
- ✅ View/delete images

**Profile:**
- ✅ User info
- ✅ Farm details
- ✅ Settings (notifications, theme)
- ✅ Avatar upload

**Assistant:**
- ✅ Chatbot queries
- ✅ Conversation history
- ✅ Smart responses

## 🔧 Common Issues

### Apache won't start
- Another service using port 80 (Skype, IIS)
- Solution: Change Apache port in XAMPP config

### MySQL won't start
- Another MySQL instance running
- Solution: Stop other MySQL services

### "Authorization token required"
- This is normal! Login first to get token
- Include token in subsequent requests

### Android app can't connect
- Use your PC's local IP, not localhost
- For emulator: use `10.0.2.2`
- Ensure firewall allows connections

### "Database connection failed"
- Check MySQL is running
- Verify credentials in `config/database.php`
- Ensure `weedx` database exists

## 📖 Next Steps

1. ✅ Backend running locally
2. 🔄 Test all endpoints with Postman
3. 🔄 Update Android app BASE_URL
4. 🔄 Implement mobile API services
5. 🔄 Connect ViewModels to backend

## 🎓 Demo Credentials

**User:**
- Email: `admin@weedx.com`
- Password: `admin123`

**Database:**
- Username: `root`
- Password: `` (empty)
- Database: `weedx`

## 📞 Need Help?

Check:
1. `backend/README.md` - Detailed documentation
2. `docs/api_endpoints.md` - All API endpoints
3. XAMPP logs - `xampp/apache/logs/error.log`

---

**Backend is ready! Now you can start building the Android app integration! 🚀**
