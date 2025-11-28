<?php
/**
 * Weed Detections Endpoint
 * GET /api/weed-logs/detections
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
    $weedType = $_GET['type'] ?? null;
    
    $query = "SELECT * FROM weed_detections";
    
    if ($weedType) {
        $query .= " WHERE weed_type = :weed_type";
    }
    
    $query .= " ORDER BY detected_at DESC LIMIT :limit OFFSET :offset";
    
    $stmt = $db->prepare($query);
    
    if ($weedType) {
        $stmt->bindParam(':weed_type', $weedType);
    }
    
    $stmt->bindParam(':limit', $limit, PDO::PARAM_INT);
    $stmt->bindParam(':offset', $offset, PDO::PARAM_INT);
    $stmt->execute();
    
    $detections = $stmt->fetchAll();
    
    $response = array_map(function($detection) {
        // Convert relative path to full URL using ImageHelper
        $imageUrl = $detection['image_path'] ? ImageHelper::getFullUrl($detection['image_path']) : null;
        return [
            'id' => (int)$detection['id'],
            'weed_type' => $detection['weed_type'],
            'confidence' => (float)$detection['confidence'],
            'location' => [
                'latitude' => (float)$detection['latitude'],
                'longitude' => (float)$detection['longitude']
            ],
            'image_url' => $imageUrl,
            'crop_type' => $detection['crop_type'] ?? null,
            'detected_at' => $detection['detected_at']
        ];
    }, $detections);
    
    Response::success($response);
} catch (Exception $e) {
    Response::error('Failed to fetch detections: ' . $e->getMessage(), 500);
}
