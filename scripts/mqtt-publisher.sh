#!/bin/bash
# WeedX MQTT Publisher Control Script
# Publish data to MQTT topics in a controlled environment

set -e

MQTT_HOST="${MQTT_HOST:-localhost}"
MQTT_PORT="${MQTT_PORT:-1883}"
USER_ID="${USER_ID:-1}"

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

show_help() {
    cat << EOF
WeedX MQTT Publisher - Send data to MQTT topics

Usage: $(basename "$0") [COMMAND] [OPTIONS]

Commands:
    status              Publish robot status update
    location            Publish GPS location
    battery             Publish battery/herbicide levels
    weed                Publish weed detection
    weed-img            Publish weed detection with image
    soil                Publish soil sensor data
    alert               Publish system alert
    batch               Publish multiple messages in sequence
    help                Show this help

Options:
    -h HOST             MQTT broker host (default: localhost)
    -p PORT             MQTT broker port (default: 1883)
    -u USER_ID          User ID for the data (default: 1)

Environment Variables:
    MQTT_HOST           Override default MQTT host
    MQTT_PORT           Override default MQTT port
    USER_ID             Override default user ID

Examples:
    # Publish robot status
    $(basename "$0") status

    # Publish with custom user ID
    $(basename "$0") -u 2 weed

    # Publish to remote broker
    $(basename "$0") -h raspberrypi.mullet-bull.ts.net weed

    # Send batch of test messages
    $(basename "$0") batch

EOF
}

publish() {
    local topic=$1
    local message=$2
    echo -e "${BLUE}[$(date '+%H:%M:%S')]${NC} Publishing to: ${GREEN}${topic}${NC}"
    mosquitto_pub -h "$MQTT_HOST" -p "$MQTT_PORT" -t "$topic" -m "$message"
    echo -e "${GREEN}✓${NC} Published successfully\n"
}

publish_status() {
    local status="${1:-active}"
    local activity="${2:-Scanning field}"
    
    publish "weedx/robot/status" "{
        \"user_id\": ${USER_ID},
        \"status\": \"${status}\",
        \"activity\": \"${activity}\"
    }"
}

publish_location() {
    local lat="${1:-31.5204}"
    local lon="${2:-74.3587}"
    local speed="${3:-3.5}"
    local heading="${4:-45.0}"
    
    publish "weedx/robot/location" "{
        \"latitude\": ${lat},
        \"longitude\": ${lon},
        \"speed\": ${speed},
        \"heading\": ${heading}
    }"
}

publish_battery() {
    local battery="${1:-85}"
    local herbicide="${2:-70}"
    
    publish "weedx/robot/battery" "{
        \"battery\": ${battery},
        \"herbicide\": ${herbicide}
    }"
}

publish_weed() {
    local weed_type="${1:-Broadleaf Weed}"
    local crop_type="${2:-Wheat}"
    local confidence="${3:-92.5}"
    local lat="${4:-31.5204}"
    local lon="${5:-74.3587}"
    
    publish "weedx/weed/detection" "{
        \"user_id\": ${USER_ID},
        \"weed_type\": \"${weed_type}\",
        \"crop_type\": \"${crop_type}\",
        \"confidence\": ${confidence},
        \"latitude\": ${lat},
        \"longitude\": ${lon}
    }"
}

publish_weed_with_image() {
    local weed_type="${1:-Grass Weed}"
    local crop_type="${2:-Corn}"
    local confidence="${3:-88.3}"
    local lat="${4:-31.5210}"
    local lon="${5:-74.3590}"
    
    # 1x1 red pixel PNG in base64 for testing
    local sample_image="iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8DwHwAFBQIAX8jx0gAAAABJRU5ErkJggg=="
    
    publish "weedx/weed/detection" "{
        \"user_id\": ${USER_ID},
        \"weed_type\": \"${weed_type}\",
        \"crop_type\": \"${crop_type}\",
        \"confidence\": ${confidence},
        \"latitude\": ${lat},
        \"longitude\": ${lon},
        \"image_base64\": \"${sample_image}\",
        \"image_mime_type\": \"image/png\"
    }"
}

publish_soil() {
    local moisture="${1:-45.0}"
    local temp="${2:-22.0}"
    local ph="${3:-6.5}"
    local n="${4:-50}"
    local p="${5:-30}"
    local k="${6:-40}"
    local om="${7:-3.5}"
    
    publish "weedx/sensor/soil" "{
        \"moisture\": ${moisture},
        \"temperature\": ${temp},
        \"ph\": ${ph},
        \"nitrogen\": ${n},
        \"phosphorus\": ${p},
        \"potassium\": ${k},
        \"organic_matter\": ${om}
    }"
}

publish_alert() {
    local type="${1:-battery}"
    local severity="${2:-warning}"
    local message="${3:-Battery at 75%. Consider charging soon.}"
    
    publish "weedx/alert" "{
        \"user_id\": ${USER_ID},
        \"type\": \"${type}\",
        \"severity\": \"${severity}\",
        \"message\": \"${message}\"
    }"
}

batch_publish() {
    echo -e "${YELLOW}Starting batch publish...${NC}\n"
    
    publish_status "active" "Scanning wheat field"
    sleep 1
    
    publish_location 31.5204 74.3587 3.5 45.0
    sleep 1
    
    publish_battery 85 70
    sleep 1
    
    publish_weed "Broadleaf Weed" "Wheat" 92.5 31.5204 74.3587
    sleep 1
    
    publish_weed_with_image "Grass Weed" "Corn" 88.3 31.5210 74.3590
    sleep 1
    
    publish_soil 45.0 22.0 6.5 50 30 40 3.5
    sleep 1
    
    publish_alert "detection" "info" "15 new weeds detected in the last hour"
    
    echo -e "${GREEN}✓ Batch complete!${NC}"
}

# Parse options
while getopts "h:p:u:" opt; do
    case $opt in
        h) MQTT_HOST="$OPTARG" ;;
        p) MQTT_PORT="$OPTARG" ;;
        u) USER_ID="$OPTARG" ;;
        *) show_help; exit 1 ;;
    esac
done
shift $((OPTIND-1))

# Check if mosquitto_pub is available
if ! command -v mosquitto_pub &> /dev/null; then
    echo -e "${YELLOW}Error:${NC} mosquitto_pub not found. Install with: sudo apt-get install mosquitto-clients"
    exit 1
fi

# Handle command
COMMAND="${1:-help}"

echo -e "${GREEN}========================================"
echo "WeedX MQTT Publisher"
echo "========================================${NC}"
echo "Host:    ${MQTT_HOST}:${MQTT_PORT}"
echo "User ID: ${USER_ID}"
echo ""

case "$COMMAND" in
    status)
        publish_status "${2:-active}" "${3:-Scanning field}"
        ;;
    location)
        publish_location "${2}" "${3}" "${4}" "${5}"
        ;;
    battery)
        publish_battery "${2}" "${3}"
        ;;
    weed)
        publish_weed "${2}" "${3}" "${4}" "${5}" "${6}"
        ;;
    weed-img)
        publish_weed_with_image "${2}" "${3}" "${4}" "${5}" "${6}"
        ;;
    soil)
        publish_soil "${2}" "${3}" "${4}" "${5}" "${6}" "${7}" "${8}"
        ;;
    alert)
        publish_alert "${2}" "${3}" "${4}"
        ;;
    batch)
        batch_publish
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        echo -e "${YELLOW}Unknown command:${NC} $COMMAND"
        echo ""
        show_help
        exit 1
        ;;
esac
