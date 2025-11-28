<?php
/**
 * Weed Trend Endpoint
 * GET /api/reports/weed-trend
 */

require_once __DIR__ . '/../../config/database.php';
require_once __DIR__ . '/../../utils/response.php';
require_once __DIR__ . '/../../utils/auth.php';
require_once __DIR__ . '/../../utils/logger.php';

Logger::logRequest('/api/reports/weed-trend', 'GET');

$tokenData = Auth::validateToken();
Logger::logAuth('/api/reports/weed-trend', $tokenData['userId'] ?? null, true);
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
    
    Logger::logSuccess('/api/reports/weed-trend', 'Fetched ' . count($response) . ' days of trend data');
    Response::success($response);
} catch (Exception $e) {
    Logger::logError('/api/reports/weed-trend', $e->getMessage(), 500);
    Response::error('Failed to fetch weed trend: ' . $e->getMessage(), 500);
}
