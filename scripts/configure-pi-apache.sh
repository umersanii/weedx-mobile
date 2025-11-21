#!/bin/bash

# Apache Configuration Script for Raspberry Pi
# Run this ON the Raspberry Pi to configure Apache for WeedX backend

set -e

echo "=========================================="
echo "WeedX - Apache Configuration for Pi"
echo "=========================================="
echo ""

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Check if running as root
if [ "$EUID" -ne 0 ]; then 
    echo -e "${RED}Please run as root or with sudo${NC}"
    exit 1
fi

echo -e "${YELLOW}Step 1: Installing required packages...${NC}"

# Update package list
apt-get update

# Install Apache, MariaDB, PHP
apt-get install -y apache2 mariadb-server php libapache2-mod-php php-mysql php-curl php-json php-mbstring

echo -e "${GREEN}✓ Packages installed${NC}"
echo ""

echo -e "${YELLOW}Step 2: Enabling Apache modules...${NC}"

# Enable required modules
a2enmod rewrite
a2enmod headers
a2enmod php8.2  # Adjust version if needed

echo -e "${GREEN}✓ Modules enabled${NC}"
echo ""

echo -e "${YELLOW}Step 3: Configuring Apache...${NC}"

# Backup original config
if [ ! -f /etc/apache2/apache2.conf.bak ]; then
    cp /etc/apache2/apache2.conf /etc/apache2/apache2.conf.bak
fi

# Update directory configuration for AllowOverride
if ! grep -q "AllowOverride All" /etc/apache2/apache2.conf; then
    cat >> /etc/apache2/apache2.conf << 'EOF'

# WeedX Configuration
<Directory /var/www/html>
    Options Indexes FollowSymLinks
    AllowOverride All
    Require all granted
</Directory>
EOF
    echo -e "${GREEN}✓ AllowOverride configured${NC}"
else
    echo -e "${GREEN}✓ AllowOverride already configured${NC}"
fi

# Create .htaccess for backend if not exists
mkdir -p /var/www/html/weedx-backend
cat > /var/www/html/weedx-backend/.htaccess << 'EOF'
# Enable CORS
Header set Access-Control-Allow-Origin "*"
Header set Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS"
Header set Access-Control-Allow-Headers "Content-Type, Authorization"

# Handle preflight requests
RewriteEngine On
RewriteCond %{REQUEST_METHOD} OPTIONS
RewriteRule ^(.*)$ $1 [R=200,L]

# Remove .php extension from URLs
RewriteCond %{REQUEST_FILENAME} !-d
RewriteCond %{REQUEST_FILENAME}\.php -f
RewriteRule ^(.*)$ $1.php [L]

# Redirect everything to index.php if file doesn't exist
RewriteCond %{REQUEST_FILENAME} !-f
RewriteCond %{REQUEST_FILENAME} !-d
RewriteRule ^(.*)$ index.php [QSA,L]
EOF

echo -e "${GREEN}✓ .htaccess created${NC}"
echo ""

echo -e "${YELLOW}Step 4: Configuring PHP...${NC}"

# Find php.ini location
PHP_INI=$(php --ini | grep "Loaded Configuration File" | cut -d: -f2 | xargs)

if [ -n "$PHP_INI" ]; then
    # Backup original
    if [ ! -f "${PHP_INI}.bak" ]; then
        cp "$PHP_INI" "${PHP_INI}.bak"
    fi
    
    # Update settings
    sed -i 's/upload_max_filesize = .*/upload_max_filesize = 20M/' "$PHP_INI"
    sed -i 's/post_max_size = .*/post_max_size = 20M/' "$PHP_INI"
    sed -i 's/max_execution_time = .*/max_execution_time = 300/' "$PHP_INI"
    
    echo -e "${GREEN}✓ PHP configured${NC}"
else
    echo -e "${YELLOW}⚠ Could not find php.ini${NC}"
fi

echo ""

echo -e "${YELLOW}Step 5: Configuring MariaDB...${NC}"

# Secure MariaDB installation (basic)
systemctl start mariadb
systemctl enable mariadb

# Create WeedX database and user
mysql -u root << 'EOSQL' 2>/dev/null || true
CREATE DATABASE IF NOT EXISTS weedx;
CREATE USER IF NOT EXISTS 'weedx_user'@'localhost' IDENTIFIED BY 'weedx_pass_2024';
GRANT ALL PRIVILEGES ON weedx.* TO 'weedx_user'@'localhost';
FLUSH PRIVILEGES;
EOSQL

echo -e "${GREEN}✓ MariaDB configured${NC}"
echo ""

echo -e "${YELLOW}Step 6: Setting up Tailscale firewall rules...${NC}"

# Allow HTTP traffic (Tailscale automatically handles network routing)
if command -v ufw &> /dev/null; then
    ufw allow 80/tcp
    ufw allow 443/tcp
    echo -e "${GREEN}✓ Firewall rules updated (ufw)${NC}"
elif command -v iptables &> /dev/null; then
    iptables -A INPUT -p tcp --dport 80 -j ACCEPT
    iptables -A INPUT -p tcp --dport 443 -j ACCEPT
    echo -e "${GREEN}✓ Firewall rules updated (iptables)${NC}"
else
    echo -e "${YELLOW}⚠ No firewall detected${NC}"
fi

echo ""

echo -e "${YELLOW}Step 7: Restarting services...${NC}"

systemctl restart apache2
systemctl restart mariadb

echo -e "${GREEN}✓ Services restarted${NC}"
echo ""

echo -e "${YELLOW}Step 8: Testing configuration...${NC}"

# Test Apache
if systemctl is-active --quiet apache2; then
    echo -e "${GREEN}✓ Apache is running${NC}"
else
    echo -e "${RED}✗ Apache is not running${NC}"
fi

# Test MariaDB
if systemctl is-active --quiet mariadb; then
    echo -e "${GREEN}✓ MariaDB is running${NC}"
else
    echo -e "${RED}✗ MariaDB is not running${NC}"
fi

# Get Tailscale IP
TAILSCALE_IP=$(tailscale ip -4 2>/dev/null || echo "unknown")
echo ""
echo -e "${GREEN}Tailscale IP: $TAILSCALE_IP${NC}"

echo ""
echo "=========================================="
echo -e "${GREEN}Configuration Complete!${NC}"
echo "=========================================="
echo ""
echo -e "Next steps:"
echo "  1. Deploy backend files to /var/www/html/weedx-backend/"
echo "  2. Import database schema: mysql -u root weedx < schema.sql"
echo "  3. Test from another device: curl http://raspberrypi.mullet-bull.ts.net/weedx-backend/"
echo ""
echo -e "View logs:"
echo "  sudo tail -f /var/log/apache2/error.log"
echo "  sudo tail -f /var/log/apache2/access.log"
echo ""
echo -e "${GREEN}Done! 🎉${NC}"
