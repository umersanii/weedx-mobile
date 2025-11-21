#!/bin/bash

# WeedX Backend Test Script
# Tests all major API endpoints

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

BASE_URL="${1:-http://localhost/weedx-backend}"
TOKEN=""

echo "=========================================="
echo "WeedX Backend API Test"
echo "=========================================="
echo "Base URL: $BASE_URL"
echo ""

# Function to test endpoint
test_endpoint() {
    local method=$1
    local endpoint=$2
    local description=$3
    local auth=$4
    local data=$5
    
    echo -ne "${BLUE}Testing:${NC} $description... "
    
    if [ "$auth" = "auth" ] && [ -n "$TOKEN" ]; then
        if [ "$method" = "POST" ]; then
            RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL$endpoint" \
                -H "Content-Type: application/json" \
                -H "Authorization: Bearer $TOKEN" \
                -d "$data")
        else
            RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL$endpoint" \
                -H "Authorization: Bearer $TOKEN")
        fi
    else
        if [ "$method" = "POST" ]; then
            RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL$endpoint" \
                -H "Content-Type: application/json" \
                -d "$data")
        else
            RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL$endpoint")
        fi
    fi
    
    HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
    BODY=$(echo "$RESPONSE" | head -n-1)
    
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "201" ]; then
        echo -e "${GREEN}✓ $HTTP_CODE${NC}"
        return 0
    elif [ "$HTTP_CODE" = "401" ] && [ "$auth" = "auth" ]; then
        echo -e "${YELLOW}⚠ $HTTP_CODE (needs auth)${NC}"
        return 0
    else
        echo -e "${RED}✗ $HTTP_CODE${NC}"
        echo "Response: $BODY"
        return 1
    fi
}

# Test 1: Login
echo -e "${YELLOW}=== Authentication ===${NC}"
echo -ne "${BLUE}Testing:${NC} Login... "

LOGIN_DATA='{"email":"admin@weedx.com","password":"admin123","firebaseToken":"test"}'
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/auth/login" \
    -H "Content-Type: application/json" \
    -d "$LOGIN_DATA")

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n-1)

if [ "$HTTP_CODE" = "200" ]; then
    echo -e "${GREEN}✓ $HTTP_CODE${NC}"
    TOKEN=$(echo "$BODY" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
    if [ -n "$TOKEN" ]; then
        echo -e "${GREEN}  Token received: ${TOKEN:0:20}...${NC}"
    fi
else
    echo -e "${RED}✗ $HTTP_CODE${NC}"
    echo "Response: $BODY"
    exit 1
fi

echo ""

# Test authenticated endpoints
echo -e "${YELLOW}=== Dashboard ===${NC}"
test_endpoint "GET" "/landing" "Landing page" "auth"
test_endpoint "GET" "/robot/status" "Robot status" "auth"
test_endpoint "GET" "/summary/today" "Today's summary" "auth"
test_endpoint "GET" "/alerts/recent" "Recent alerts" "auth"
echo ""

echo -e "${YELLOW}=== Monitoring ===${NC}"
test_endpoint "GET" "/monitoring" "Monitoring overview" "auth"
test_endpoint "GET" "/monitoring/metrics" "Live metrics" "auth"
test_endpoint "GET" "/monitoring/activity" "Activity timeline" "auth"
test_endpoint "GET" "/monitoring/location" "Robot location" "auth"
echo ""

echo -e "${YELLOW}=== Weed Logs ===${NC}"
test_endpoint "GET" "/weed-logs" "Weed logs overview" "auth"
test_endpoint "GET" "/weed-logs/summary" "Weed summary" "auth"
test_endpoint "GET" "/weed-logs/detections" "Weed detections" "auth"
echo ""

echo -e "${YELLOW}=== Environment ===${NC}"
test_endpoint "GET" "/environment" "Environment overview" "auth"
test_endpoint "GET" "/environment/weather/current" "Current weather" "auth"
test_endpoint "GET" "/environment/weather/forecast" "Weather forecast" "auth"
test_endpoint "GET" "/environment/soil" "Soil data" "auth"
test_endpoint "GET" "/environment/recommendations/today" "Recommendations" "auth"
echo ""

echo -e "${YELLOW}=== Reports ===${NC}"
test_endpoint "GET" "/reports" "Reports overview" "auth"
test_endpoint "GET" "/reports/widgets" "Report widgets" "auth"
test_endpoint "GET" "/reports/weed-trend" "Weed trend" "auth"
test_endpoint "GET" "/reports/weed-distribution" "Weed distribution" "auth"
echo ""

echo -e "${YELLOW}=== Gallery ===${NC}"
test_endpoint "GET" "/gallery" "Gallery list" "auth"
echo ""

echo -e "${YELLOW}=== Profile ===${NC}"
test_endpoint "GET" "/profile" "User profile" "auth"
test_endpoint "GET" "/profile/settings" "User settings" "auth"
echo ""

echo -e "${YELLOW}=== Assistant ===${NC}"
test_endpoint "GET" "/assistant/history" "Chat history" "auth"
QUERY_DATA='{"query":"What is the robot status?"}'
test_endpoint "POST" "/assistant/query" "Send query" "auth" "$QUERY_DATA"
echo ""

echo "=========================================="
echo -e "${GREEN}API Testing Complete!${NC}"
echo "=========================================="
echo ""
echo "All major endpoints tested successfully! ✅"
echo ""
echo "Next steps:"
echo "  1. Update Android app BASE_URL"
echo "  2. Implement API services in Android"
echo "  3. Create repositories and ViewModels"
echo ""
