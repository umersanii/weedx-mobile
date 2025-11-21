#!/bin/bash

# Test WeedX Backend via Tailscale
# This script tests the backend connectivity and basic endpoints

set -e

echo "=========================================="
echo "WeedX Backend - Tailscale Connection Test"
echo "=========================================="
echo ""

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Configuration
PI_HOST="raspberrypi.mullet-bull.ts.net"
BACKEND_URL="http://${PI_HOST}/weedx-backend"
DEMO_EMAIL="admin@weedx.com"
DEMO_PASSWORD="admin123"

echo -e "${BLUE}Backend URL: $BACKEND_URL${NC}"
echo ""

# Test function
test_endpoint() {
    local name="$1"
    local url="$2"
    local method="${3:-GET}"
    local data="${4:-}"
    
    echo -e "${YELLOW}Testing: $name${NC}"
    
    if [ -n "$data" ]; then
        RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" -X "$method" "$url" \
            -H "Content-Type: application/json" \
            -d "$data" 2>/dev/null || echo "000")
    else
        RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" -X "$method" "$url" 2>/dev/null || echo "000")
    fi
    
    if [ "$RESPONSE" = "200" ] || [ "$RESPONSE" = "401" ] || [ "$RESPONSE" = "302" ]; then
        echo -e "${GREEN}  ✓ Response: HTTP $RESPONSE${NC}"
        return 0
    elif [ "$RESPONSE" = "000" ]; then
        echo -e "${RED}  ✗ Connection failed${NC}"
        return 1
    else
        echo -e "${YELLOW}  ⚠ Response: HTTP $RESPONSE${NC}"
        return 1
    fi
}

# Step 1: Test Tailscale connectivity
echo -e "${YELLOW}Step 1: Testing Tailscale connectivity...${NC}"
if ping -c 1 "$PI_HOST" &> /dev/null; then
    echo -e "${GREEN}✓ Pi is reachable via Tailscale${NC}"
else
    echo -e "${RED}✗ Cannot reach Pi at $PI_HOST${NC}"
    echo ""
    echo "Troubleshooting:"
    echo "  1. Check Tailscale status: tailscale status"
    echo "  2. Verify Pi is online in Tailscale"
    echo "  3. Try: ping $PI_HOST"
    exit 1
fi
echo ""

# Step 2: Test backend base URL
echo -e "${YELLOW}Step 2: Testing backend base URL...${NC}"
test_endpoint "Base URL" "$BACKEND_URL/"
echo ""

# Step 3: Test authentication endpoint
echo -e "${YELLOW}Step 3: Testing authentication...${NC}"
LOGIN_DATA="{\"email\":\"$DEMO_EMAIL\",\"password\":\"$DEMO_PASSWORD\",\"firebaseToken\":\"test_token\"}"
LOGIN_RESPONSE=$(curl -s -X POST "$BACKEND_URL/auth/login" \
    -H "Content-Type: application/json" \
    -d "$LOGIN_DATA" 2>/dev/null || echo "{\"error\":\"Connection failed\"}")

echo "$LOGIN_RESPONSE" | jq '.' 2>/dev/null || echo "$LOGIN_RESPONSE"

if echo "$LOGIN_RESPONSE" | grep -q '"token"'; then
    echo -e "${GREEN}✓ Login successful${NC}"
    TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.token' 2>/dev/null)
    echo -e "  Token: ${TOKEN:0:20}...${NC}"
else
    echo -e "${YELLOW}⚠ Login failed or returned unexpected response${NC}"
    TOKEN=""
fi
echo ""

# Step 4: Test protected endpoints
if [ -n "$TOKEN" ]; then
    echo -e "${YELLOW}Step 4: Testing protected endpoints...${NC}"
    
    test_endpoint "Robot Status" "$BACKEND_URL/robot/status" "GET"
    test_endpoint "Dashboard Stats" "$BACKEND_URL/dashboard/stats" "GET"
    test_endpoint "Recent Activity" "$BACKEND_URL/dashboard/recent-activity" "GET"
    
    echo ""
fi

# Step 5: Test from phone (instructions)
echo -e "${YELLOW}Step 5: Test from your Android phone...${NC}"
echo ""
echo -e "${BLUE}Install curl on Termux (if not already):${NC}"
echo "  pkg install curl"
echo ""
echo -e "${BLUE}Test connection from phone:${NC}"
echo "  curl -v http://${PI_HOST}/weedx-backend/"
echo ""
echo -e "${BLUE}Test login from phone:${NC}"
echo "  curl -X POST http://${PI_HOST}/weedx-backend/auth/login \\"
echo "    -H \"Content-Type: application/json\" \\"
echo "    -d '{\"email\":\"${DEMO_EMAIL}\",\"password\":\"${DEMO_PASSWORD}\",\"firebaseToken\":\"test\"}'"
echo ""

# Summary
echo "=========================================="
echo -e "${GREEN}Test Summary${NC}"
echo "=========================================="
echo ""
echo -e "${BLUE}Backend Status:${NC}"
if [ -n "$TOKEN" ]; then
    echo -e "  ${GREEN}✓ Backend is accessible and working${NC}"
else
    echo -e "  ${YELLOW}⚠ Backend is accessible but login needs verification${NC}"
fi
echo ""
echo -e "${BLUE}Android App Configuration:${NC}"
echo "  File: app/src/main/java/com/example/weedx/utils/Constants.kt"
echo "  const val BASE_URL = \"http://${PI_HOST}/weedx-backend/\""
echo ""
echo -e "${BLUE}Next Steps:${NC}"
echo "  1. Build and install the Android app on your phone"
echo "  2. Make sure phone is connected to Tailscale"
echo "  3. Open WeedX app and try logging in"
echo "  4. Monitor Pi logs: ssh pi@${PI_HOST} 'sudo tail -f /var/log/apache2/error.log'"
echo ""
echo -e "${GREEN}Testing complete! 🎉${NC}"
