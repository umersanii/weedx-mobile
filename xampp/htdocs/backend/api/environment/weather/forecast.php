<?php
/**
 * Weather Forecast Endpoint
 * GET /api/environment/weather/forecast
 */

require_once __DIR__ . '/../../../config/database.php';
require_once __DIR__ . '/../../../utils/response.php';
require_once __DIR__ . '/../../../utils/auth.php';
require_once __DIR__ . '/../../../utils/logger.php';

Logger::logRequest('/api/environment/weather/forecast', 'GET');

$tokenData = Auth::validateToken();
Logger::logAuth('/api/environment/weather/forecast', $tokenData['userId'] ?? null, true);
$database = new Database();
$db = $database->getConnection();

try {
    $days = $_GET['days'] ?? 7;
    
    $query = "SELECT * FROM weather_forecast ORDER BY forecast_date ASC LIMIT :days";
    $stmt = $db->prepare($query);
    $stmt->bindParam(':days', $days, PDO::PARAM_INT);
    $stmt->execute();
    
    $forecast = $stmt->fetchAll();
    
    $response = array_map(function($day) {
        return [
            'date' => $day['forecast_date'],
            'temp_high' => (float)$day['temp_high'],
            'temp_low' => (float)$day['temp_low'],
            'condition' => $day['weather_condition'],
            'precipitation_chance' => (int)$day['precipitation_chance'],
            'humidity' => (int)$day['humidity'],
            'wind_speed' => (float)$day['wind_speed']
        ];
    }, $forecast);
    
    Logger::logSuccess('/api/environment/weather/forecast', 'Fetched ' . count($response) . ' days forecast');
    Response::success($response);
} catch (Exception $e) {
    Logger::logError('/api/environment/weather/forecast', $e->getMessage(), 500);
    Response::error('Failed to fetch forecast: ' . $e->getMessage(), 500);
}
