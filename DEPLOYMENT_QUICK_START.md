 WeedX Backend - Tailscale Deployment Quick Start

Deploy WeedX backend to Raspberry Pi via Tailscale network.

## 🎯 Quick Deploy

### ✅ Backend Status: DEPLOYED & OPERATIONAL

The backend is already running on your Pi!
- **URL**: `http://raspberrypi.mullet-bull.ts.net/weedx-backend/`
- **Location**: `/var/www/html/weedx-backend/`
- **Database**: MySQL with 12 tables
- **Status**: All 30+ endpoints working

### Update Backend (if needed)
```bash
./scripts/deploy-to-pi.sh
```

### Test Backend
```bash
# Test all endpoints
cd scripts && ./test-backend.sh

# Or quick test
curl http://raspberrypi.mullet-bull.ts.net/weedx-backend/auth/login \
  -X POST -H "Content-Type: application/json" \
  -d '{"email":"admin@weedx.com","password":"admin123","firebaseToken":"test"}'
```

### 5. Build App
```bash
./gradlew assembleDebug
```

## 🌐 URLs

- **Backend**: `http://raspberrypi.mullet-bull.ts.net/weedx-backend/`
- **Pi SSH**: `ssh pi@raspberrypi.mullet-bull.ts.net`

## 🔑 Demo Login

- Email: `admin@weedx.com`
- Password: `admin123`

## 📝 Scripts

| Script | Purpose |
|--------|---------|
| `configure-pi-apache.sh` | Configure Apache on Pi (run once) |
| `deploy-to-pi.sh` | Deploy backend to Pi |
| `test-tailscale-backend.sh` | Test backend connectivity |
| `deploy-backend.sh` | Deploy to local Arch Linux (legacy) |

## 🔧 Troubleshooting

```bash
# Check Tailscale
ping raspberrypi.mullet-bull.ts.net

# Check Apache
ssh pi@raspberrypi.mullet-bull.ts.net 'sudo systemctl status apache2'

# View logs
ssh pi@raspberrypi.mullet-bull.ts.net 'sudo tail -f /var/log/apache2/error.log'

# Restart services
ssh pi@raspberrypi.mullet-bull.ts.net 'sudo systemctl restart apache2'
```

## 📚 Full Documentation

See [TAILSCALE_DEPLOYMENT.md](docs/TAILSCALE_DEPLOYMENT.md) for complete guide.
