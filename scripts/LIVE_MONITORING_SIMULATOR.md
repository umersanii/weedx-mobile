# Live Monitoring Simulator

Python script that generates realistic dummy data for the WeedX live monitoring feature.

## Features

- **Realistic Data Generation**: Uses Faker library to create believable sensor data
- **Multiple MQTT Topics**: Publishes to all 6 WeedX MQTT topics
- **Continuous Operation**: Runs indefinitely with configurable intervals
- **Stateful Simulation**: Maintains robot state (battery drains, coverage increases, etc.)
- **User-Specific Data**: Can simulate different users by specifying user_id

## Prerequisites

Install required Python packages:

```bash
pip3 install paho-mqtt faker
```

## Usage

### Basic Usage (Default Settings)

```bash
cd /home/umersani/weedx-mobile/scripts
python3 live_monitoring_simulator.py
```

Default settings:
- MQTT Host: `localhost`
- MQTT Port: `1883`
- User ID: `1`
- Interval: `60` seconds

### Custom Configuration

```bash
# Run with custom user ID
python3 live_monitoring_simulator.py -u 2

# Run with faster interval (for testing)
python3 live_monitoring_simulator.py -i 10

# Run with custom MQTT broker
python3 live_monitoring_simulator.py --host 192.168.1.100 --port 1883

# Combine options
python3 live_monitoring_simulator.py -u 3 -i 30 --host raspberrypi.local
```

### Command Line Options

| Option | Description | Default |
|--------|-------------|---------|
| `--host` | MQTT broker hostname/IP | `localhost` |
| `--port`, `-p` | MQTT broker port | `1883` |
| `--user-id`, `-u` | User ID for simulated data | `1` |
| `--interval`, `-i` | Seconds between data cycles | `60` |

## What Data is Published

The simulator publishes to the following MQTT topics every cycle:

### 1. `weedx/robot/status`
- Battery level
- Operational status
- Current mode (autonomous/manual/scanning)
- Speed

### 2. `weedx/robot/location`
- GPS coordinates (latitude/longitude)
- Heading direction
- Speed
- Accuracy

### 3. `weedx/robot/battery`
- Battery level (percentage)
- Herbicide level (liters)
- Battery voltage

### 4. `weedx/weed/detection` (70% chance)
- Weed type (Broadleaf, Grass, Sedge, etc.)
- Confidence level
- Count of weeds detected
- Location coordinates
- Action taken (sprayed/marked/skipped)

### 5. `weedx/sensor/soil` (50% chance)
- Soil moisture
- Temperature
- pH level
- NPK values (Nitrogen, Phosphorus, Potassium)

### 6. `weedx/alert` (30% chance)
- Alert type and severity
- Alert message
- Location coordinates

## How It Works

### Simulation State

The simulator maintains realistic state that changes over time:

- **Battery**: Decreases by 0.1-0.5% per cycle
- **Herbicide**: Decreases by 0.05-0.2L per cycle
- **Coverage**: Increases by 0.01-0.05 hectares per cycle
- **GPS Position**: Slightly drifts to simulate robot movement
- **Total Weeds**: Accumulates detected weeds

### Data Flow

```
Python Simulator → MQTT Broker → PHP Subscriber → MySQL Database → REST API → Android App
```

1. **Simulator publishes** data to MQTT topics
2. **MQTT subscriber** (`weedx-mqtt.service`) receives and saves to MySQL
3. **Backend API** (`/api/monitoring`) reads from MySQL
4. **Android app** polls API every 60 seconds
5. **Live Monitoring page** displays updated data

## Testing

### 1. Start the MQTT Subscriber (if not running)

```bash
sudo systemctl start weedx-mqtt
sudo systemctl status weedx-mqtt
```

### 2. Run the Simulator

```bash
# Quick test with 10-second intervals
python3 live_monitoring_simulator.py -i 10
```

### 3. Monitor MQTT Subscriber Logs

```bash
# In another terminal
sudo journalctl -u weedx-mqtt -f
```

### 4. Check Database

```bash
# Verify data is being saved
mysql -u weedx_user -p weedx_db -e "SELECT * FROM robot_status ORDER BY updated_at DESC LIMIT 5;"
```

### 5. Test Backend API

```bash
# Check if API returns the data
./test-backend.sh http://raspberrypi.mullet-bull.ts.net/weedx-backend
```

### 6. Open Android App

- Navigate to **Live Monitoring** page
- Data should auto-update every 60 seconds
- Check "Last updated" timestamp

## Example Output

```
============================================================
🚀 WeedX Live Monitoring Simulator Started
============================================================
📡 MQTT Broker: localhost:1883
👤 User ID: 1
⏱️  Interval: 60 seconds
🕐 Started at: 2025-12-08 14:30:00
============================================================

Press Ctrl+C to stop...

🔄 Cycle #1
============================================================
⏰ Publishing cycle at 14:30:00
👤 User ID: 1
🔋 Battery: 99.7% | 💧 Herbicide: 49.9L
📍 Coverage: 0.03ha | 🌿 Total Weeds: 5
============================================================
📤 Published to weedx/robot/status
📤 Published to weedx/robot/location
📤 Published to weedx/robot/battery
📤 Published to weedx/weed/detection
📤 Published to weedx/sensor/soil
✅ Cycle complete. Waiting 60s for next cycle...
```

## Stopping the Simulator

Press `Ctrl+C` to stop. You'll see a session summary:

```
============================================================
⏹️  Simulator stopped by user
============================================================
📊 Session Summary:
   • Total Cycles: 15
   • Total Weeds Detected: 73
   • Total Coverage: 0.68ha
   • Final Battery: 92.3%
   • Final Herbicide: 47.1L
============================================================
```

## Running Multiple Users

To simulate multiple robots/users simultaneously:

```bash
# Terminal 1 - User 1
python3 live_monitoring_simulator.py -u 1 -i 60

# Terminal 2 - User 2
python3 live_monitoring_simulator.py -u 2 -i 60

# Terminal 3 - User 3
python3 live_monitoring_simulator.py -u 3 -i 60
```

## Troubleshooting

### "Connection refused" Error
- Ensure Mosquitto is running: `sudo systemctl status mosquitto`
- Check MQTT broker is accessible: `mosquitto_pub -h localhost -t test -m "hello"`

### No Data in App
1. Check MQTT subscriber is running: `sudo systemctl status weedx-mqtt`
2. Check subscriber logs: `sudo journalctl -u weedx-mqtt -f`
3. Verify database has data: Query `robot_status` table
4. Test backend API: `curl http://localhost/weedx-backend/api/monitoring`
5. Check app is using correct user_id in API calls

### Data Not Updating in App
- Ensure auto-refresh is working (check "Last updated" timestamp)
- Check network connectivity between app and backend
- Verify JWT token is valid (app authenticated)

## Production Use

**⚠️ This is a TESTING tool only!**

In production:
- Real robot hardware sends actual sensor data
- Data frequency may vary based on robot activity
- No manual intervention needed for data generation

## Files Modified

- `/scripts/live_monitoring_simulator.py` - Main simulator script
- `/app/src/main/java/com/example/weedx/LiveMonitoringActivity.kt` - Added auto-refresh
- `/app/src/main/res/layout/activity_live_monitoring.xml` - Added "Last updated" text

## Related Documentation

- [MQTT Setup Guide](../docs/SETUP_GUIDE.md)
- [Backend API Documentation](../xampp/htdocs/backend/README.md)
- [Architecture Overview](../docs/architecture.md)
