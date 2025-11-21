#!/bin/bash

# WeedX Backend Deployment Script for Debian/Ubuntu/Raspberry Pi OS
# This script automates the deployment of WeedX backend to /var/www/html/

set -e  # Exit on error

echo "=========================================="
echo "WeedX Backend Deployment Script"
echo "=========================================="
echo ""

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if running as root
if [ "$EUID" -eq 0 ]; then 
    echo -e "${RED}Please do not run as root. Use sudo when prompted.${NC}"
    exit 1
fi

# Get the script directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
BACKEND_SRC="$SCRIPT_DIR/../xampp/htdocs/backend"
BACKEND_DEST="/var/www/html/weedx-backend"

echo -e "${YELLOW}Step 1: Checking prerequisites...${NC}"

# Check if Apache is installed
if ! command -v apache2 &> /dev/null; then
    echo -e "${RED}Apache2 is not installed!${NC}"
    echo "Install it with: sudo apt install apache2"
    exit 1
fi

# Check if MariaDB/MySQL is installed
if ! command -v mysql &> /dev/null; then
    echo -e "${RED}MariaDB/MySQL is not installed!${NC}"
    echo "Install it with: sudo apt install mariadb-server"
    exit 1
fi

# Check if PHP is installed
if ! command -v php &> /dev/null; then
    echo -e "${RED}PHP is not installed!${NC}"
    echo "Install it with: sudo apt install php libapache2-mod-php php-mysql php-curl php-json php-mbstring"
    exit 1
fi

echo -e "${GREEN}✓ All prerequisites installed${NC}"
echo ""

echo -e "${YELLOW}Step 2: Copying backend files...${NC}"

# Check if source exists
if [ ! -d "$BACKEND_SRC" ]; then
    echo -e "${RED}Backend source not found at: $BACKEND_SRC${NC}"
    exit 1
fi

# Remove old backend if exists
if [ -d "$BACKEND_DEST" ]; then
    echo -e "${YELLOW}Removing old backend...${NC}"
    sudo rm -rf "$BACKEND_DEST"
fi

# Copy backend
sudo cp -r "$BACKEND_SRC" "$BACKEND_DEST"
echo -e "${GREEN}✓ Backend files copied${NC}"
echo ""

echo -e "${YELLOW}Step 3: Setting permissions...${NC}"

# Set ownership (www-data is the default Apache user on Debian)
sudo chown -R www-data:www-data "$BACKEND_DEST"
sudo chmod -R 755 "$BACKEND_DEST"

# Create upload directories
sudo mkdir -p "$BACKEND_DEST/uploads/gallery"
sudo mkdir -p "$BACKEND_DEST/uploads/avatars"
sudo chmod -R 777 "$BACKEND_DEST/uploads"

echo -e "${GREEN}✓ Permissions set${NC}"
echo ""

echo -e "${YELLOW}Step 4: Checking Apache configuration...${NC}"

# Enable mod_rewrite if not already enabled
if ! apache2ctl -M 2>/dev/null | grep -q "rewrite_module"; then
    echo -e "${YELLOW}Enabling mod_rewrite...${NC}"
    sudo a2enmod rewrite
fi

# Check AllowOverride in default site config
if [ -f /etc/apache2/sites-available/000-default.conf ]; then
    if ! grep -q "AllowOverride All" /etc/apache2/sites-available/000-default.conf; then
        echo -e "${YELLOW}⚠ AllowOverride might not be set to 'All'${NC}"
        echo "You may need to update /etc/apache2/sites-available/000-default.conf"
        echo "Add inside <VirtualHost>:"
        echo "  <Directory /var/www/html>"
        echo "      AllowOverride All"
        echo "  </Directory>"
    fi
fi

echo -e "${GREEN}✓ Apache configuration checked${NC}"
echo ""

echo -e "${YELLOW}Step 5: Starting services...${NC}"

# Start Apache
sudo systemctl start apache2
echo -e "${GREEN}✓ Apache started${NC}"

# Start MariaDB
sudo systemctl start mariadb
echo -e "${GREEN}✓ MariaDB started${NC}"

# Restart Apache to apply any configuration changes
sudo systemctl restart apache2
echo -e "${GREEN}✓ Apache restarted${NC}"

echo ""

echo -e "${YELLOW}Step 6: Database setup...${NC}"

# Check if database exists
DB_EXISTS=$(sudo mysql -u root -e "SHOW DATABASES LIKE 'weedx';" | grep weedx || true)

if [ -z "$DB_EXISTS" ]; then
    echo -e "${YELLOW}Creating database and importing schema...${NC}"
    
    # Import schema
    sudo mysql -u root < "$BACKEND_DEST/database/schema.sql"
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ Database created and schema imported${NC}"
    else
        echo -e "${RED}✗ Failed to import database schema${NC}"
        echo "Try manually: sudo mysql -u root < $BACKEND_DEST/database/schema.sql"
    fi
else
    echo -e "${GREEN}✓ Database 'weedx' already exists${NC}"
fi

echo ""

echo -e "${YELLOW}Step 7: Testing backend...${NC}"

# Wait a moment for services to fully start
sleep 2

# Test endpoint
RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost/weedx-backend/robot/status)

if [ "$RESPONSE" = "401" ] || [ "$RESPONSE" = "200" ]; then
    echo -e "${GREEN}✓ Backend is responding correctly (HTTP $RESPONSE)${NC}"
else
    echo -e "${RED}✗ Backend returned unexpected code: HTTP $RESPONSE${NC}"
    echo "Check Apache error log: sudo tail /var/log/apache2/error.log"
fi

echo ""
echo "=========================================="
echo -e "${GREEN}Deployment Complete!${NC}"
echo "=========================================="
echo ""

# Get local IP
LOCAL_IP=$(ip addr show | grep "inet " | grep -v 127.0.0.1 | awk '{print $2}' | cut -d/ -f1 | head -n1)

echo "Backend URL: http://localhost/weedx-backend/"
echo "Local IP: http://$LOCAL_IP/weedx-backend/"
echo ""
echo "Demo Credentials:"
echo "  Email: admin@weedx.com"
echo "  Password: admin123"
echo ""
echo "Update Android app Constants.kt:"
echo "  const val BASE_URL = \"http://$LOCAL_IP/weedx-backend/\""
echo ""
echo "Test login:"
echo "  curl -X POST http://localhost/weedx-backend/auth/login \\"
echo "    -H \"Content-Type: application/json\" \\"
echo "    -d '{\"email\":\"admin@weedx.com\",\"password\":\"admin123\",\"firebaseToken\":\"test\"}'"
echo ""
echo "View logs:"
echo "  sudo tail -f /var/log/apache2/error.log"
echo ""
echo "Enable services on boot:"
echo "  sudo systemctl enable apache2"
echo "  sudo systemctl enable mariadb"
echo ""
echo -e "${GREEN}Happy coding! 🚀${NC}"
