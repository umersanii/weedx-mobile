
# WeedX Backend - Arch Linux Setup Guide

Your native Arch LAMP stack is perfect for WeedX backend! Here's how to deploy it.

## 🎯 Your Current Setup

- **Web Server**: Apache (`httpd`)
- **PHP**: `php`, `php-apache`
- **Database**: MariaDB
- **Web Root**: `/srv/http/`
- **Management**: `systemctl`

## 🚀 Deployment Steps

### 1. Copy Backend to Web Root

```bash
# Copy backend to Apache web root
sudo cp -r /home/sadasani/AndroidStudioProjects/WeedX/xampp/htdocs/backend /srv/http/weedx-backend

# Set proper permissions
sudo chown -R http:http /srv/http/weedx-backend
sudo chmod -R 755 /srv/http/weedx-backend

# Create uploads directories
sudo mkdir -p /srv/http/weedx-backend/uploads/gallery
sudo mkdir -p /srv/http/weedx-backend/uploads/avatars
sudo chmod -R 777 /srv/http/weedx-backend/uploads
```

### 2. Enable Required Apache Modules

```bash
# Enable mod_rewrite (required for URL routing)
sudo nano /etc/httpd/conf/httpd.conf
```

Uncomment these lines:
```apache
LoadModule rewrite_module modules/mod_rewrite.so
```

Find the `<Directory "/srv/http">` section and change:
```apache
<Directory "/srv/http">
    Options Indexes FollowSymLinks
    AllowOverride None    # Change this
    Require all granted
</Directory>
```

To:
```apache
<Directory "/srv/http">
    Options Indexes FollowSymLinks
    AllowOverride All    # Now .htaccess will work
    Require all granted
</Directory>
```

### 3. Configure PHP

```bash
# Edit PHP config
sudo nano /etc/php/php.ini
```

Uncomment/enable these extensions:
```ini
extension=pdo_mysql
extension=mysqli
extension=json
extension=mbstring
extension=openssl
```

Set proper upload limits:
```ini
upload_max_filesize = 10M
post_max_size = 10M
memory_limit = 256M
```

### 4. Setup Database

```bash
# Start MariaDB
sudo systemctl start mariadb
sudo systemctl enable mariadb

# Secure installation (first time only)
sudo mysql_secure_installation

# Import database
sudo mysql -u root -p < /srv/http/weedx-backend/database/schema.sql

# Or login and import manually
sudo mysql -u root -p
```

In MySQL:
```sql
CREATE DATABASE IF NOT EXISTS weedx;
USE weedx;
SOURCE /srv/http/weedx-backend/database/schema.sql;
SHOW TABLES;  -- Verify 12 tables created
EXIT;
```

### 5. Configure Database Connection

```bash
sudo nano /srv/http/weedx-backend/config/database.php
```

Update credentials:
```php
private $host = 'localhost';
private $db_name = 'weedx';
private $username = 'root';
private $password = 'your_mysql_password';  // Set during mysql_secure_installation
```

### 6. Restart Apache

```bash
sudo systemctl restart httpd
```

Check status:
```bash
sudo systemctl status httpd
```

### 7. Test Backend

```bash
# Test basic endpoint
curl http://localhost/weedx-backend/robot/status

# Expected: {"success":false,"message":"Authorization token required"}
# This is good! It means routing works.

# Test login
curl -X POST http://localhost/weedx-backend/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@weedx.com","password":"admin123","firebaseToken":"test"}'

# Expected: JWT token in response
```

### 8. Get Your Local IP for Android

```bash
# Find your IP address
ip addr show | grep "inet " | grep -v 127.0.0.1

# Example output: inet 192.168.1.100/24
```

Update Android app `Constants.kt`:
```kotlin
const val BASE_URL = "http://192.168.1.100/weedx-backend/"
```

## 🔧 Common Commands

### Service Management
```bash
# Start services
sudo systemctl start httpd
sudo systemctl start mariadb

# Stop services
sudo systemctl stop httpd
sudo systemctl stop mariadb

# Restart Apache (after config changes)
sudo systemctl restart httpd

# Check status
sudo systemctl status httpd
sudo systemctl status mariadb

# Enable auto-start on boot
sudo systemctl enable httpd
sudo systemctl enable mariadb
```

### Logs
```bash
# Apache error log
sudo tail -f /var/log/httpd/error_log

# Apache access log
sudo tail -f /var/log/httpd/access_log

# PHP errors (if display_errors is off)
sudo tail -f /var/log/httpd/php_error_log
```

### Database
```bash
# Connect to MySQL
sudo mysql -u root -p

# Backup database
mysqldump -u root -p weedx > weedx_backup.sql

# Restore database
mysql -u root -p weedx < weedx_backup.sql
```

## 📁 File Locations

| Component | Location |
|-----------|----------|
| Web root | `/srv/http/` |
| Backend | `/srv/http/weedx-backend/` |
| Apache config | `/etc/httpd/conf/httpd.conf` |
| PHP config | `/etc/php/php.ini` |
| Apache logs | `/var/log/httpd/` |
| Database data | `/var/lib/mysql/` |

## 🐛 Troubleshooting

### "403 Forbidden"
```bash
# Check permissions
ls -la /srv/http/weedx-backend/
sudo chown -R http:http /srv/http/weedx-backend/
```

### ".htaccess not working"
```bash
# Ensure AllowOverride All is set
sudo nano /etc/httpd/conf/httpd.conf
# Look for <Directory "/srv/http"> and set AllowOverride All
sudo systemctl restart httpd
```

### "Database connection failed"
```bash
# Check MariaDB is running
sudo systemctl status mariadb

# Test connection
sudo mysql -u root -p
USE weedx;
SHOW TABLES;
```

### "PHP not working"
```bash
# Ensure php-apache is installed
sudo pacman -S php-apache

# Check PHP module is loaded
httpd -M | grep php

# Should see: php_module (shared)
```

### Can't connect from Android
```bash
# Check firewall
sudo iptables -L | grep 80

# Allow HTTP temporarily (for testing)
sudo iptables -I INPUT -p tcp --dport 80 -j ACCEPT

# Or disable firewall temporarily
sudo systemctl stop iptables
```

## 🔐 Security (Production)

For production deployment:

```bash
# Create dedicated database user
sudo mysql -u root -p
```

```sql
CREATE USER 'weedx_user'@'localhost' IDENTIFIED BY 'strong_password';
GRANT ALL PRIVILEGES ON weedx.* TO 'weedx_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

Update `config/database.php`:
```php
private $username = 'weedx_user';
private $password = 'strong_password';
```

## 📦 Optional: Composer (for MQTT)

If you want MQTT integration:

```bash
# Install Composer
sudo pacman -S composer

# Install PHP MQTT client
cd /srv/http/weedx-backend
composer install

# Run MQTT subscriber
php mqtt/subscriber.php
```

## ✅ Verification Checklist

- [ ] Apache running: `sudo systemctl status httpd`
- [ ] MariaDB running: `sudo systemctl status mariadb`
- [ ] Database imported: 12 tables in `weedx` database
- [ ] Backend accessible: `curl http://localhost/weedx-backend/robot/status`
- [ ] Login works: Get JWT token from `/auth/login`
- [ ] Android can connect: Use local IP in `BASE_URL`

## 🎓 Arch LAMP Advantages

Your setup is **better** than XAMPP because:

✅ **Lightweight** - No unnecessary bundled software
✅ **Native** - Uses Arch package manager, stays updated
✅ **Performance** - Optimized for your system
✅ **Control** - Full access to all configs
✅ **Production-like** - Same stack as real servers
✅ **Secure** - Latest security patches via `pacman`

---

**Your Arch LAMP stack is production-ready! 🚀**

Next: Test all endpoints → Connect Android app → Start building API services
