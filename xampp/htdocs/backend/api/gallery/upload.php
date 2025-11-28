<?php
/**
 * Gallery Upload Endpoint
 * POST /api/gallery
 */

require_once __DIR__ . '/../../config/database.php';
require_once __DIR__ . '/../../utils/response.php';
require_once __DIR__ . '/../../utils/auth.php';
require_once __DIR__ . '/../../utils/image_helper.php';
require_once __DIR__ . '/../../utils/logger.php';

Logger::logRequest('/api/gallery', 'POST');

$tokenData = Auth::validateToken();
Logger::logAuth('/api/gallery', $tokenData['user_id'] ?? null, true);
$userId = $tokenData['user_id'];

// Check if file was uploaded
if (!isset($_FILES['image'])) {
    Logger::logError('/api/gallery', 'No image file provided', 400);
    Response::error('No image file provided', 400);
}

$file = $_FILES['image'];

// Validate file type
$allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'];
if (!in_array($file['type'], $allowedTypes)) {
    Logger::logError('/api/gallery', 'Invalid file type: ' . $file['type'], 400);
    Response::error('Invalid file type. Only JPEG, PNG, GIF, and WebP allowed', 400);
}

// Validate file size (max 10MB for base64 storage)
if ($file['size'] > 10 * 1024 * 1024) {
    Logger::logError('/api/gallery', 'File too large: ' . $file['size'] . ' bytes', 400);
    Response::error('File too large. Maximum 10MB', 400);
}

// Read file and encode to base64
$imageData = file_get_contents($file['tmp_name']);
if ($imageData === false) {
    Logger::logError('/api/gallery', 'Failed to read uploaded file', 500);
    Response::error('Failed to read uploaded file', 500);
}

$base64Image = base64_encode($imageData);
$mimeType = $file['type'];

// Save to database
$database = new Database();
$db = $database->getConnection();

$weedType = $_POST['weed_type'] ?? 'Unknown';
$cropType = $_POST['crop_type'] ?? null;
$confidence = $_POST['confidence'] ?? 0;
$latitude = $_POST['latitude'] ?? 0;
$longitude = $_POST['longitude'] ?? 0;

$query = "
    INSERT INTO weed_detections 
    (user_id, weed_type, crop_type, confidence, latitude, longitude, image_base64, image_mime_type, detected_at) 
    VALUES (:user_id, :weed_type, :crop_type, :confidence, :latitude, :longitude, :image_base64, :image_mime_type, NOW())
";

$stmt = $db->prepare($query);
$stmt->bindParam(':user_id', $userId);
$stmt->bindParam(':weed_type', $weedType);
$stmt->bindParam(':crop_type', $cropType);
$stmt->bindParam(':confidence', $confidence);
$stmt->bindParam(':latitude', $latitude);
$stmt->bindParam(':longitude', $longitude);
$stmt->bindParam(':image_base64', $base64Image);
$stmt->bindParam(':image_mime_type', $mimeType);

if ($stmt->execute()) {
    $insertId = (int)$db->lastInsertId();
    Logger::logSuccess('/api/gallery', 'Image uploaded, ID: ' . $insertId . ', Type: ' . $weedType);
    Response::success([
        'id' => $insertId,
        'mime_type' => $mimeType,
        'size_bytes' => strlen($base64Image),
        'uploaded_at' => date('Y-m-d H:i:s')
    ], 'Image uploaded successfully', 201);
} else {
    Logger::logError('/api/gallery', 'Failed to save image data', 500);
    Response::error('Failed to save image data', 500);
}
