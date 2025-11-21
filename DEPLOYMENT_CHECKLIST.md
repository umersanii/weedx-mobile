# 🚀 WeedX Tailscale Deployment Checklist

Use this checklist to deploy your WeedX backend to Raspberry Pi via Tailscale.

## 📋 Pre-Deployment Checklist

### On Raspberry Pi
- [ ] Pi is powered on and connected to network
- [ ] Tailscale is installed: `curl -fsSL https://tailscale.com/install.sh | sh`
- [ ] Tailscale is authenticated: `sudo tailscale up`
- [ ] SSH is enabled (Raspberry Pi Configuration → Interfaces → SSH)
- [ ] Pi hostname verified: `hostname` should be `raspberrypi`

### On Development Machine (Your Current Machine)
- [ ] Tailscale is installed and running
- [ ] Can ping Pi: `ping raspberrypi.mullet-bull.ts.net`
- [ ] SSH key is set up: `ssh-copy-id pi@raspberrypi.mullet-bull.ts.net`
- [ ] Backend code is ready in `xampp/htdocs/backend/`

### On Android Phone
- [ ] Tailscale app is installed from Play Store
- [ ] Tailscale is authenticated and connected
- [ ] Can access Tailscale network

---

## 🎯 Deployment Steps

### Option A: Automated (Recommended) ⭐

Run the interactive setup wizard:
```bash
cd ~/weedx-mobile
./scripts/setup-wizard.sh
```

The wizard will:
- ✅ Check all prerequisites
- ✅ Configure Apache on Pi
- ✅ Deploy backend
- ✅ Import database
- ✅ Test connectivity
- ✅ Build Android app

---

### Option B: Manual Step-by-Step

#### Step 1: Configure Apache on Pi
```bash
# Copy script to Pi
scp scripts/configure-pi-apache.sh pi@raspberrypi.mullet-bull.ts.net:~/

# SSH into Pi
ssh pi@raspberrypi.mullet-bull.ts.net

# Run configuration (on Pi)
sudo bash ~/configure-pi-apache.sh

# Exit SSH
exit
```

**Checklist after Step 1:**
- [ ] Apache is running: `ssh pi@raspberrypi.mullet-bull.ts.net 'systemctl status apache2'`
- [ ] MariaDB is running: `ssh pi@raspberrypi.mullet-bull.ts.net 'systemctl status mariadb'`
- [ ] Directory created: `ssh pi@raspberrypi.mullet-bull.ts.net 'ls -la /var/www/html'`

---

#### Step 2: Deploy Backend
```bash
cd ~/weedx-mobile
./scripts/deploy-to-pi.sh
```

**Checklist after Step 2:**
- [ ] Script completed without errors
- [ ] Backend files on Pi: `ssh pi@raspberrypi.mullet-bull.ts.net 'ls -la /var/www/html/weedx-backend'`
- [ ] Apache restarted successfully

---

#### Step 3: Import Database
```bash
ssh pi@raspberrypi.mullet-bull.ts.net

# Import schema
mysql -u root weedx < /var/www/html/weedx-backend/database/schema.sql

# Verify tables exist
mysql -u weedx_user -pweedx_pass_2024 weedx -e "SHOW TABLES;"

# Exit SSH
exit
```

**Checklist after Step 3:**
- [ ] Schema imported without errors
- [ ] 12 tables created (users, robot_status, weed_detections, etc.)
- [ ] Sample data loaded

---

#### Step 4: Test Backend
```bash
./scripts/test-tailscale-backend.sh
```

**Checklist after Step 4:**
- [ ] Pi is reachable via Tailscale
- [ ] Backend responds to HTTP requests
- [ ] Login endpoint works
- [ ] JWT token received

---

#### Step 5: Build Android App
```bash
# Clean build
./gradlew clean

# Build debug APK
./gradlew assembleDebug
```

**Checklist after Step 5:**
- [ ] Build completed successfully
- [ ] APK exists at: `app/build/outputs/apk/debug/app-debug.apk`
- [ ] No build errors

---

#### Step 6: Install on Phone

**Option A: Via USB**
```bash
# Enable USB debugging on phone
# Connect phone via USB
adb install app/build/outputs/apk/debug/app-debug.apk
```

**Option B: Via File Transfer**
- Copy APK to phone (USB, cloud storage, etc.)
- Open APK on phone to install
- Allow installation from unknown sources if prompted

**Checklist after Step 6:**
- [ ] App installed on phone
- [ ] Tailscale is running on phone
- [ ] Phone is connected to Tailscale network

---

#### Step 7: Test App

1. Open WeedX app on phone
2. Login with demo credentials:
   - **Email**: `admin@weedx.com`
   - **Password**: `admin123`
3. Check dashboard loads

**Checklist after Step 7:**
- [ ] App opens without crashes
- [ ] Login successful
- [ ] Dashboard displays data
- [ ] No network errors

---

## ✅ Verification Tests

### From Development Machine
```bash
# Ping test
ping -c 3 raspberrypi.mullet-bull.ts.net

# HTTP test
curl http://raspberrypi.mullet-bull.ts.net/weedx-backend/

# Login test
curl -X POST http://raspberrypi.mullet-bull.ts.net/weedx-backend/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@weedx.com","password":"admin123","firebaseToken":"test"}'

# Should return JWT token
```

### From Phone (Optional - using Termux)
```bash
# Install Termux from Play Store
pkg install curl

# Test from phone
curl http://raspberrypi.mullet-bull.ts.net/weedx-backend/
```

### Check Pi Logs
```bash
# View Apache error log
ssh pi@raspberrypi.mullet-bull.ts.net 'sudo tail -50 /var/log/apache2/error.log'

# View Apache access log
ssh pi@raspberrypi.mullet-bull.ts.net 'sudo tail -50 /var/log/apache2/access.log'

# Monitor in real-time
ssh pi@raspberrypi.mullet-bull.ts.net 'sudo tail -f /var/log/apache2/error.log'
```

---

## 🔧 Troubleshooting Guide

### Problem: Can't reach Pi via Tailscale

**Solution:**
```bash
# Check Tailscale status on dev machine
tailscale status

# Check Tailscale status on Pi
ssh pi@192.168.1.x  # Use local IP if Tailscale not working
tailscale status
sudo systemctl restart tailscaled

# Verify hostname
hostname  # Should show: raspberrypi
```

---

### Problem: Backend returns 404

**Solution:**
```bash
# Check backend files exist
ssh pi@raspberrypi.mullet-bull.ts.net 'ls -la /var/www/html/weedx-backend'

# Check Apache config
ssh pi@raspberrypi.mullet-bull.ts.net 'sudo apache2ctl -t'

# Restart Apache
ssh pi@raspberrypi.mullet-bull.ts.net 'sudo systemctl restart apache2'

# Check .htaccess
ssh pi@raspberrypi.mullet-bull.ts.net 'cat /var/www/html/weedx-backend/.htaccess'
```

---

### Problem: Database connection failed

**Solution:**
```bash
# Check MariaDB is running
ssh pi@raspberrypi.mullet-bull.ts.net 'sudo systemctl status mariadb'

# Test database connection
ssh pi@raspberrypi.mullet-bull.ts.net 'mysql -u weedx_user -pweedx_pass_2024 weedx -e "SELECT 1;"'

# Check database exists
ssh pi@raspberrypi.mullet-bull.ts.net 'mysql -u root -e "SHOW DATABASES LIKE \"weedx\";"'

# Restart MariaDB
ssh pi@raspberrypi.mullet-bull.ts.net 'sudo systemctl restart mariadb'
```

---

### Problem: App can't connect from phone

**Solution:**
1. Check Tailscale is running on phone (open Tailscale app)
2. Verify Tailscale shows "Connected"
3. Check Pi is listed in Tailscale devices
4. Test from phone browser: `http://raspberrypi.mullet-bull.ts.net/weedx-backend/`
5. Check app permissions (Internet access)
6. View app logs in Android Studio (Logcat)

---

### Problem: CORS errors in app

**Solution:**
```bash
# Check Apache modules
ssh pi@raspberrypi.mullet-bull.ts.net 'apache2ctl -M | grep headers'

# Enable headers module if needed
ssh pi@raspberrypi.mullet-bull.ts.net 'sudo a2enmod headers && sudo systemctl restart apache2'

# Verify .htaccess has CORS headers
ssh pi@raspberrypi.mullet-bull.ts.net 'cat /var/www/html/weedx-backend/.htaccess | grep CORS'
```

---

## 🎉 Success Indicators

You're ready to go when:
- ✅ Backend accessible via: `http://raspberrypi.mullet-bull.ts.net/weedx-backend/` **← DONE**
- ✅ Login returns JWT token **← DONE**
- ✅ Dashboard endpoint returns data **← DONE**
- ✅ All API endpoints working **← DONE**
- ⏳ Android app can login
- ⏳ Dashboard displays in app
- ✅ No errors in Pi logs **← DONE**
- ✅ Services auto-start on Pi reboot **← DONE**

**Current Status**: Backend fully operational! Ready for Android app testing.

---

## 🔄 Maintenance

### Update Backend Code
```bash
cd ~/weedx-mobile
./scripts/deploy-to-pi.sh
```

### Update Android App
```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Monitor Backend Health
```bash
# Check services
ssh pi@raspberrypi.mullet-bull.ts.net 'sudo systemctl status apache2 mariadb'

# View recent errors
ssh pi@raspberrypi.mullet-bull.ts.net 'sudo tail -50 /var/log/apache2/error.log'

# Check disk space
ssh pi@raspberrypi.mullet-bull.ts.net 'df -h'
```

### Restart Services
```bash
ssh pi@raspberrypi.mullet-bull.ts.net 'sudo systemctl restart apache2 mariadb'
```

---

## 📚 Documentation References

- **Quick Start**: [DEPLOYMENT_QUICK_START.md](DEPLOYMENT_QUICK_START.md)
- **Full Guide**: [docs/TAILSCALE_DEPLOYMENT.md](docs/TAILSCALE_DEPLOYMENT.md)
- **Setup Summary**: [TAILSCALE_SETUP_SUMMARY.md](TAILSCALE_SETUP_SUMMARY.md)
- **API Docs**: [docs/api_endpoints.md](docs/api_endpoints.md)
- **Architecture**: [docs/architecture.md](docs/architecture.md)

---

## 🆘 Getting Help

If you're stuck:
1. Check logs on Pi: `sudo tail -f /var/log/apache2/error.log`
2. Test connectivity: `ping raspberrypi.mullet-bull.ts.net`
3. Review this checklist from the start
4. Check [docs/TAILSCALE_DEPLOYMENT.md](docs/TAILSCALE_DEPLOYMENT.md)

---

**Good luck with your deployment! 🚀**
