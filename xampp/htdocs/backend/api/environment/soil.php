<?php
/**
 * Soil Data Endpoint
 * GET /api/environment/soil
 */

require_once __DIR__ . '/../../config/database.php';
require_once __DIR__ . '/../../utils/response.php';
require_once __DIR__ . '/../../utils/auth.php';
require_once __DIR__ . '/../../utils/logger.php';

Logger::logRequest('/api/environment/soil', 'GET');

$tokenData = Auth::validateToken();
Logger::logAuth('/api/environment/soil', $tokenData['userId'] ?? null, true);
$database = new Database();
$db = $database->getConnection();

try {
    $query = "SELECT * FROM soil_data ORDER BY recorded_at DESC LIMIT 1";
    $soil = $db->query($query)->fetch();
    
    $response = [
        'moisture' => (float)($soil['moisture'] ?? 45.0),
        'temperature' => (float)($soil['temperature'] ?? 22.0),
        'ph' => (float)($soil['ph'] ?? 6.5),
        'nitrogen' => (int)($soil['nitrogen'] ?? 50),
        'phosphorus' => (int)($soil['phosphorus'] ?? 30),
        'potassium' => (int)($soil['potassium'] ?? 40),
        'organic_matter' => (float)($soil['organic_matter'] ?? 3.5),
        'recorded_at' => $soil['recorded_at'] ?? date('Y-m-d H:i:s')
    ];
    
    Logger::logSuccess('/api/environment/soil', 'Moisture: ' . $response['moisture'] . '%, pH: ' . $response['ph']);
    Response::success($response);
} catch (Exception $e) {
    Logger::logError('/api/environment/soil', $e->getMessage(), 500);
    Response::error('Failed to fetch soil data: ' . $e->getMessage(), 500);
}
