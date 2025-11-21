<?php
/**
 * Weed Distribution Endpoint
 * GET /api/reports/weed-distribution
 */

require_once __DIR__ . '/../../config/database.php';
require_once __DIR__ . '/../../utils/response.php';
require_once __DIR__ . '/../../utils/auth.php';

$tokenData = Auth::validateToken();
$database = new Database();
$db = $database->getConnection();

try {
    $query = "
        SELECT crop_type, weed_type, COUNT(*) as count 
        FROM weed_detections 
        GROUP BY crop_type, weed_type
        ORDER BY crop_type, count DESC
    ";
    $distribution = $db->query($query)->fetchAll();
    
    $response = array_map(function($item) {
        return [
            'crop_type' => $item['crop_type'] ?? 'Unknown',
            'weed_type' => $item['weed_type'],
            'count' => (int)$item['count']
        ];
    }, $distribution);
    
    Response::success($response);
} catch (Exception $e) {
    Response::error('Failed to fetch weed distribution: ' . $e->getMessage(), 500);
}
