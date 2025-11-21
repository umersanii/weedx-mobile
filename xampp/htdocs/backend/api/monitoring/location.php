<?php
/**
 * Monitoring Location Endpoint
 * GET /api/monitoring/location
 */

require_once __DIR__ . '/../../config/database.php';
require_once __DIR__ . '/../../utils/response.php';
require_once __DIR__ . '/../../utils/auth.php';

$tokenData = Auth::validateToken();
$database = new Database();
$db = $database->getConnection();

try {
    $query = "SELECT latitude, longitude, speed, heading, updated_at FROM robot_status ORDER BY updated_at DESC LIMIT 1";
    $location = $db->query($query)->fetch();
    
    $response = [
        'latitude' => (float)($location['latitude'] ?? 0),
        'longitude' => (float)($location['longitude'] ?? 0),
        'speed' => (float)($location['speed'] ?? 0),
        'heading' => (float)($location['heading'] ?? 0),
        'last_updated' => $location['updated_at'] ?? null
    ];
    
    Response::success($response);
} catch (Exception $e) {
    Response::error('Failed to fetch location: ' . $e->getMessage(), 500);
}
