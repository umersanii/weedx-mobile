<?php
/**
 * Monitoring Metrics Endpoint
 * GET /api/monitoring/metrics
 */

require_once __DIR__ . '/../../config/database.php';
require_once __DIR__ . '/../../utils/response.php';
require_once __DIR__ . '/../../utils/auth.php';
require_once __DIR__ . '/../../utils/logger.php';

Logger::logRequest('/api/monitoring/metrics', 'GET');

$tokenData = Auth::validateToken();
Logger::logAuth('/api/monitoring/metrics', $tokenData['userId'] ?? null, true);
$database = new Database();
$db = $database->getConnection();

try {
    $query = "SELECT * FROM robot_status ORDER BY updated_at DESC LIMIT 1";
    $status = $db->query($query)->fetch();
    
    $response = [
        'battery' => (int)($status['battery_level'] ?? 0),
        'herbicide_level' => (int)($status['herbicide_level'] ?? 0),
        'coverage' => (float)($status['area_covered_today'] ?? 0),
        'efficiency' => (float)($status['efficiency'] ?? 85.5)
    ];
    
    Logger::logSuccess('/api/monitoring/metrics', 'Battery: ' . $response['battery'] . '%, Herbicide: ' . $response['herbicide_level'] . '%');
    Response::success($response);
} catch (Exception $e) {
    Logger::logError('/api/monitoring/metrics', $e->getMessage(), 500);
    Response::error('Failed to fetch metrics: ' . $e->getMessage(), 500);
}
