<?php
/**
 * Gallery View Single Image Endpoint
 * GET /api/gallery/:id
 */

require_once __DIR__ . '/../../config/database.php';
require_once __DIR__ . '/../../utils/response.php';
require_once __DIR__ . '/../../utils/auth.php';

$tokenData = Auth::validateToken();
$database = new Database();
$db = $database->getConnection();

$imageId = $_GET['id'] ?? null;

if (!$imageId) {
    Response::error('Image ID required', 400);
}

try {
    $query = "SELECT * FROM weed_detections WHERE id = :id LIMIT 1";
    $stmt = $db->prepare($query);
    $stmt->bindParam(':id', $imageId, PDO::PARAM_INT);
    $stmt->execute();
    
    $image = $stmt->fetch();
    
    if (!$image) {
        Response::error('Image not found', 404);
    }
    
    $response = [
        'id' => (int)$image['id'],
        'url' => $image['image_path'],
        'weed_type' => $image['weed_type'],
        'confidence' => (float)$image['confidence'],
        'location' => [
            'latitude' => (float)$image['latitude'],
            'longitude' => (float)$image['longitude']
        ],
        'crop_type' => $image['crop_type'] ?? null,
        'captured_at' => $image['detected_at']
    ];
    
    Response::success($response);
} catch (Exception $e) {
    Response::error('Failed to fetch image: ' . $e->getMessage(), 500);
}
