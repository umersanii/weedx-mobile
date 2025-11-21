<?php
/**
 * Gallery Upload Endpoint
 * POST /api/gallery
 */

require_once __DIR__ . '/../../config/database.php';
require_once __DIR__ . '/../../utils/response.php';
require_once __DIR__ . '/../../utils/auth.php';

$tokenData = Auth::validateToken();

// Check if file was uploaded
if (!isset($_FILES['image'])) {
    Response::error('No image file provided', 400);
}

$file = $_FILES['image'];

// Validate file type
$allowedTypes = ['image/jpeg', 'image/jpg', 'image/png'];
if (!in_array($file['type'], $allowedTypes)) {
    Response::error('Invalid file type. Only JPEG and PNG allowed', 400);
}

// Validate file size (max 5MB)
if ($file['size'] > 5 * 1024 * 1024) {
    Response::error('File too large. Maximum 5MB', 400);
}

// Create uploads directory if not exists
$uploadDir = __DIR__ . '/../../uploads/gallery/';
if (!file_exists($uploadDir)) {
    mkdir($uploadDir, 0755, true);
}

// Generate unique filename
$extension = pathinfo($file['name'], PATHINFO_EXTENSION);
$filename = uniqid('weed_', true) . '.' . $extension;
$filepath = $uploadDir . $filename;

// Move uploaded file
if (!move_uploaded_file($file['tmp_name'], $filepath)) {
    Response::error('Failed to upload file', 500);
}

// Save to database
$database = new Database();
$db = $database->getConnection();

$imagePath = '/uploads/gallery/' . $filename;
$weedType = $_POST['weed_type'] ?? 'Unknown';
$confidence = $_POST['confidence'] ?? 0;
$latitude = $_POST['latitude'] ?? 0;
$longitude = $_POST['longitude'] ?? 0;

$query = "
    INSERT INTO weed_detections 
    (weed_type, confidence, latitude, longitude, image_path, detected_at) 
    VALUES (:weed_type, :confidence, :latitude, :longitude, :image_path, NOW())
";

$stmt = $db->prepare($query);
$stmt->bindParam(':weed_type', $weedType);
$stmt->bindParam(':confidence', $confidence);
$stmt->bindParam(':latitude', $latitude);
$stmt->bindParam(':longitude', $longitude);
$stmt->bindParam(':image_path', $imagePath);

if ($stmt->execute()) {
    Response::success([
        'id' => (int)$db->lastInsertId(),
        'url' => $imagePath,
        'uploaded_at' => date('Y-m-d H:i:s')
    ], 'Image uploaded successfully', 201);
} else {
    Response::error('Failed to save image data', 500);
}
