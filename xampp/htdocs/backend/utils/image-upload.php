#!/usr/bin/env php
<?php
/**
 * Terminal Utility: Upload Image to Database
 * Usage: php utils/image-upload.php <image-path> [options]
 * 
 * Options:
 *   --user-id=<id>          User ID (default: 1)
 *   --weed-type=<type>      Weed type (default: Unknown)
 *   --crop-type=<type>      Crop type (optional)
 *   --confidence=<value>    Confidence 0-100 (default: 0)
 *   --latitude=<value>      Latitude (default: 0)
 *   --longitude=<value>     Longitude (default: 0)
 *   --category=<category>   Category for user_images table (avatar, gallery, weed)
 *   --title=<title>         Title for user_images (optional)
 *   --description=<desc>    Description for user_images (optional)
 *   --table=<table>         Target table: weed_detections (default) or user_images
 */

require_once __DIR__ . '/../config/database.php';

// Color output helpers
function colorOutput($text, $color = 'green') {
    $colors = [
        'red' => "\033[31m",
        'green' => "\033[32m",
        'yellow' => "\033[33m",
        'blue' => "\033[34m",
        'reset' => "\033[0m"
    ];
    return $colors[$color] . $text . $colors['reset'];
}

function error($message) {
    echo colorOutput("❌ ERROR: $message\n", 'red');
    exit(1);
}

function success($message) {
    echo colorOutput("✅ SUCCESS: $message\n", 'green');
}

function info($message) {
    echo colorOutput("ℹ️  INFO: $message\n", 'blue');
}

// Parse command line arguments
if ($argc < 2 || in_array('--help', $argv) || in_array('-h', $argv)) {
    echo colorOutput("Image Upload Utility\n", 'yellow');
    echo "Usage: php utils/image-upload.php <image-path> [options]\n\n";
    echo "Examples:\n";
    echo "  php utils/image-upload.php data/images/weed.jpg --weed-type=\"Broadleaf Weed\" --confidence=95\n";
    echo "  php utils/image-upload.php avatar.png --table=user_images --category=avatar --user-id=1\n\n";
    echo "Options:\n";
    echo "  --user-id=<id>          User ID (default: 1)\n";
    echo "  --weed-type=<type>      Weed type (default: Unknown)\n";
    echo "  --crop-type=<type>      Crop type (optional)\n";
    echo "  --confidence=<value>    Confidence 0-100 (default: 0)\n";
    echo "  --latitude=<value>      Latitude (default: 0)\n";
    echo "  --longitude=<value>     Longitude (default: 0)\n";
    echo "  --category=<category>   Category for user_images (avatar, gallery, weed)\n";
    echo "  --title=<title>         Title for user_images\n";
    echo "  --description=<desc>    Description for user_images\n";
    echo "  --table=<table>         Target table: weed_detections (default) or user_images\n";
    exit(0);
}

$imagePath = $argv[1];

// Default values
$options = [
    'user_id' => 1,
    'weed_type' => 'Unknown',
    'crop_type' => null,
    'confidence' => 0,
    'latitude' => 0,
    'longitude' => 0,
    'category' => 'gallery',
    'title' => null,
    'description' => null,
    'table' => 'weed_detections'
];

// Parse options
for ($i = 2; $i < $argc; $i++) {
    if (preg_match('/^--([^=]+)=(.+)$/', $argv[$i], $matches)) {
        $key = str_replace('-', '_', $matches[1]);
        $options[$key] = $matches[2];
    }
}

// Validate image file
if (!file_exists($imagePath)) {
    error("Image file not found: $imagePath");
}

if (!is_readable($imagePath)) {
    error("Image file not readable: $imagePath");
}

// Get file info
$finfo = finfo_open(FILEINFO_MIME_TYPE);
$mimeType = finfo_file($finfo, $imagePath);
finfo_close($finfo);

$allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'];
if (!in_array($mimeType, $allowedTypes)) {
    error("Invalid image type: $mimeType. Allowed: " . implode(', ', $allowedTypes));
}

$fileSize = filesize($imagePath);
if ($fileSize > 10 * 1024 * 1024) {
    error("File too large: " . number_format($fileSize / 1024 / 1024, 2) . "MB. Maximum 10MB allowed.");
}

info("Reading image: $imagePath");
info("MIME Type: $mimeType");
info("File Size: " . number_format($fileSize / 1024, 2) . "KB");

// Read and encode image
$imageData = file_get_contents($imagePath);
if ($imageData === false) {
    error("Failed to read image file");
}

$base64Image = base64_encode($imageData);
$base64Size = strlen($base64Image);

info("Base64 Size: " . number_format($base64Size / 1024, 2) . "KB");
info("Connecting to database...");

// Connect to database
try {
    $database = new Database();
    $db = $database->getConnection();
    
    if ($options['table'] === 'user_images') {
        // Insert into user_images table
        $query = "
            INSERT INTO user_images 
            (user_id, title, description, image_base64, image_mime_type, category, created_at) 
            VALUES (:user_id, :title, :description, :image_base64, :image_mime_type, :category, NOW())
        ";
        
        $stmt = $db->prepare($query);
        $stmt->bindParam(':user_id', $options['user_id']);
        $stmt->bindParam(':title', $options['title']);
        $stmt->bindParam(':description', $options['description']);
        $stmt->bindParam(':image_base64', $base64Image);
        $stmt->bindParam(':image_mime_type', $mimeType);
        $stmt->bindParam(':category', $options['category']);
        
        info("Uploading to user_images table...");
        
    } else {
        // Insert into weed_detections table
        $query = "
            INSERT INTO weed_detections 
            (user_id, weed_type, crop_type, confidence, latitude, longitude, image_base64, image_mime_type, detected_at) 
            VALUES (:user_id, :weed_type, :crop_type, :confidence, :latitude, :longitude, :image_base64, :image_mime_type, NOW())
        ";
        
        $stmt = $db->prepare($query);
        $stmt->bindParam(':user_id', $options['user_id']);
        $stmt->bindParam(':weed_type', $options['weed_type']);
        $stmt->bindParam(':crop_type', $options['crop_type']);
        $stmt->bindParam(':confidence', $options['confidence']);
        $stmt->bindParam(':latitude', $options['latitude']);
        $stmt->bindParam(':longitude', $options['longitude']);
        $stmt->bindParam(':image_base64', $base64Image);
        $stmt->bindParam(':image_mime_type', $mimeType);
        
        info("Uploading to weed_detections table...");
    }
    
    if ($stmt->execute()) {
        $insertId = $db->lastInsertId();
        success("Image uploaded successfully!");
        echo "\n";
        echo colorOutput("Details:\n", 'yellow');
        echo "  ID: $insertId\n";
        echo "  Table: {$options['table']}\n";
        echo "  User ID: {$options['user_id']}\n";
        echo "  MIME Type: $mimeType\n";
        echo "  Original Size: " . number_format($fileSize / 1024, 2) . "KB\n";
        echo "  Base64 Size: " . number_format($base64Size / 1024, 2) . "KB\n";
        
        if ($options['table'] === 'weed_detections') {
            echo "  Weed Type: {$options['weed_type']}\n";
            echo "  Confidence: {$options['confidence']}%\n";
        } else {
            echo "  Category: {$options['category']}\n";
            if ($options['title']) echo "  Title: {$options['title']}\n";
        }
    } else {
        error("Failed to insert image into database");
    }
    
} catch (Exception $e) {
    error("Database error: " . $e->getMessage());
}
