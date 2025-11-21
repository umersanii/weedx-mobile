<?php
/**
 * Robot Status Endpoint
 * GET /api/robot/status
 */

require_once __DIR__ . '/../../config/database.php';
require_once __DIR__ . '/../../utils/response.php';
require_once __DIR__ . '/../../utils/auth.php';

// Validate token
$tokenData = Auth::validateToken();

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
    
    Response::success($response);
    
} catch (Exception $e) {
    Response::error('Failed to fetch robot status: ' . $e->getMessage(), 500);
}
