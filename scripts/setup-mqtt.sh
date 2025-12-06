#!/bin/bash
# WeedX MQTT Setup and Deployment Script
# This script sets up the MQTT system for WeedX

set -e  # Exit on error

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
BACKEND_SOURCE="$PROJECT_ROOT/xampp/htdocs/backend"
BACKEND_DEPLOY="/var/www/html/weedx-backend"

echo "========================================"
echo "WeedX MQTT Setup & Deployment"
echo "========================================"
echo ""

# Check if running as root
if [ "$EUID" -eq 0 ]; then 
    echo "❌ Please do not run this script as root"
    echo "   The script will request sudo when needed"
    exit 1
fi

# Function to check if a command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Step 1: Check Mosquitto installation
echo "Step 1: Checking Mosquitto MQTT broker..."
if command_exists mosquitto; then
    echo "✓ Mosquitto is installed"
    mosquitto -h | head -n 1
else
    echo "❌ Mosquitto not found. Installing..."
    sudo apt-get update
    sudo apt-get install -y mosquitto mosquitto-clients
    echo "✓ Mosquitto installed"
fi
echo ""

# Step 2: Start and enable Mosquitto service
echo "Step 2: Starting Mosquitto service..."
sudo systemctl enable mosquitto
sudo systemctl start mosquitto
if sudo systemctl is-active --quiet mosquitto; then
    echo "✓ Mosquitto service is running"
else
    echo "❌ Failed to start Mosquitto service"
    exit 1
fi
echo ""

# Step 3: Check PHP and Composer
echo "Step 3: Checking PHP and Composer..."
if command_exists php; then
    echo "✓ PHP is installed: $(php -v | head -n 1)"
else
    echo "❌ PHP not found. Please install PHP first."
    exit 1
fi

cd "$BACKEND_SOURCE"
if [ ! -f "composer.phar" ]; then
    echo "Downloading Composer..."
    wget https://getcomposer.org/composer-stable.phar -O composer.phar
fi

if [ ! -d "vendor" ]; then
    echo "Installing PHP dependencies..."
    php composer.phar install
    echo "✓ PHP dependencies installed"
else
    echo "✓ PHP dependencies already installed"
fi
echo ""

# Step 4: Deploy backend to production
echo "Step 4: Deploying backend to production..."
if [ -d "$BACKEND_DEPLOY" ]; then
    echo "Updating backend files..."
    sudo cp -r "$BACKEND_SOURCE"/* "$BACKEND_DEPLOY/"
    sudo chown -R www-data:www-data "$BACKEND_DEPLOY"
    echo "✓ Backend deployed to $BACKEND_DEPLOY"
else
    echo "❌ Backend deployment directory not found: $BACKEND_DEPLOY"
    echo "   Please run the main deployment script first."
    exit 1
fi
echo ""

# Step 5: Install systemd service
echo "Step 5: Installing MQTT subscriber service..."
if [ -f "$SCRIPT_DIR/weedx-mqtt.service" ]; then
    sudo cp "$SCRIPT_DIR/weedx-mqtt.service" /etc/systemd/system/
    sudo systemctl daemon-reload
    echo "✓ Systemd service installed"
else
    echo "❌ Service file not found: $SCRIPT_DIR/weedx-mqtt.service"
    exit 1
fi
echo ""

# Step 6: Start MQTT subscriber service
echo "Step 6: Starting MQTT subscriber service..."
sudo systemctl enable weedx-mqtt
sudo systemctl restart weedx-mqtt

sleep 2

if sudo systemctl is-active --quiet weedx-mqtt; then
    echo "✓ MQTT subscriber service is running"
    echo ""
    echo "Service status:"
    sudo systemctl status weedx-mqtt --no-pager | head -n 10
else
    echo "❌ Failed to start MQTT subscriber service"
    echo "Check logs with: sudo journalctl -u weedx-mqtt -f"
    exit 1
fi
echo ""

# Step 7: Make test script executable
echo "Step 7: Setting up test script..."
chmod +x "$SCRIPT_DIR/test-mqtt-publish.sh"
echo "✓ Test script is ready"
echo ""

# Success message
echo "========================================"
echo "✅ MQTT Setup Complete!"
echo "========================================"
echo ""
echo "MQTT Topics:"
echo "  - weedx/robot/status      → Robot status updates"
echo "  - weedx/robot/location    → GPS location data"
echo "  - weedx/robot/battery     → Battery & herbicide levels"
echo "  - weedx/weed/detection    → Weed detection data"
echo "  - weedx/sensor/weather    → Weather sensor data"
echo "  - weedx/sensor/soil       → Soil sensor data"
echo "  - weedx/alert             → System alerts"
echo ""
echo "Useful Commands:"
echo "  Test MQTT:        bash $SCRIPT_DIR/test-mqtt-publish.sh"
echo "  View logs:        sudo journalctl -u weedx-mqtt -f"
echo "  Restart service:  sudo systemctl restart weedx-mqtt"
echo "  Stop service:     sudo systemctl stop weedx-mqtt"
echo "  Service status:   sudo systemctl status weedx-mqtt"
echo ""
echo "Next Steps:"
echo "  1. Run test script to verify: bash $SCRIPT_DIR/test-mqtt-publish.sh"
echo "  2. Configure your robot to publish to these MQTT topics"
echo "  3. Monitor logs to ensure data is being processed"
echo ""
