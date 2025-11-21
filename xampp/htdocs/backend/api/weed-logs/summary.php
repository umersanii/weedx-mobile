<?php
/**
 * Weed Logs Summary Endpoint
 * GET /api/weed-logs/summary
 */

require_once __DIR__ . '/../../config/database.php';
require_once __DIR__ . '/../../utils/response.php';
require_once __DIR__ . '/../../utils/auth.php';

$tokenData = Auth::validateToken();
$database = new Database();
$db = $database->getConnection();

try {
    $query = "
        SELECT weed_type, COUNT(*) as count 
        FROM weed_detections 
        GROUP BY weed_type
        ORDER BY count DESC
    ";
    $summary = $db->query($query)->fetchAll();
    
    $response = array_map(function($item) {
        return [
            'weed_type' => $item['weed_type'],
            'count' => (int)$item['count']
        ];
    }, $summary);
    
    Response::success($response);
} catch (Exception $e) {
    Response::error('Failed to fetch weed summary: ' . $e->getMessage(), 500);
}
