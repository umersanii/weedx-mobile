<?php
/**
 * Hourly Weather Forecast Endpoint
 * GET /api/environment/weather/hourly
 * 
 * Uses farm location from user profile (location + country fields)
 * Query params (optional):
 *   hours - number of hours (1-168, default: 24)
 */

require_once __DIR__ . '/../../../config/database.php';
require_once __DIR__ . '/../../../utils/response.php';
require_once __DIR__ . '/../../../utils/auth.php';
require_once __DIR__ . '/../../../utils/logger.php';
require_once __DIR__ . '/../../../utils/weather_service.php';

Logger::logRequest('/api/environment/weather/hourly', 'GET');

$tokenData = Auth::validateToken();
Logger::logAuth('/api/environment/weather/hourly', $tokenData['userId'] ?? null, true);

$database = new Database();
$db = $database->getConnection();

try {
    $weatherService = new WeatherService();
    $userId = $tokenData['userId'];
    $hours = isset($_GET['hours']) ? (int)$_GET['hours'] : 24;
    
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
    
    // Fetch hourly forecast from Open-Meteo API
    $data = $weatherService->getHourlyForecast($hours, $lat, $lon);
    $data['location']['name'] = $locationName;
    
    Logger::logSuccess('/api/environment/weather/hourly', 'Fetched ' . count($data['hourly']) . ' hours forecast');
    Response::success($data);
} catch (Exception $e) {
    Logger::logError('/api/environment/weather/hourly', $e->getMessage(), 500);
    Response::error('Failed to fetch hourly forecast: ' . $e->getMessage(), 500);
}
