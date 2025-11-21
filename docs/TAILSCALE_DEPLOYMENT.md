# WeedX Backend Deployment Guide - Tailscale

This guide explains how to deploy the WeedX backend on your Raspberry Pi and access it via Tailscale from your Android phone.

## 📋 Prerequisites

### On Raspberry Pi
- Raspberry Pi (running Raspberry Pi OS or similar)
- Tailscale installed and running
- Internet connection
- SSH access enabled

### On Development Machine
- Tailscale installed and running
- SSH key configured for Pi access
- Backend code ready to deploy

### On Android Phone
- Tailscale installed and running
- Connected to same Tailscale network

## 🌐 Tailscale Network

- **Tailscale Suffix**: `mullet-bull.ts.net`
- **Pi Hostname**: `raspberrypi.mullet-bull.ts.net`
- **Phone Hostname**: `google-pixel-9a.mullet-bull.ts.net`
- **Backend URL**: `http://raspberrypi.mullet-bull.ts.net/weedx-backend/`

## 🚀 Deployment Steps

### Step 1: Configure Apache on Pi (One-time setup)

SSH into your Pi and run the configuration script:

```bash
# Copy the script to Pi
scp scripts/configure-pi-apache.sh pi@raspberrypi.mullet-bull.ts.net:~/

# SSH into Pi
ssh pi@raspberrypi.mullet-bull.ts.net

# Run the configuration script
sudo bash ~/configure-pi-apache.sh
```

This script will:
- Install Apache, MariaDB, PHP
- Enable required Apache modules
- Configure AllowOverride for .htaccess
- Set up CORS headers
- Create WeedX database and user
- Configure firewall rules
- Start and enable services

### Step 2: Deploy Backend to Pi

From your development machine, run:

```bash
cd ~/weedx-mobile
./scripts/deploy-to-pi.sh
```

This script will:
- Test Tailscale connectivity
- Create an archive of the backend code
- Upload it to the Pi via SCP
- Extract and install on Pi
- Set proper permissions
- Restart Apache and MariaDB
- Test basic connectivity

**Note**: The script assumes your Pi username is `pi`. Edit the script if you use a different username:
```bash
PI_USER="your_username"  # Change in deploy-to-pi.sh
```

### Step 3: Import Database Schema

If this is the first deployment, import the database schema:

```bash
ssh pi@raspberrypi.mullet-bull.ts.net

# Import schema
mysql -u root weedx < /var/www/html/weedx-backend/database/schema.sql

# Verify database
mysql -u weedx_user -pweedx_pass_2024 weedx -e "SHOW TABLES;"
```

### Step 4: Test Backend

Run the test script from your development machine:

```bash
./scripts/test-tailscale-backend.sh
```

This will test:
- Tailscale connectivity to Pi
- Backend base URL
- Authentication endpoint
- Protected API endpoints

### Step 5: Build Android App

The Android app is already configured with the Tailscale URL:

```kotlin
// app/src/main/java/com/example/weedx/utils/Constants.kt
const val BASE_URL = "http://raspberrypi.mullet-bull.ts.net/weedx-backend/"
```

Build and install the app:

```bash
./gradlew assembleDebug

# Or build and install directly if phone is connected via USB
./gradlew installDebug
```

### Step 6: Test on Phone

1. **Ensure Tailscale is running** on your phone
2. **Verify connection**:
   ```bash
   # Install Termux on your phone (optional)
   # Then test:
   ping raspberrypi.mullet-bull.ts.net
   curl http://raspberrypi.mullet-bull.ts.net/weedx-backend/
   ```

3. **Open WeedX App** and login:
   - Email: `admin@weedx.com`
   - Password: `admin123`

## 🔧 Troubleshooting

### Pi not reachable via Tailscale

```bash
# Check Tailscale status on Pi
ssh pi@raspberrypi.mullet-bull.ts.net
tailscale status

# Restart Tailscale if needed
sudo systemctl restart tailscaled
```

### Backend returns 404

```bash
# Check Apache is running
ssh pi@raspberrypi.mullet-bull.ts.net
sudo systemctl status apache2

# Check backend files exist
ls -la /var/www/html/weedx-backend/

# Check Apache logs
sudo tail -f /var/log/apache2/error.log
```

### Database connection errors

```bash
# Check MariaDB is running
sudo systemctl status mariadb

# Test database connection
mysql -u weedx_user -pweedx_pass_2024 weedx -e "SELECT 1;"

# Verify database exists
mysql -u root -e "SHOW DATABASES LIKE 'weedx';"
```

### CORS errors in app

```bash
# Verify .htaccess exists
cat /var/www/html/weedx-backend/.htaccess

# Check Apache headers module
apache2ctl -M | grep headers

# Enable if not active
sudo a2enmod headers
sudo systemctl restart apache2
```

### Phone can't connect

1. **Check Tailscale on phone**: Open Tailscale app, verify connected
2. **Test from phone**: Use Termux or browser to test `http://raspberrypi.mullet-bull.ts.net/weedx-backend/`
3. **Check firewall**: Ensure Pi allows HTTP (port 80)
4. **Verify hostnames**: Use `tailscale status` on both devices

## 📱 Running Backend 24/7

### Enable Apache autostart

```bash
ssh pi@raspberrypi.mullet-bull.ts.net
sudo systemctl enable apache2
sudo systemctl enable mariadb
```

### Monitor backend health

Create a simple monitoring script on Pi:

```bash
# Create monitor script
cat > /home/pi/monitor-backend.sh << 'EOF'
#!/bin/bash
while true; do
    if ! curl -s http://localhost/weedx-backend/ > /dev/null; then
        echo "Backend down, restarting Apache..."
        sudo systemctl restart apache2
    fi
    sleep 300  # Check every 5 minutes
done
EOF

chmod +x /home/pi/monitor-backend.sh

# Run in background (or use systemd service)
nohup /home/pi/monitor-backend.sh &
```

### View logs remotely

```bash
# From development machine
ssh pi@raspberrypi.mullet-bull.ts.net 'sudo tail -f /var/log/apache2/error.log'
```

## 🔄 Updating Backend

To update backend code after changes:

```bash
cd ~/weedx-mobile
./scripts/deploy-to-pi.sh
```

The script will:
- Upload new code
- Replace old backend
- Restart services
- Test connectivity

## 📊 Monitoring & Logs

### Check Apache logs
```bash
ssh pi@raspberrypi.mullet-bull.ts.net
sudo tail -f /var/log/apache2/error.log
sudo tail -f /var/log/apache2/access.log
```

### Check PHP errors
```bash
# View PHP error log
sudo tail -f /var/log/apache2/error.log | grep PHP
```

### Check database
```bash
# MySQL slow query log
sudo tail -f /var/log/mysql/slow.log
```

## 🔐 Security Notes

1. **Change default passwords** in production:
   ```bash
   # Update in: xampp/htdocs/backend/config/database.php
   # Then redeploy
   ```

2. **Enable HTTPS** for production (optional but recommended):
   ```bash
   # Install certbot
   sudo apt install certbot python3-certbot-apache
   
   # Get certificate (requires public domain)
   sudo certbot --apache
   ```

3. **Restrict database access**:
   ```sql
   # Only allow local connections
   UPDATE mysql.user SET Host='localhost' WHERE User='weedx_user';
   FLUSH PRIVILEGES;
   ```

## 📞 Support

If you encounter issues:

1. Check logs on Pi: `sudo tail -f /var/log/apache2/error.log`
2. Verify Tailscale connectivity: `ping raspberrypi.mullet-bull.ts.net`
3. Test from browser: `http://raspberrypi.mullet-bull.ts.net/weedx-backend/`
4. Check services: `sudo systemctl status apache2 mariadb`

## 🎉 Success Checklist

- [ ] Tailscale running on Pi, phone, and dev machine
- [ ] Apache and MariaDB configured on Pi
- [ ] Backend deployed to `/var/www/html/weedx-backend/`
- [ ] Database schema imported
- [ ] Backend accessible via `http://raspberrypi.mullet-bull.ts.net/weedx-backend/`
- [ ] Android app built with Tailscale URL
- [ ] Phone can login to app
- [ ] Services set to start on boot

---

**Next**: Once backend is running, proceed with [API Integration](implementation_status.md) to connect all Android app features.
