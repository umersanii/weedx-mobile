<?php
/**
 * Weed Trend Endpoint
 * GET /api/reports/weed-trend
 */

require_once __DIR__ . '/../../config/database.php';
require_once __DIR__ . '/../../utils/response.php';
require_once __DIR__ . '/../../utils/auth.php';

$tokenData = Auth::validateToken();
$database = new Database();
$db = $database->getConnection();

try {
    $days = $_GET['days'] ?? 30;
    
    $query = "
        SELECT DATE(detected_at) as date, COUNT(*) as count 
        FROM weed_detections 
        WHERE detected_at >= DATE_SUB(CURDATE(), INTERVAL :days DAY)
        GROUP BY DATE(detected_at)
        ORDER BY date ASC
    ";
    $stmt = $db->prepare($query);
    $stmt->bindParam(':days', $days, PDO::PARAM_INT);
    $stmt->execute();
    
    $trend = $stmt->fetchAll();
    
    $response = array_map(function($item) {
        return [
            'date' => $item['date'],
            'count' => (int)$item['count']
        ];
    }, $trend);
    
    Response::success($response);
} catch (Exception $e) {
    Response::error('Failed to fetch weed trend: ' . $e->getMessage(), 500);
}
