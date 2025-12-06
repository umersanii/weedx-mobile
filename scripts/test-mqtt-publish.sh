#!/bin/bash
# WeedX MQTT Test Publisher
# This script simulates robot data being sent to MQTT

MQTT_HOST="localhost"
MQTT_PORT="1883"
USER_ID=1  # Change this to test different users

echo "========================================"
echo "WeedX MQTT Test Publisher"
echo "========================================"
echo "MQTT Broker: $MQTT_HOST:$MQTT_PORT"
echo "User ID: $USER_ID"
echo ""

# Function to publish a message
publish_message() {
    local topic=$1
    local message=$2
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] Publishing to: $topic"
    mosquitto_pub -h "$MQTT_HOST" -p "$MQTT_PORT" -t "$topic" -m "$message"
    echo "Message: $message"
    echo ""
}

# Test 1: Robot Status Update
echo "Test 1: Robot Status Update"
publish_message "weedx/robot/status" '{
    "user_id": '"$USER_ID"',
    "status": "active",
    "activity": "Scanning wheat field sector B"
}'

sleep 2

# Test 2: Robot Location Update
echo "Test 2: Robot Location Update"
publish_message "weedx/robot/location" '{
    "latitude": 31.5204,
    "longitude": 74.3587,
    "speed": 3.5,
    "heading": 45.0
}'

sleep 2

# Test 3: Battery Level Update
echo "Test 3: Battery Level Update"
publish_message "weedx/robot/battery" '{
    "battery": 75,
    "herbicide": 60
}'

sleep 2

# Test 4: Weed Detection (without image)
echo "Test 4: Weed Detection"
publish_message "weedx/weed/detection" '{
    "user_id": '"$USER_ID"',
    "weed_type": "Broadleaf Weed",
    "crop_type": "Wheat",
    "confidence": 92.5,
    "latitude": 31.5204,
    "longitude": 74.3587
}'

sleep 2

# Test 5: Weed Detection (with base64 image - small sample)
echo "Test 5: Weed Detection with Base64 Image"
# This is a tiny 1x1 red pixel PNG in base64 for testing
SAMPLE_IMAGE="iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8DwHwAFBQIAX8jx0gAAAABJRU5ErkJggg=="

publish_message "weedx/weed/detection" '{
    "user_id": '"$USER_ID"',
    "weed_type": "Grass Weed",
    "crop_type": "Corn",
    "confidence": 88.3,
    "latitude": 31.5210,
    "longitude": 74.3590,
    "image_base64": "'"$SAMPLE_IMAGE"'",
    "image_mime_type": "image/png"
}'

sleep 2

# Test 6: Soil Data
echo "Test 6: Soil Data"
publish_message "weedx/sensor/soil" '{
    "moisture": 45.0,
    "temperature": 22.0,
    "ph": 6.5,
    "nitrogen": 50,
    "phosphorus": 30,
    "potassium": 40,
    "organic_matter": 3.5
}'

sleep 2

# Test 7: Alert - Battery Warning
echo "Test 7: Alert - Battery Warning"
publish_message "weedx/alert" '{
    "user_id": '"$USER_ID"',
    "type": "battery",
    "severity": "warning",
    "message": "Robot battery at 75%. Consider charging soon."
}'

sleep 2

# Test 8: Alert - Maintenance
echo "Test 8: Alert - Maintenance"
publish_message "weedx/alert" '{
    "user_id": '"$USER_ID"',
    "type": "maintenance",
    "severity": "info",
    "message": "Regular maintenance due in 2 days."
}'

sleep 2

# Test 9: Alert - Detection Summary
echo "Test 9: Alert - Detection Summary"
publish_message "weedx/alert" '{
    "user_id": '"$USER_ID"',
    "type": "detection",
    "severity": "info",
    "message": "15 new weeds detected in the last hour."
}'

echo ""
echo "========================================"
echo "All tests completed!"
echo "========================================"
echo ""
echo "Check the MQTT subscriber logs to verify message processing."
echo "You can also check the database to see if data was saved correctly."
