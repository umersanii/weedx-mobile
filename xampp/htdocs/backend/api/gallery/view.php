<?php
/**
 * Gallery View Single Image Endpoint
 * GET /api/gallery/:id
 */

require_once __DIR__ . '/../../config/database.php';
require_once __DIR__ . '/../../utils/response.php';
require_once __DIR__ . '/../../utils/auth.php';
require_once __DIR__ . '/../../utils/image_helper.php';
require_once __DIR__ . '/../../utils/logger.php';

$imageId = $_GET['id'] ?? null;
Logger::logRequest('/api/gallery/' . $imageId, 'GET');

$tokenData = Auth::validateToken();
Logger::logAuth('/api/gallery/' . $imageId, $tokenData['userId'] ?? null, true);
$database = new Database();
$db = $database->getConnection();

if (!$imageId) {
    Logger::logError('/api/gallery/:id', 'Image ID required', 400);
    Response::error('Image ID required', 400);
}

try {
    $query = "SELECT * FROM weed_detections WHERE id = :id LIMIT 1";
    $stmt = $db->prepare($query);
    $stmt->bindParam(':id', $imageId, PDO::PARAM_INT);
    $stmt->execute();
    
    $image = $stmt->fetch();
    
    if (!$image) {
        Logger::logError('/api/gallery/' . $imageId, 'Image not found', 404);
        Response::error('Image not found', 404);
    }
    
    // Determine image URL: prefer base64, fallback to file path
    $imageUrl = null;
    if (!empty($image['image_base64'])) {
        $mime = $image['image_mime_type'] ?? 'image/jpeg';
        $imageUrl = 'data:' . $mime . ';base64,' . $image['image_base64'];
    } elseif (!empty($image['image_path'])) {
        $imageUrl = ImageHelper::getFullUrl($image['image_path']);
    }
    
    $response = [
        'id' => (int)$image['id'],
        'url' => $imageUrl,
        'weed_type' => $image['weed_type'],
        'confidence' => (float)$image['confidence'],
        'location' => [
            'latitude' => (float)$image['latitude'],
            'longitude' => (float)$image['longitude']
        ],
        'crop_type' => $image['crop_type'] ?? null,
        'captured_at' => $image['detected_at']
    ];
    
    Logger::logSuccess('/api/gallery/' . $imageId, 'Image details fetched');
    Response::success($response);
} catch (Exception $e) {
    Logger::logError('/api/gallery/' . $imageId, $e->getMessage(), 500);
    Response::error('Failed to fetch image: ' . $e->getMessage(), 500);
}
