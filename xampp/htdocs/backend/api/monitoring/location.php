<?php
/**
 * Monitoring Location Endpoint
 * GET /api/monitoring/location
 */

require_once __DIR__ . '/../../config/database.php';
require_once __DIR__ . '/../../utils/response.php';
require_once __DIR__ . '/../../utils/auth.php';
require_once __DIR__ . '/../../utils/logger.php';

Logger::logRequest('/api/monitoring/location', 'GET');

$tokenData = Auth::validateToken();
Logger::logAuth('/api/monitoring/location', $tokenData['userId'] ?? null, true);
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
    
    Logger::logSuccess('/api/monitoring/location', 'Lat: ' . $response['latitude'] . ', Lng: ' . $response['longitude']);
    Response::success($response);
} catch (Exception $e) {
    Logger::logError('/api/monitoring/location', $e->getMessage(), 500);
    Response::error('Failed to fetch location: ' . $e->getMessage(), 500);
}
