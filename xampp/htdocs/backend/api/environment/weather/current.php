<?php
/**
 * Current Weather Endpoint
 * GET /api/environment/weather/current
 */

require_once __DIR__ . '/../../../config/database.php';
require_once __DIR__ . '/../../../utils/response.php';
require_once __DIR__ . '/../../../utils/auth.php';
require_once __DIR__ . '/../../../utils/logger.php';

Logger::logRequest('/api/environment/weather/current', 'GET');

$tokenData = Auth::validateToken();
Logger::logAuth('/api/environment/weather/current', $tokenData['userId'] ?? null, true);
$database = new Database();
$db = $database->getConnection();

try {
    $query = "SELECT * FROM weather_data ORDER BY recorded_at DESC LIMIT 1";
    $weather = $db->query($query)->fetch();
    
    $response = [
        'temperature' => (float)($weather['temperature'] ?? 25.0),
        'humidity' => (int)($weather['humidity'] ?? 60),
        'condition' => $weather['weather_condition'] ?? 'Clear',
        'wind_speed' => (float)($weather['wind_speed'] ?? 10.5),
        'wind_direction' => $weather['wind_direction'] ?? 'N',
        'pressure' => (float)($weather['pressure'] ?? 1013.0),
        'recorded_at' => $weather['recorded_at'] ?? date('Y-m-d H:i:s')
    ];
    
    Logger::logSuccess('/api/environment/weather/current', 'Temp: ' . $response['temperature'] . '°C, Condition: ' . $response['condition']);
    Response::success($response);
} catch (Exception $e) {
    Logger::logError('/api/environment/weather/current', $e->getMessage(), 500);
    Response::error('Failed to fetch weather: ' . $e->getMessage(), 500);
}
