<?php
/**
 * Robot Status Endpoint
 * GET /api/robot/status
 */

require_once __DIR__ . '/../../config/database.php';
require_once __DIR__ . '/../../utils/response.php';
require_once __DIR__ . '/../../utils/auth.php';
require_once __DIR__ . '/../../utils/logger.php';

Logger::logRequest('/api/robot/status', 'GET');

// Validate token
$tokenData = Auth::validateToken();
Logger::logAuth('/api/robot/status', $tokenData['userId'] ?? null, true);

// Connect to database
$database = new Database();
$db = $database->getConnection();

try {
    $query = "SELECT * FROM robot_status ORDER BY updated_at DESC LIMIT 1";
    $stmt = $db->query($query);
    $status = $stmt->fetch();
    
    if (!$status) {
        Response::success([
            'status' => 'offline',
            'battery' => 0,
            'location' => ['latitude' => 0, 'longitude' => 0],
            'speed' => 0,
            'last_updated' => null
        ]);
    }
    
    $response = [
        'status' => $status['status'],
        'battery' => (int)$status['battery_level'],
        'location' => [
            'latitude' => (float)$status['latitude'],
            'longitude' => (float)$status['longitude']
        ],
        'speed' => (float)$status['speed'],
        'activity' => $status['activity'],
        'last_updated' => $status['updated_at']
    ];
    
    Logger::logSuccess('/api/robot/status', 'Robot status: ' . $response['status']);
    Response::success($response);
    
} catch (Exception $e) {
    Logger::logError('/api/robot/status', $e->getMessage(), 500);
    Response::error('Failed to fetch robot status: ' . $e->getMessage(), 500);
}
