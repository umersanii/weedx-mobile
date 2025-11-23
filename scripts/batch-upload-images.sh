#!/bin/bash
# Batch Image Upload Utility
# Upload multiple images from a directory

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
UPLOAD_SCRIPT="$SCRIPT_DIR/upload-image.sh"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

if [ $# -eq 0 ]; then
    echo -e "${YELLOW}Batch Image Upload Utility${NC}"
    echo "Usage: $0 <directory> [options]"
    echo ""
    echo "Example:"
    echo "  $0 data/images/ --weed-type=\"Broadleaf Weed\" --confidence=90"
    echo ""
    echo "This will upload all images in the directory with the specified options."
    exit 0
fi

IMAGE_DIR="$1"
shift  # Remove first argument, keep the rest for options

if [ ! -d "$IMAGE_DIR" ]; then
    echo -e "${RED}Error: Directory not found: $IMAGE_DIR${NC}"
    exit 1
fi

echo -e "${BLUE}Scanning directory: $IMAGE_DIR${NC}"

# Count images
TOTAL=0
for file in "$IMAGE_DIR"/*.{jpg,jpeg,png,gif,webp,JPG,JPEG,PNG,GIF,WEBP}; do
    [ -e "$file" ] || continue
    TOTAL=$((TOTAL + 1))
done

if [ $TOTAL -eq 0 ]; then
    echo -e "${YELLOW}No image files found in directory${NC}"
    exit 0
fi

echo -e "${BLUE}Found $TOTAL image(s)${NC}"
echo ""

# Upload each image
SUCCESS=0
FAILED=0
for file in "$IMAGE_DIR"/*.{jpg,jpeg,png,gif,webp,JPG,JPEG,PNG,GIF,WEBP}; do
    [ -e "$file" ] || continue
    
    filename=$(basename "$file")
    echo -e "${YELLOW}Processing: $filename${NC}"
    
    if bash "$UPLOAD_SCRIPT" "$file" "$@"; then
        SUCCESS=$((SUCCESS + 1))
        echo ""
    else
        FAILED=$((FAILED + 1))
        echo -e "${RED}Failed to upload: $filename${NC}"
        echo ""
    fi
done

# Summary
echo "=========================================="
echo -e "${GREEN}Upload Complete${NC}"
echo "Total: $TOTAL"
echo -e "${GREEN}Success: $SUCCESS${NC}"
if [ $FAILED -gt 0 ]; then
    echo -e "${RED}Failed: $FAILED${NC}"
fi
echo "=========================================="
