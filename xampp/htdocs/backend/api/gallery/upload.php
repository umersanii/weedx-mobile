<?php
/**
 * Gallery Upload Endpoint
 * POST /api/gallery
 */

require_once __DIR__ . '/../../config/database.php';
require_once __DIR__ . '/../../utils/response.php';
require_once __DIR__ . '/../../utils/auth.php';
require_once __DIR__ . '/../../utils/image_helper.php';

$tokenData = Auth::validateToken();
$userId = $tokenData['user_id'];

// Check if file was uploaded
if (!isset($_FILES['image'])) {
    Response::error('No image file provided', 400);
}

// Use ImageHelper to save the image to gallery category
$result = ImageHelper::saveUploadedImage(
    $_FILES['image'],
    ImageHelper::CATEGORY_GALLERY,
    null,
    5 // Max 5MB
);

<<<<<<< HEAD
if (!$result['success']) {
    Response::error($result['error'], 400);
}
=======
// Validate file type
$allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'];
if (!in_array($file['type'], $allowedTypes)) {
    Response::error('Invalid file type. Only JPEG, PNG, GIF, and WebP allowed', 400);
}

// Validate file size (max 10MB for base64 storage)
if ($file['size'] > 10 * 1024 * 1024) {
    Response::error('File too large. Maximum 10MB', 400);
}

// Read file and encode to base64
$imageData = file_get_contents($file['tmp_name']);
if ($imageData === false) {
    Response::error('Failed to read uploaded file', 500);
}

$base64Image = base64_encode($imageData);
$mimeType = $file['type'];
>>>>>>> e54912b (images endpoint)

// Save to database
$database = new Database();
$db = $database->getConnection();

<<<<<<< HEAD
$imagePath = $result['relative_path'];
=======
>>>>>>> e54912b (images endpoint)
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
    Response::success([
        'id' => (int)$db->lastInsertId(),
<<<<<<< HEAD
        'url' => $result['full_url'],
=======
        'mime_type' => $mimeType,
        'size_bytes' => strlen($base64Image),
>>>>>>> e54912b (images endpoint)
        'uploaded_at' => date('Y-m-d H:i:s')
    ], 'Image uploaded successfully', 201);
} else {
    Response::error('Failed to save image data', 500);
}
