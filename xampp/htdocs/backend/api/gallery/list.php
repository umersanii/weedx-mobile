<?php
/**
 * Gallery List Endpoint
 * GET /api/gallery
 */

require_once __DIR__ . '/../../config/database.php';
require_once __DIR__ . '/../../utils/response.php';
require_once __DIR__ . '/../../utils/auth.php';
require_once __DIR__ . '/../../utils/image_helper.php';

$tokenData = Auth::validateToken();
$database = new Database();
$db = $database->getConnection();

try {
    $limit = $_GET['limit'] ?? 50;
    $offset = $_GET['offset'] ?? 0;
    $userId = $tokenData['user_id'] ?? null;
    
    $query = "
        SELECT id, weed_type, confidence, latitude, longitude, 
               image_base64, image_mime_type, detected_at 
        FROM weed_detections 
        WHERE image_base64 IS NOT NULL
    ";
    
    // Filter by user if user_id is provided
    if ($userId) {
        $query .= " AND (user_id = :user_id OR user_id IS NULL)";
    }
    
    $query .= " ORDER BY detected_at DESC LIMIT :limit OFFSET :offset";
    
    $stmt = $db->prepare($query);
    if ($userId) {
        $stmt->bindParam(':user_id', $userId, PDO::PARAM_INT);
    }
    $stmt->bindParam(':limit', $limit, PDO::PARAM_INT);
    $stmt->bindParam(':offset', $offset, PDO::PARAM_INT);
    $stmt->execute();
    
    $images = $stmt->fetchAll();
    
    $response = array_map(function($image) {
        // Convert relative path to full URL using ImageHelper
        $fullUrl = ImageHelper::getFullUrl($image['image_path']);
        return [
            'id' => (int)$image['id'],
<<<<<<< HEAD
            'url' => $fullUrl,
            'thumbnail_url' => $fullUrl, // TODO: Generate thumbnails
=======
            'image_base64' => $image['image_base64'],
            'image_mime_type' => $image['image_mime_type'],
>>>>>>> e54912b (images endpoint)
            'weed_type' => $image['weed_type'],
            'confidence' => (float)$image['confidence'],
            'location' => [
                'latitude' => (float)$image['latitude'],
                'longitude' => (float)$image['longitude']
            ],
            'captured_at' => $image['detected_at']
        ];
    }, $images);
    
    Response::success($response);
} catch (Exception $e) {
    Response::error('Failed to fetch gallery: ' . $e->getMessage(), 500);
}
