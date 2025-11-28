<?php
/**
 * Current Weather Endpoint
 * GET /api/environment/weather/current
 * 
 * Uses farm location from user profile (location + country fields)
 */

require_once __DIR__ . '/../../../config/database.php';
require_once __DIR__ . '/../../../utils/response.php';
require_once __DIR__ . '/../../../utils/auth.php';
require_once __DIR__ . '/../../../utils/logger.php';
require_once __DIR__ . '/../../../utils/weather_service.php';

Logger::logRequest('/api/environment/weather/current', 'GET');

$tokenData = Auth::validateToken();
Logger::logAuth('/api/environment/weather/current', $tokenData['userId'] ?? null, true);

$database = new Database();
$db = $database->getConnection();

try {
    $weatherService = new WeatherService();
    $userId = $tokenData['userId'];
    
    // Get user's farm location from database
    $farmQuery = "SELECT location, country FROM farms WHERE user_id = :userId LIMIT 1";
    $farmStmt = $db->prepare($farmQuery);
    $farmStmt->execute([':userId' => $userId]);
    $farm = $farmStmt->fetch();
    
    // Geocode farm location
    $lat = null;
    $lon = null;
    $locationName = 'Default';
    
    if ($farm && $farm['location']) {
        $geoData = $weatherService->geocode($farm['location'], $farm['country']);
        $lat = $geoData['latitude'];
        $lon = $geoData['longitude'];
        $locationName = $farm['location'] . ', ' . ($farm['country'] ?? '');
    }
    
    // Fetch real weather from Open-Meteo API
    $response = $weatherService->getCurrentWeather($lat, $lon);
    $response['location']['name'] = $locationName;
    
    Logger::logSuccess('/api/environment/weather/current', 'Temp: ' . $response['temperature'] . '°C, Condition: ' . $response['condition']);
    Response::success($response);
} catch (Exception $e) {
    Logger::logError('/api/environment/weather/current', $e->getMessage(), 500);
    Response::error('Failed to fetch weather: ' . $e->getMessage(), 500);
}
