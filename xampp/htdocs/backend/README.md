# WeedX Backend API

PHP REST API backend for the WeedX precision farming system.

## 📋 Requirements

- **PHP**: 7.4 or higher
- **MySQL**: 5.7 or higher (or MariaDB 10.3+)
- **Apache**: 2.4+ with mod_rewrite enabled
- **Composer**: For MQTT library (optional)
- **MQTT Broker**: Mosquitto (if using robot integration)

## 🚀 Installation

### 1. Clone/Copy Backend

Copy the `backend` folder to your web server directory:

```bash
# For XAMPP (Windows)
C:\xampp\htdocs\weedx-backend\

# For LAMP (Linux)
/var/www/html/weedx-backend/

# For MAMP (macOS)
/Applications/MAMP/htdocs/weedx-backend/
```

### 2. Configure Database

Edit `config/database.php` with your MySQL credentials:

```php
private $host = 'localhost';
private $db_name = 'weedx';
private $username = 'root';
private $password = 'your_password';
```

### 3. Import Database Schema

```bash
mysql -u root -p < database/schema.sql
```

Or use phpMyAdmin:
1. Open phpMyAdmin
2. Create database `weedx`
3. Import `database/schema.sql`

### 4. Configure Apache

**Enable mod_rewrite:**

```bash
# Linux
sudo a2enmod rewrite
sudo systemctl restart apache2

# XAMPP - usually enabled by default
```

**Virtual Host (Optional):**

```apache
<VirtualHost *:80>
    ServerName weedx.local
    DocumentRoot "C:/xampp/htdocs/weedx-backend"
    
    <Directory "C:/xampp/htdocs/weedx-backend">
        AllowOverride All
        Require all granted
    </Directory>
</VirtualHost>
```

Add to hosts file:
```
127.0.0.1 weedx.local
```

### 5. Test Installation

Visit: `http://localhost/weedx-backend/robot/status`

You should see a JSON response (may show 401 if not authenticated).

## 🔐 Authentication

The API uses JWT (JSON Web Tokens) for authentication.

### Demo Credentials

- **Email**: `admin@weedx.com`
- **Password**: `admin123`

### Login Example

```bash
curl -X POST http://localhost/weedx-backend/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@weedx.com",
    "password": "admin123",
    "firebaseToken": "dummy-token"
  }'
```

Response:
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJ0eXAiOiJKV1QiLCJhbGc...",
    "user": {
      "id": 1,
      "name": "Admin User",
      "email": "admin@weedx.com"
    }
  }
}
```

### Using Token

Include the token in all subsequent requests:

```bash
curl -X GET http://localhost/weedx-backend/landing \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

## 📡 API Endpoints

See [API Documentation](../docs/api_endpoints.md) for complete list.

**Key endpoints:**

- `POST /auth/login` - User login
- `GET /landing` - Dashboard overview
- `GET /robot/status` - Robot status
- `GET /weed-logs` - Weed detections
- `GET /monitoring` - Live monitoring
- `GET /environment` - Weather & soil
- `GET /reports` - Analytics reports
- `GET /gallery` - Image gallery (base64 encoded)
- `GET /profile` - User profile

**Image Storage:** Images are stored as base64-encoded strings in the database. See [BASE64_IMAGES.md](BASE64_IMAGES.md) for details.

## 🤖 MQTT Integration (Optional)

### Install PHP MQTT Library

```bash
cd backend
composer require php-mqtt/client
```

### Run MQTT Subscriber

```bash
php mqtt/subscriber.php
```

This script listens to robot MQTT topics and saves data to database.

### MQTT Topics

- `weedx/robot/status` - Robot status updates
- `weedx/robot/location` - GPS coordinates
- `weedx/robot/battery` - Battery/herbicide levels
- `weedx/weed/detection` - Weed detections
- `weedx/sensor/weather` - Weather data
- `weedx/sensor/soil` - Soil sensor data
- `weedx/alert` - Alerts/notifications

## 🛠️ Configuration

### Change JWT Secret

Edit `utils/auth.php`:

```php
private static $secret_key = 'your-super-secret-key-here';
```

### Change Base URL

The API uses relative URLs. If deploying to production, update CORS settings in `index.php`.

### Firebase Configuration

Edit `config/firebase.php` with your Firebase credentials (optional).

## 📱 Connect Android App

In your Android app, update `Constants.kt`:

```kotlin
const val BASE_URL = "http://YOUR_IP:PORT/weedx-backend/"
// Example: "http://192.168.1.100/weedx-backend/"
```

**Find your local IP:**

```bash
# Windows
ipconfig

# Linux/Mac
ifconfig
```

Use your local network IP (e.g., `192.168.1.100`) so the Android app can connect.

## 🧪 Testing Endpoints

### Using cURL

```bash
# Login
curl -X POST http://localhost/weedx-backend/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@weedx.com","password":"admin123","firebaseToken":"test"}'

# Get Dashboard (use token from login)
curl -X GET http://localhost/weedx-backend/landing \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Using Postman

1. Import endpoints from `docs/api_endpoints.md`
2. Set base URL: `http://localhost/weedx-backend`
3. Login first to get token
4. Add token to Authorization header for other requests

## 🗂️ Project Structure

```
backend/
├── api/                    # API endpoint handlers
│   ├── auth/              # Authentication
│   ├── robot/             # Robot status
│   ├── monitoring/        # Live monitoring
│   ├── weed-logs/         # Weed detections
│   ├── environment/       # Weather/soil
│   ├── reports/           # Analytics
│   ├── gallery/           # Images
│   ├── profile/           # User profile
│   └── assistant/         # Chatbot
├── config/                # Configuration files
│   ├── database.php       # MySQL connection
│   └── firebase.php       # Firebase config
├── data/                  # Static data files
│   └── images/            # Static images served via API
├── database/              # Database files
│   └── schema.sql         # Database schema
├── mqtt/                  # MQTT subscriber
│   └── subscriber.php
├── uploads/               # Uploaded files
│   ├── gallery/           # Weed images
│   └── avatars/           # User avatars
├── utils/                 # Helper functions
│   ├── response.php       # Response formatter
│   ├── auth.php           # JWT authentication
│   └── logger.php         # API request/response logging
├── logs/                  # API log files
│   └── api_YYYY-MM-DD.log # Daily rotating logs
├── .htaccess              # Apache rewrite rules
├── index.php              # Main router
└── README.md              # This file
```

## 🔧 Troubleshooting

### View API Logs

All API requests are logged for debugging:

```bash
# View today's logs
tail -f /var/www/html/weedx-backend/logs/api_$(date +%Y-%m-%d).log

# Or via Apache error log (real-time)
sudo tail -f /var/log/apache2/error.log | grep -E '\[API|REQUEST|AUTH|SUCCESS|ERROR\]'
```

Log format shows:
- Request method & endpoint
- Auth status (with/without token)
- Response success/error with data
- Timestamps for each call

### "404 Not Found" on all endpoints

- Enable Apache mod_rewrite
- Check `.htaccess` is in root directory
- Verify `AllowOverride All` in Apache config

### "Connection refused" from Android

- Use your local IP, not `localhost`
- Disable firewall temporarily
- Ensure Apache is running
- Android emulator: use `10.0.2.2` instead of `localhost`

### "Database connection failed"

- Check MySQL is running
- Verify credentials in `config/database.php`
- Ensure database `weedx` exists
- Import `database/schema.sql`

### CORS errors

- CORS headers are set in `index.php`
- If still issues, add to `.htaccess`:

```apache
Header set Access-Control-Allow-Origin "*"
Header set Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS"
Header set Access-Control-Allow-Headers "Content-Type, Authorization"
```

## 📊 Sample Data

The database schema includes sample data:

- 1 demo user (`admin@weedx.com`)
- Robot status with 85% battery
- 4 weed detections
- 7-day weather forecast
- Soil sensor data
- 3 alerts

You can add more test data manually via phpMyAdmin.

## 🔒 Security Notes

**For Development Only:**

- Change JWT secret key in production
- Use HTTPS in production
- Implement proper Firebase token validation
- Add rate limiting
- Sanitize all inputs
- Use prepared statements (already done)

## 📝 License

Educational project - WeedX Team 2025

## 📞 Support

For issues or questions, check the main project README or create an issue on GitHub.
