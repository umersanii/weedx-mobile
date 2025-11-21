#!/bin/bash

# WeedX Setup Helper - Interactive deployment wizard

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

clear
echo -e "${CYAN}"
cat << "EOF"
 _       __              ____  __  __
| |     / /__  ___  ____/ / |/ / / /
| | /| / / _ \/ _ \/ __  /   /  / / 
| |/ |/ /  __/  __/ /_/ /   |  /_/  
|__/|__/\___/\___/\____/_/|_| (_)   
                                     
Tailscale Deployment Wizard
EOF
echo -e "${NC}"
echo ""

PI_HOST="raspberrypi.mullet-bull.ts.net"
PI_USER="pi"

# Check if running from project root
if [ ! -f "settings.gradle.kts" ]; then
    echo -e "${RED}Error: Please run this script from the weedx-mobile root directory${NC}"
    exit 1
fi

echo -e "${BLUE}=== WeedX Backend Deployment Wizard ===${NC}"
echo ""
echo "This wizard will help you:"
echo "  1. Check prerequisites"
echo "  2. Configure Apache on Pi (if needed)"
echo "  3. Deploy backend to Pi"
echo "  4. Import database schema"
echo "  5. Test connectivity"
echo "  6. Build Android app"
echo ""
read -p "Continue? (y/n) " -n 1 -r
echo ""
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    exit 0
fi

echo ""
echo -e "${YELLOW}Step 1: Checking prerequisites...${NC}"
echo ""

# Check Tailscale
if ! command -v tailscale &> /dev/null; then
    echo -e "${RED}✗ Tailscale not found${NC}"
    echo "Install Tailscale: https://tailscale.com/download"
    exit 1
fi
echo -e "${GREEN}✓ Tailscale installed${NC}"

# Check Tailscale status
if ! tailscale status &> /dev/null; then
    echo -e "${RED}✗ Tailscale not running${NC}"
    echo "Start Tailscale and authenticate"
    exit 1
fi
echo -e "${GREEN}✓ Tailscale running${NC}"

# Check Pi connectivity
echo -n "Testing Pi connectivity... "
if ! ping -c 1 -W 2 "$PI_HOST" &> /dev/null; then
    echo -e "${RED}✗ Cannot reach Pi${NC}"
    echo ""
    echo "Troubleshooting:"
    echo "  1. Check Pi is powered on"
    echo "  2. Check Tailscale is running on Pi"
    echo "  3. Verify hostname: $PI_HOST"
    echo "  4. Run: tailscale status"
    exit 1
fi
echo -e "${GREEN}✓ Pi reachable${NC}"

# Check SSH
echo -n "Testing SSH access... "
if ! ssh -o ConnectTimeout=5 -o BatchMode=yes "${PI_USER}@${PI_HOST}" exit 2>/dev/null; then
    echo -e "${RED}✗ Cannot SSH to Pi${NC}"
    echo ""
    echo "Set up SSH keys:"
    echo "  ssh-keygen -t rsa"
    echo "  ssh-copy-id ${PI_USER}@${PI_HOST}"
    exit 1
fi
echo -e "${GREEN}✓ SSH access working${NC}"

echo ""
echo -e "${GREEN}All prerequisites met!${NC}"
echo ""

# Step 2: Check if Apache is configured
echo -e "${YELLOW}Step 2: Checking Apache configuration on Pi...${NC}"
echo ""

APACHE_CONFIGURED=$(ssh "${PI_USER}@${PI_HOST}" "[ -d /var/www/html ] && echo 'yes' || echo 'no'" 2>/dev/null || echo "no")

if [ "$APACHE_CONFIGURED" = "no" ] || ! ssh "${PI_USER}@${PI_HOST}" "systemctl is-active apache2" &>/dev/null; then
    echo -e "${YELLOW}Apache needs to be configured on Pi${NC}"
    echo ""
    read -p "Configure Apache now? (y/n) " -n 1 -r
    echo ""
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo -e "${BLUE}Copying configuration script to Pi...${NC}"
        scp scripts/configure-pi-apache.sh "${PI_USER}@${PI_HOST}:~/"
        
        echo -e "${BLUE}Running configuration script on Pi (will prompt for sudo)...${NC}"
        ssh -t "${PI_USER}@${PI_HOST}" "sudo bash ~/configure-pi-apache.sh"
        
        echo -e "${GREEN}✓ Apache configured${NC}"
    else
        echo -e "${YELLOW}Skipping Apache configuration${NC}"
        echo "You can run it later:"
        echo "  scp scripts/configure-pi-apache.sh ${PI_USER}@${PI_HOST}:~/"
        echo "  ssh ${PI_USER}@${PI_HOST}"
        echo "  sudo bash ~/configure-pi-apache.sh"
    fi
else
    echo -e "${GREEN}✓ Apache already configured${NC}"
fi

echo ""

# Step 3: Deploy backend
echo -e "${YELLOW}Step 3: Deploying backend to Pi...${NC}"
echo ""

read -p "Deploy backend now? (y/n) " -n 1 -r
echo ""
if [[ $REPLY =~ ^[Yy]$ ]]; then
    ./scripts/deploy-to-pi.sh
    echo ""
    echo -e "${GREEN}✓ Backend deployed${NC}"
else
    echo -e "${YELLOW}Skipping deployment${NC}"
fi

echo ""

# Step 4: Database setup
echo -e "${YELLOW}Step 4: Database setup...${NC}"
echo ""

DB_EXISTS=$(ssh "${PI_USER}@${PI_HOST}" "mysql -u root -e \"SHOW DATABASES LIKE 'weedx';\" 2>/dev/null | grep -c weedx || echo 0" 2>/dev/null || echo "0")

if [ "$DB_EXISTS" = "0" ]; then
    echo -e "${YELLOW}Database needs to be imported${NC}"
    echo ""
    read -p "Import database schema now? (y/n) " -n 1 -r
    echo ""
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo -e "${BLUE}Importing database schema...${NC}"
        ssh "${PI_USER}@${PI_HOST}" "mysql -u root weedx < /var/www/html/weedx-backend/database/schema.sql 2>/dev/null || mysql -u root < /var/www/html/weedx-backend/database/schema.sql"
        echo -e "${GREEN}✓ Database imported${NC}"
    else
        echo -e "${YELLOW}Skipping database import${NC}"
        echo "Import later with:"
        echo "  ssh ${PI_USER}@${PI_HOST} 'mysql -u root weedx < /var/www/html/weedx-backend/database/schema.sql'"
    fi
else
    echo -e "${GREEN}✓ Database already exists${NC}"
fi

echo ""

# Step 5: Test connectivity
echo -e "${YELLOW}Step 5: Testing backend...${NC}"
echo ""

read -p "Run connectivity tests? (y/n) " -n 1 -r
echo ""
if [[ $REPLY =~ ^[Yy]$ ]]; then
    ./scripts/test-tailscale-backend.sh
else
    echo -e "${YELLOW}Skipping tests${NC}"
fi

echo ""

# Step 6: Build app
echo -e "${YELLOW}Step 6: Building Android app...${NC}"
echo ""

read -p "Build Android app now? (y/n) " -n 1 -r
echo ""
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo -e "${BLUE}Building debug APK...${NC}"
    ./gradlew assembleDebug
    
    APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
    if [ -f "$APK_PATH" ]; then
        echo ""
        echo -e "${GREEN}✓ APK built successfully!${NC}"
        echo -e "Location: ${BLUE}$APK_PATH${NC}"
        echo ""
        echo "Transfer to phone:"
        echo "  adb install $APK_PATH"
        echo "  OR copy via USB/cloud storage"
    fi
else
    echo -e "${YELLOW}Skipping build${NC}"
    echo "Build later with: ./gradlew assembleDebug"
fi

echo ""
echo "=========================================="
echo -e "${GREEN}Setup Complete! 🎉${NC}"
echo "=========================================="
echo ""
echo -e "${BLUE}Backend URL:${NC}"
echo "  http://${PI_HOST}/weedx-backend/"
echo ""
echo -e "${BLUE}Demo Credentials:${NC}"
echo "  Email: admin@weedx.com"
echo "  Password: admin123"
echo ""
echo -e "${BLUE}Test from phone:${NC}"
echo "  1. Install Tailscale on phone"
echo "  2. Connect to Tailscale"
echo "  3. Install WeedX app"
echo "  4. Login with demo credentials"
echo ""
echo -e "${BLUE}Monitor logs:${NC}"
echo "  ssh ${PI_USER}@${PI_HOST} 'sudo tail -f /var/log/apache2/error.log'"
echo ""
echo -e "${BLUE}Documentation:${NC}"
echo "  docs/TAILSCALE_DEPLOYMENT.md"
echo "  DEPLOYMENT_QUICK_START.md"
echo ""
echo -e "${GREEN}Happy coding! 🚀${NC}"
