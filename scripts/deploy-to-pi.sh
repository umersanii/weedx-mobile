#!/bin/bash

# WeedX Backend Deployment Script for Raspberry Pi (via Tailscale)
# This script deploys the backend to your Pi through Tailscale network

set -e  # Exit on error

echo "=========================================="
echo "WeedX Backend - Pi Deployment (Tailscale)"
echo "=========================================="
echo ""

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
PI_HOST="raspberrypi.mullet-bull.ts.net"
PI_USER="umersanii"  # Change if you use different username
BACKEND_DEST="/var/www/html/weedx-backend"
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
BACKEND_SRC="$PROJECT_ROOT/xampp/htdocs/backend"

echo -e "${BLUE}Configuration:${NC}"
echo "  Pi Host: $PI_HOST"
echo "  Pi User: $PI_USER"
echo "  Backend Source: $BACKEND_SRC"
echo "  Backend Destination: $BACKEND_DEST"
echo ""

# Check if backend source exists
if [ ! -d "$BACKEND_SRC" ]; then
    echo -e "${RED}Backend source not found at: $BACKEND_SRC${NC}"
    exit 1
fi

echo -e "${YELLOW}Step 1: Testing Tailscale connection...${NC}"
if ! ping -c 1 "$PI_HOST" &> /dev/null; then
    echo -e "${RED}Cannot reach Pi at $PI_HOST${NC}"
    echo "Make sure:"
    echo "  1. Tailscale is running on both devices"
    echo "  2. Pi hostname is correct: $PI_HOST"
    echo "  3. Both devices are connected to Tailscale"
    exit 1
fi
echo -e "${GREEN}✓ Pi is reachable via Tailscale${NC}"
echo ""

echo -e "${YELLOW}Step 2: Creating temporary archive...${NC}"
TEMP_ARCHIVE="/tmp/weedx-backend.tar.gz"
tar -czf "$TEMP_ARCHIVE" -C "$(dirname "$BACKEND_SRC")" "$(basename "$BACKEND_SRC")"
echo -e "${GREEN}✓ Archive created: $TEMP_ARCHIVE${NC}"
echo ""

echo -e "${YELLOW}Step 3: Uploading backend to Pi...${NC}"
scp "$TEMP_ARCHIVE" "${PI_USER}@${PI_HOST}:/tmp/"
echo -e "${GREEN}✓ Backend uploaded${NC}"
echo ""

echo -e "${YELLOW}Step 4: Deploying on Pi...${NC}"
ssh "${PI_USER}@${PI_HOST}" << 'ENDSSH'
set -e

echo "  → Extracting archive..."
cd /tmp
tar -xzf weedx-backend.tar.gz

echo "  → Stopping Apache if running..."
sudo systemctl stop apache2 2>/dev/null || true

echo "  → Removing old backend..."
sudo rm -rf /var/www/html/weedx-backend

echo "  → Installing new backend..."
sudo mv backend /var/www/html/weedx-backend

echo "  → Setting permissions..."
sudo chown -R www-data:www-data /var/www/html/weedx-backend
sudo chmod -R 755 /var/www/html/weedx-backend

echo "  → Creating upload directories..."
sudo mkdir -p /var/www/html/weedx-backend/uploads/gallery
sudo mkdir -p /var/www/html/weedx-backend/uploads/avatars
sudo chmod -R 777 /var/www/html/weedx-backend/uploads

echo "  → Cleaning up..."
rm -f /tmp/weedx-backend.tar.gz

echo "  → Starting Apache..."
sudo systemctl start apache2
sudo systemctl enable apache2

echo "  → Starting MariaDB..."
sudo systemctl start mariadb
sudo systemctl enable mariadb

ENDSSH

echo -e "${GREEN}✓ Deployment completed on Pi${NC}"
echo ""

echo -e "${YELLOW}Step 5: Cleaning up local temporary files...${NC}"
rm -f "$TEMP_ARCHIVE"
echo -e "${GREEN}✓ Cleanup done${NC}"
echo ""

echo -e "${YELLOW}Step 6: Testing backend availability...${NC}"
sleep 3  # Wait for services to fully start

RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" "http://${PI_HOST}/weedx-backend/" 2>/dev/null || echo "000")

if [ "$RESPONSE" = "200" ] || [ "$RESPONSE" = "302" ]; then
    echo -e "${GREEN}✓ Backend is responding correctly (HTTP $RESPONSE)${NC}"
elif [ "$RESPONSE" = "000" ]; then
    echo -e "${YELLOW}⚠ Could not connect to backend${NC}"
    echo "The backend might still be starting. Try manually:"
    echo "  curl http://${PI_HOST}/weedx-backend/"
else
    echo -e "${YELLOW}⚠ Backend returned code: HTTP $RESPONSE${NC}"
fi

echo ""
echo "=========================================="
echo -e "${GREEN}Deployment Complete!${NC}"
echo "=========================================="
echo ""
echo -e "${BLUE}Backend URL (Tailscale):${NC}"
echo "  http://${PI_HOST}/weedx-backend/"
echo ""
echo -e "${BLUE}Android App Configuration:${NC}"
echo "  File: app/src/main/java/com/example/weedx/utils/Constants.kt"
echo "  const val BASE_URL = \"http://${PI_HOST}/weedx-backend/\""
echo ""
echo -e "${BLUE}Demo Credentials:${NC}"
echo "  Email: admin@weedx.com"
echo "  Password: admin123"
echo ""
echo -e "${BLUE}Test from your phone:${NC}"
echo "  curl -X POST http://${PI_HOST}/weedx-backend/auth/login \\"
echo "    -H \"Content-Type: application/json\" \\"
echo "    -d '{\"email\":\"admin@weedx.com\",\"password\":\"admin123\",\"firebaseToken\":\"test\"}'"
echo ""
echo -e "${BLUE}SSH into Pi:${NC}"
echo "  ssh ${PI_USER}@${PI_HOST}"
echo ""
echo -e "${BLUE}View Pi logs:${NC}"
echo "  ssh ${PI_USER}@${PI_HOST} 'sudo tail -f /var/log/apache2/error.log'"
echo ""
echo -e "${GREEN}Happy coding! 🚀${NC}"
