#!/bin/bash
# Image Upload Utility - Bash Wrapper
# Makes it easier to upload images from terminal

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PHP_SCRIPT="$SCRIPT_DIR/../xampp/htdocs/backend/utils/image-upload.php"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if PHP script exists
if [ ! -f "$PHP_SCRIPT" ]; then
    echo -e "${RED}Error: PHP script not found at $PHP_SCRIPT${NC}"
    exit 1
fi

# Check if PHP is available
if ! command -v php &> /dev/null; then
    echo -e "${RED}Error: PHP is not installed or not in PATH${NC}"
    exit 1
fi

# If no arguments, show help
if [ $# -eq 0 ]; then
    php "$PHP_SCRIPT"
    exit 0
fi

# Run the PHP script with all arguments
php "$PHP_SCRIPT" "$@"
