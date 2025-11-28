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

if (!$result['success']) {
    Response::error($result['error'], 400);
}

// Save to database
$database = new Database();
$db = $database->getConnection();

$imagePath = $result['relative_path'];
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
        'url' => $result['full_url'],
        'uploaded_at' => date('Y-m-d H:i:s')
    ], 'Image uploaded successfully', 201);
} else {
    Response::error('Failed to save image data', 500);
}
