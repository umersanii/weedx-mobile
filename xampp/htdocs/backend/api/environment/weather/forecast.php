<?php
/**
 * Weather Forecast Endpoint
 * GET /api/environment/weather/forecast
 * 
 * Uses farm location from user profile (location + country fields)
 * Query params (optional):
 *   days - number of forecast days (1-16, default: 7)
 */

require_once __DIR__ . '/../../../config/database.php';
require_once __DIR__ . '/../../../utils/response.php';
require_once __DIR__ . '/../../../utils/auth.php';
require_once __DIR__ . '/../../../utils/logger.php';
require_once __DIR__ . '/../../../utils/weather_service.php';

Logger::logRequest('/api/environment/weather/forecast', 'GET');

$tokenData = Auth::validateToken();
Logger::logAuth('/api/environment/weather/forecast', $tokenData['userId'] ?? null, true);

$database = new Database();
$db = $database->getConnection();

try {
    $weatherService = new WeatherService();
    $userId = $tokenData['userId'];
    $days = isset($_GET['days']) ? (int)$_GET['days'] : 7;
    
    // Get user's farm location from database
    $farmQuery = "SELECT location FROM farms WHERE user_id = :userId LIMIT 1";
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
    
    // Fetch real forecast from Open-Meteo API
    $data = $weatherService->getForecast($days, $lat, $lon);
    $data['location']['name'] = $locationName;
    
    Logger::logSuccess('/api/environment/weather/forecast', 'Fetched ' . count($data['forecast']) . ' days forecast');
    Response::success($data);
} catch (Exception $e) {
    Logger::logError('/api/environment/weather/forecast', $e->getMessage(), 500);
    Response::error('Failed to fetch forecast: ' . $e->getMessage(), 500);
}
