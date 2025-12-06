# WeedX MQTT Integration Guide

## Overview

The WeedX system uses MQTT (Message Queuing Telemetry Transport) for real-time communication between the farming robot and the backend system. The robot publishes sensor data, weed detections, and status updates to MQTT topics, which are then consumed by a PHP subscriber service and stored in MySQL.

## Architecture

```
Robot Hardware → MQTT Broker (Mosquitto) → PHP Subscriber → MySQL Database → REST API → Android App
```

## MQTT Topics

| Topic | Description | Data Format |
|-------|-------------|-------------|
| `weedx/robot/status` | Robot operational status | JSON with status, activity, user_id |
| `weedx/robot/location` | GPS coordinates and movement | JSON with lat, lon, speed, heading |
| `weedx/robot/battery` | Power and resource levels | JSON with battery %, herbicide % |
| `weedx/weed/detection` | Detected weed information | JSON with weed data + optional image |
| `weedx/sensor/soil` | Soil quality measurements | JSON with moisture, pH, NPK values |
| `weedx/alert` | System alerts and notifications | JSON with type, severity, message |

**Note:** Weather data is fetched via backend API, not through MQTT.

## Installation & Setup

### Prerequisites

- Raspberry Pi (or Linux server) with Apache and MySQL installed
- PHP 7.4 or higher
- Internet connection for package installation

### Quick Setup

Run the automated setup script:

```bash
cd /home/umersani/weedx-mobile/scripts
bash setup-mqtt.sh
```

This script will:
1. Install Mosquitto MQTT broker
2. Install PHP MQTT client library via Composer
3. Deploy backend to production directory
4. Install systemd service for MQTT subscriber
5. Start and enable all services

### Manual Setup

If you prefer manual installation:

#### 1. Install Mosquitto

```bash
sudo apt-get update
sudo apt-get install -y mosquitto mosquitto-clients
sudo systemctl enable mosquitto
sudo systemctl start mosquitto
```

#### 2. Install PHP Dependencies

```bash
cd /home/umersani/weedx-mobile/xampp/htdocs/backend
wget https://getcomposer.org/composer-stable.phar -O composer.phar
php composer.phar install
```

#### 3. Deploy Backend

```bash
sudo cp -r /home/umersani/weedx-mobile/xampp/htdocs/backend/* /var/www/html/weedx-backend/
sudo chown -R www-data:www-data /var/www/html/weedx-backend
```

#### 4. Install Systemd Service

```bash
sudo cp /home/umersani/weedx-mobile/scripts/weedx-mqtt.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable weedx-mqtt
sudo systemctl start weedx-mqtt
```

## Testing the System

### Test MQTT Publishing

Run the test script to simulate robot data:

```bash
cd /home/umersani/weedx-mobile/scripts
bash test-mqtt-publish.sh
```

This will publish sample data to all MQTT topics.

### Monitor MQTT Subscriber Logs

Watch real-time processing:

```bash
sudo journalctl -u weedx-mqtt -f
```

### Test Individual Messages

Publish a single test message:

```bash
# Robot status update
mosquitto_pub -h localhost -p 1883 -t "weedx/robot/status" -m '{
    "user_id": 1,
    "status": "active",
    "activity": "Scanning wheat field"
}'

# Weed detection
mosquitto_pub -h localhost -p 1883 -t "weedx/weed/detection" -m '{
    "user_id": 1,
    "weed_type": "Broadleaf Weed",
    "crop_type": "Wheat",
    "confidence": 92.5,
    "latitude": 31.5204,
    "longitude": 74.3587
}'
```

### Subscribe to Topics (Monitor)

Listen to all messages on a topic:

```bash
mosquitto_sub -h localhost -p 1883 -t "weedx/#" -v
```

## Message Formats

### Robot Status

```json
{
    "user_id": 1,
    "status": "active|idle|charging|maintenance|offline",
    "activity": "Description of current activity"
}
```

### Robot Location

```json
{
    "latitude": 31.5204,
    "longitude": 74.3587,
    "speed": 3.5,
    "heading": 45.0
}
```

### Battery Level

```json
{
    "battery": 85,
    "herbicide": 70
}
```

### Weed Detection (Without Image)

```json
{
    "user_id": 1,
    "weed_type": "Broadleaf Weed",
    "crop_type": "Wheat",
    "confidence": 92.5,
    "latitude": 31.5204,
    "longitude": 74.3587
}
```

### Weed Detection (With Image)

```json
{
    "user_id": 1,
    "weed_type": "Grass Weed",
    "crop_type": "Corn",
    "confidence": 88.3,
    "latitude": 31.5210,
    "longitude": 74.3590,
    "image_base64": "base64_encoded_image_data_here",
    "image_mime_type": "image/jpeg"
}
```

### Soil Data

```json
{
    "moisture": 45.0,
    "temperature": 22.0,
    "ph": 6.5,
    "nitrogen": 50,
    "phosphorus": 30,
    "potassium": 40,
    "organic_matter": 3.5
}
```

### Alert

```json
{
    "user_id": 1,
    "type": "battery|fault|maintenance|detection",
    "severity": "info|warning|critical",
    "message": "Alert message text"
}
```

## Service Management

### Start/Stop/Restart

```bash
sudo systemctl start weedx-mqtt    # Start the service
sudo systemctl stop weedx-mqtt     # Stop the service
sudo systemctl restart weedx-mqtt  # Restart the service
sudo systemctl status weedx-mqtt   # Check status
```

### Enable/Disable Auto-start

```bash
sudo systemctl enable weedx-mqtt   # Auto-start on boot
sudo systemctl disable weedx-mqtt  # Disable auto-start
```

### View Logs

```bash
# Follow logs in real-time
sudo journalctl -u weedx-mqtt -f

# View last 100 lines
sudo journalctl -u weedx-mqtt -n 100

# View logs from today
sudo journalctl -u weedx-mqtt --since today

# View logs with timestamps
sudo journalctl -u weedx-mqtt -o short-iso
```

## Robot Integration

### Python Example (Robot Side)

```python
import paho.mqtt.client as mqtt
import json
import base64
from datetime import datetime

# MQTT Configuration
MQTT_BROKER = "raspberrypi.mullet-bull.ts.net"  # Or your Pi's IP
MQTT_PORT = 1883
USER_ID = 1  # Your user ID from the database

# Connect to MQTT broker
client = mqtt.Client()
client.connect(MQTT_BROKER, MQTT_PORT, 60)

# Publish robot status
def publish_status(status, activity):
    topic = "weedx/robot/status"
    payload = {
        "user_id": USER_ID,
        "status": status,
        "activity": activity
    }
    client.publish(topic, json.dumps(payload))

# Publish location
def publish_location(lat, lon, speed, heading):
    topic = "weedx/robot/location"
    payload = {
        "latitude": lat,
        "longitude": lon,
        "speed": speed,
        "heading": heading
    }
    client.publish(topic, json.dumps(payload))

# Publish weed detection with image
def publish_weed_detection(weed_type, crop_type, confidence, lat, lon, image_path=None):
    topic = "weedx/weed/detection"
    payload = {
        "user_id": USER_ID,
        "weed_type": weed_type,
        "crop_type": crop_type,
        "confidence": confidence,
        "latitude": lat,
        "longitude": lon
    }
    
    # Add image if provided
    if image_path:
        with open(image_path, "rb") as img_file:
            image_data = base64.b64encode(img_file.read()).decode('utf-8')
            payload["image_base64"] = image_data
            payload["image_mime_type"] = "image/jpeg"
    
    client.publish(topic, json.dumps(payload))

# Publish battery level
def publish_battery(battery, herbicide):
    topic = "weedx/robot/battery"
    payload = {
        "battery": battery,
        "herbicide": herbicide
    }
    client.publish(topic, json.dumps(payload))

# Publish alert
def publish_alert(alert_type, severity, message):
    topic = "weedx/alert"
    payload = {
        "user_id": USER_ID,
        "type": alert_type,
        "severity": severity,
        "message": message
    }
    client.publish(topic, json.dumps(payload))

# Example usage
publish_status("active", "Scanning wheat field")
publish_location(31.5204, 74.3587, 3.5, 45.0)
publish_weed_detection("Broadleaf Weed", "Wheat", 92.5, 31.5204, 74.3587)
publish_battery(85, 70)
publish_alert("battery", "warning", "Battery at 85%")

# Keep connection alive
client.loop_start()
```

### Node.js Example

```javascript
const mqtt = require('mqtt');
const fs = require('fs');

const MQTT_BROKER = 'mqtt://raspberrypi.mullet-bull.ts.net:1883';
const USER_ID = 1;

const client = mqtt.connect(MQTT_BROKER);

client.on('connect', () => {
    console.log('Connected to MQTT broker');
    
    // Publish robot status
    client.publish('weedx/robot/status', JSON.stringify({
        user_id: USER_ID,
        status: 'active',
        activity: 'Scanning wheat field'
    }));
    
    // Publish weed detection
    client.publish('weedx/weed/detection', JSON.stringify({
        user_id: USER_ID,
        weed_type: 'Broadleaf Weed',
        crop_type: 'Wheat',
        confidence: 92.5,
        latitude: 31.5204,
        longitude: 74.3587
    }));
});
```

## Troubleshooting

### Service Won't Start

```bash
# Check service status
sudo systemctl status weedx-mqtt

# Check logs for errors
sudo journalctl -u weedx-mqtt -n 50

# Common issues:
# 1. Mosquitto not running: sudo systemctl start mosquitto
# 2. MySQL not running: sudo systemctl start mysql
# 3. Missing vendor directory: cd /var/www/html/weedx-backend && php composer.phar install
```

### No Data in Database

1. Check if MQTT subscriber is running:
   ```bash
   sudo systemctl status weedx-mqtt
   ```

2. Monitor logs for errors:
   ```bash
   sudo journalctl -u weedx-mqtt -f
   ```

3. Test database connection:
   ```bash
   cd /var/www/html/weedx-backend
   php -r "require 'config/database.php'; \$db = new Database(); \$db->getConnection();"
   ```

4. Verify MQTT messages are being published:
   ```bash
   mosquitto_sub -h localhost -p 1883 -t "weedx/#" -v
   ```

### Mosquitto Connection Refused

```bash
# Check if Mosquitto is running
sudo systemctl status mosquitto

# Start if not running
sudo systemctl start mosquitto

# Check port
sudo netstat -tulpn | grep 1883
```

### Permission Errors

```bash
# Fix backend permissions
sudo chown -R www-data:www-data /var/www/html/weedx-backend

# Fix log permissions
sudo mkdir -p /var/www/html/weedx-backend/logs
sudo chown -R www-data:www-data /var/www/html/weedx-backend/logs
```

## Multi-User Support

The MQTT subscriber now supports multiple users. Each message can include a `user_id` field:

```json
{
    "user_id": 1,
    "weed_type": "Broadleaf Weed",
    ...
}
```

- If `user_id` is provided, data is associated with that user
- If `user_id` is omitted, defaults to user ID 1
- Ensures data isolation between different users
- Robot activity logs are linked to specific users

## Security Considerations

### Production Deployment

For production, consider:

1. **Enable MQTT Authentication**:
   ```bash
   sudo mosquitto_passwd -c /etc/mosquitto/passwd weedx
   ```
   
   Then update `/etc/mosquitto/mosquitto.conf`:
   ```
   allow_anonymous false
   password_file /etc/mosquitto/passwd
   ```

2. **Use TLS/SSL**: Configure Mosquitto with SSL certificates for encrypted communication

3. **Firewall Rules**: Restrict MQTT port (1883) access to known IPs

4. **Update subscriber.php**: Add MQTT username/password in the connection settings

## Performance Optimization

### High-Volume Data

If processing many messages per second:

1. Increase QoS level in subscriber.php
2. Add message queuing/buffering
3. Use batch database inserts
4. Monitor system resources: `htop`, `df -h`

### Database Indexing

The schema includes indexes on frequently queried columns. For better performance:

```sql
-- Add custom indexes if needed
CREATE INDEX idx_weed_user_date ON weed_detections(user_id, detected_at);
CREATE INDEX idx_alerts_user ON alerts(user_id, created_at);
```

## Monitoring

### Check MQTT Broker Stats

```bash
# Monitor active connections
sudo systemctl status mosquitto

# View Mosquitto logs
sudo journalctl -u mosquitto -f
```

### Database Growth

```bash
# Check database size
mysql -u root -p -e "SELECT table_name, ROUND((data_length + index_length) / 1024 / 1024, 2) AS 'Size (MB)' FROM information_schema.tables WHERE table_schema='weedx' ORDER BY (data_length + index_length) DESC;"
```

## Support

For issues or questions:
- Check logs: `sudo journalctl -u weedx-mqtt -f`
- Review database: MySQL client or phpMyAdmin
- Test MQTT: `mosquitto_sub -h localhost -t "weedx/#" -v`

## Files Reference

| File | Purpose |
|------|---------|
| `xampp/htdocs/backend/mqtt/subscriber.php` | PHP MQTT subscriber (source) |
| `/var/www/html/weedx-backend/mqtt/subscriber.php` | PHP MQTT subscriber (production) |
| `scripts/setup-mqtt.sh` | Automated setup script |
| `scripts/test-mqtt-publish.sh` | Test publishing script |
| `scripts/weedx-mqtt.service` | Systemd service file |
| `/etc/systemd/system/weedx-mqtt.service` | Installed service file |

## License

Part of the WeedX precision farming system.
