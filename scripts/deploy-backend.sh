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

# Detect distro-specific Apache binary and service names
APACHE_BIN=""
APACHE_SERVICE=""
APACHE_CTL=""
WEB_USER_GROUP=""
DB_CLIENT="mysql"
WEB_ROOT=""
APACHE_ERROR_LOG="/var/log/apache2/error.log"

if command -v apache2 >/dev/null 2>&1; then
    APACHE_BIN="apache2"
    APACHE_SERVICE="apache2"
    APACHE_CTL="apache2ctl"
    WEB_ROOT="/var/www/html"
elif command -v httpd >/dev/null 2>&1; then
    APACHE_BIN="httpd"
    APACHE_SERVICE="httpd"
    APACHE_CTL="apachectl"
    WEB_ROOT="/srv/http"
    APACHE_ERROR_LOG="/var/log/httpd/error_log"
fi

if id -u www-data >/dev/null 2>&1; then
    WEB_USER_GROUP="www-data:www-data"
elif id -u http >/dev/null 2>&1; then
    WEB_USER_GROUP="http:http"
fi

if command -v mariadb >/dev/null 2>&1; then
    DB_CLIENT="mariadb"
fi

# Check if running as root
if [ "$EUID" -eq 0 ]; then 
    echo -e "${RED}Please do not run as root. Use sudo when prompted.${NC}"
    exit 1
fi

# Get the script directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
BACKEND_SRC="$SCRIPT_DIR/../xampp/htdocs/backend"
if [ -z "$WEB_ROOT" ]; then
    WEB_ROOT="/var/www/html"
fi

BACKEND_DEST="$WEB_ROOT/weedx-backend"
BACKEND_PARENT="$(dirname "$BACKEND_DEST")"

echo -e "${YELLOW}Step 1: Checking prerequisites...${NC}"

# Check if Apache is installed
if [ -z "$APACHE_BIN" ]; then
    echo -e "${RED}Apache is not installed!${NC}"
    echo "Install it with one of:"
    echo "  Debian/Ubuntu: sudo apt install apache2"
    echo "  Arch Linux:    sudo pacman -S apache"
    exit 1
fi

# Check if MariaDB/MySQL is installed
if ! command -v mysql &> /dev/null; then
    echo -e "${RED}MariaDB/MySQL is not installed!${NC}"
    echo "Install it with one of:"
    echo "  Debian/Ubuntu: sudo apt install mariadb-server"
    echo "  Arch Linux:    sudo pacman -S mariadb"
    exit 1
fi

# Check if PHP is installed
if ! command -v php &> /dev/null; then
    echo -e "${RED}PHP is not installed!${NC}"
    echo "Install it with one of:"
    echo "  Debian/Ubuntu: sudo apt install php libapache2-mod-php php-mysql php-curl php-json php-mbstring"
    echo "  Arch Linux:    sudo pacman -S php php-apache"
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

# Ensure deployment parent path exists
sudo mkdir -p "$BACKEND_PARENT"

# Copy backend
sudo cp -r "$BACKEND_SRC" "$BACKEND_DEST"
echo -e "${GREEN}✓ Backend files copied${NC}"
echo ""

echo -e "${YELLOW}Step 3: Setting permissions...${NC}"

# Set ownership (www-data is the default Apache user on Debian)
if [ -n "$WEB_USER_GROUP" ]; then
    sudo chown -R "$WEB_USER_GROUP" "$BACKEND_DEST"
else
    echo -e "${YELLOW}⚠ Could not detect Apache user (www-data/http); skipping chown${NC}"
fi
sudo chmod -R 755 "$BACKEND_DEST"

# Create upload directories
sudo mkdir -p "$BACKEND_DEST/uploads/gallery"
sudo mkdir -p "$BACKEND_DEST/uploads/avatars"
sudo chmod -R 777 "$BACKEND_DEST/uploads"

echo -e "${GREEN}✓ Permissions set${NC}"
echo ""

echo -e "${YELLOW}Step 4: Checking Apache configuration...${NC}"

# Arch Linux (httpd): enable PHP integration if module exists but not configured
if [ "$APACHE_SERVICE" = "httpd" ] && [ -f /etc/httpd/conf/httpd.conf ]; then
    if grep -q "^LoadModule mpm_event_module" /etc/httpd/conf/httpd.conf && grep -q "^#LoadModule mpm_prefork_module" /etc/httpd/conf/httpd.conf; then
        echo -e "${YELLOW}Switching httpd MPM from event to prefork for mod_php compatibility...${NC}"
        sudo sed -i 's/^LoadModule mpm_event_module/#LoadModule mpm_event_module/' /etc/httpd/conf/httpd.conf
        sudo sed -i 's/^#LoadModule mpm_prefork_module/LoadModule mpm_prefork_module/' /etc/httpd/conf/httpd.conf
    fi

    if grep -q "^#LoadModule php_module" /etc/httpd/conf/httpd.conf; then
        echo -e "${YELLOW}Enabling php_module for httpd...${NC}"
        sudo sed -i 's/^#LoadModule php_module/LoadModule php_module/' /etc/httpd/conf/httpd.conf
    elif ! grep -q "^LoadModule php_module" /etc/httpd/conf/httpd.conf; then
        echo -e "${YELLOW}Adding php_module directive for httpd...${NC}"
        sudo sed -i '/^# Dynamic Shared Object (DSO) Support/ a LoadModule php_module modules/libphp.so' /etc/httpd/conf/httpd.conf
    fi

    if [ -f /etc/httpd/conf/extra/php_module.conf ] && ! grep -q "Include conf/extra/php_module.conf" /etc/httpd/conf/httpd.conf; then
        echo -e "${YELLOW}Enabling PHP module include for httpd...${NC}"
        echo "Include conf/extra/php_module.conf" | sudo tee -a /etc/httpd/conf/httpd.conf >/dev/null
    fi

    if grep -q "^#LoadModule rewrite_module" /etc/httpd/conf/httpd.conf; then
        echo -e "${YELLOW}Enabling mod_rewrite for httpd...${NC}"
        sudo sed -i 's/^#LoadModule rewrite_module/LoadModule rewrite_module/' /etc/httpd/conf/httpd.conf
    fi

    if grep -q "<Directory \"/srv/http\">" /etc/httpd/conf/httpd.conf; then
        echo -e "${YELLOW}Setting AllowOverride All for /srv/http...${NC}"
        sudo sed -i '/<Directory \"\/srv\/http\">/,/<\/Directory>/ s/AllowOverride[[:space:]]\+None/AllowOverride All/' /etc/httpd/conf/httpd.conf
        sudo sed -i '/<Directory \"\/srv\/http\">/,/<\/Directory>/ s/AllowOverride[[:space:]]\+none/AllowOverride All/' /etc/httpd/conf/httpd.conf
    fi
fi

# Enable mod_rewrite if not already enabled
if [ -n "$APACHE_CTL" ] && ! "$APACHE_CTL" -M 2>/dev/null | grep -q "rewrite_module"; then
    if command -v a2enmod >/dev/null 2>&1; then
        echo -e "${YELLOW}Enabling mod_rewrite...${NC}"
        sudo a2enmod rewrite
    else
        echo -e "${YELLOW}⚠ rewrite module is not detected and auto-enable is unavailable on this distro${NC}"
    fi
fi

# Check AllowOverride in default site config
if [ -f /etc/apache2/sites-available/000-default.conf ]; then
    if ! grep -q "AllowOverride All" /etc/apache2/sites-available/000-default.conf; then
        echo -e "${YELLOW}⚠ AllowOverride might not be set to 'All'${NC}"
        echo "You may need to update /etc/apache2/sites-available/000-default.conf"
        echo "Add inside <VirtualHost>:"
        echo "  <Directory $WEB_ROOT>"
        echo "      AllowOverride All"
        echo "  </Directory>"
    fi
fi

echo -e "${GREEN}✓ Apache configuration checked${NC}"
echo ""

echo -e "${YELLOW}Step 5: Starting services...${NC}"

# Start Apache/httpd
sudo systemctl start "$APACHE_SERVICE"
echo -e "${GREEN}✓ Apache started (${APACHE_SERVICE})${NC}"

# Start MariaDB
sudo systemctl start mariadb
echo -e "${GREEN}✓ MariaDB started${NC}"

# Restart Apache/httpd to apply any configuration changes
sudo systemctl restart "$APACHE_SERVICE"
echo -e "${GREEN}✓ Apache restarted (${APACHE_SERVICE})${NC}"

echo ""

echo -e "${YELLOW}Step 6: Database setup...${NC}"

# Check if database exists
DB_EXISTS=$(sudo "$DB_CLIENT" -u root -e "SHOW DATABASES LIKE 'weedx';" | grep weedx || true)

if [ -z "$DB_EXISTS" ]; then
    echo -e "${YELLOW}Creating database and importing schema...${NC}"
    
    # Import schema
    if sudo "$DB_CLIENT" -u root < "$BACKEND_DEST/database/schema.sql"; then
        echo -e "${GREEN}✓ Database created and schema imported${NC}"
    else
        echo -e "${YELLOW}⚠ Schema import completed with errors (usually seed-data mismatch)${NC}"
        echo "Database structure may still be usable. Review and fix: $BACKEND_DEST/database/schema.sql"
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

# Fallback for environments where rewrite isn't active yet
if [ "$RESPONSE" = "404" ]; then
    RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost/weedx-backend/index.php?request=robot/status")
fi

if [ "$RESPONSE" = "401" ] || [ "$RESPONSE" = "200" ]; then
    echo -e "${GREEN}✓ Backend is responding correctly (HTTP $RESPONSE)${NC}"
else
    echo -e "${RED}✗ Backend returned unexpected code: HTTP $RESPONSE${NC}"
    echo "Check Apache error log: sudo tail $APACHE_ERROR_LOG"
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
echo "Deployed path: $BACKEND_DEST"
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
echo "  sudo tail -f $APACHE_ERROR_LOG"
echo ""
echo "Enable services on boot:"
echo "  sudo systemctl enable $APACHE_SERVICE"
echo "  sudo systemctl enable mariadb"
echo ""
echo -e "${GREEN}Happy coding! 🚀${NC}"
