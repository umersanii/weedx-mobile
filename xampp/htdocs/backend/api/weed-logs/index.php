<?php
/**
 * Weed Logs Overview Endpoint
 * GET /api/weed-logs
 */

require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../utils/response.php';
require_once __DIR__ . '/../utils/auth.php';
require_once __DIR__ . '/../utils/logger.php';

Logger::logRequest('/api/weed-logs', 'GET');

$tokenData = Auth::validateToken();
Logger::logAuth('/api/weed-logs', $tokenData['userId'] ?? null, true);
$database = new Database();
$db = $database->getConnection();

try {
    // Get summary by weed type
    $summaryQuery = "
        SELECT weed_type, COUNT(*) as count 
        FROM weed_detections 
        WHERE user_id = :user_id
        GROUP BY weed_type
        ORDER BY count DESC
    ";
    $summaryStmt = $db->prepare($summaryQuery);
    $summaryStmt->bindParam(':user_id', $tokenData['userId'], PDO::PARAM_INT);
    $summaryStmt->execute();
    $summary = $summaryStmt->fetchAll();
    
    // Get recent detections
    $detectionsQuery = "
        SELECT * FROM weed_detections 
        WHERE user_id = :user_id
        ORDER BY detected_at DESC 
        LIMIT 50
    ";
    $detectionsStmt = $db->prepare($detectionsQuery);
    $detectionsStmt->bindParam(':user_id', $tokenData['userId'], PDO::PARAM_INT);
    $detectionsStmt->execute();
    $detections = $detectionsStmt->fetchAll();
    
    $response = [
        'summary' => array_map(function($item) {
            return [
                'weed_type' => $item['weed_type'],
                'count' => (int)$item['count']
            ];
        }, $summary),
        'detections' => array_map(function($detection) {
            return [
                'id' => (int)$detection['id'],
                'weed_type' => $detection['weed_type'],
                'confidence' => (float)$detection['confidence'],
                'location' => [
                    'latitude' => (float)$detection['latitude'],
                    'longitude' => (float)$detection['longitude']
                ],
                'image_url' => $detection['image_path'] ?? null,
                'detected_at' => $detection['detected_at']
            ];
        }, $detections)
    ];
    
    Logger::logSuccess('/api/weed-logs', 'Fetched ' . count($response['detections']) . ' detections');
    Response::success($response);
} catch (Exception $e) {
    Logger::logError('/api/weed-logs', $e->getMessage(), 500);
    Response::error('Failed to fetch weed logs: ' . $e->getMessage(), 500);
}
