<?php
/**
 * Environment Overview Endpoint
 * GET /api/environment
 * 
 * Query params (optional):
 *   lat - latitude (default: 31.5204 - Lahore)
 *   lon - longitude (default: 74.3587 - Lahore)
 */

require_once __DIR__ . '/../../config/database.php';
require_once __DIR__ . '/../../utils/response.php';
require_once __DIR__ . '/../../utils/auth.php';
require_once __DIR__ . '/../../utils/logger.php';
require_once __DIR__ . '/../../utils/weather_service.php';

Logger::logRequest('/api/environment', 'GET');

$tokenData = Auth::validateToken();
Logger::logAuth('/api/environment', $tokenData['userId'] ?? null, true);
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
    
    // Geocode farm location to get coordinates
    $lat = null;
    $lon = null;
    $locationName = 'Default';
    
    if ($farm && $farm['location']) {
        $geoData = $weatherService->geocode($farm['location'], $farm['country']);
        $lat = $geoData['latitude'];
        $lon = $geoData['longitude'];
        $locationName = $farm['location'] . ', ' . ($farm['country'] ?? '');
    }
    
    // Fetch real current weather from API
    $currentWeather = $weatherService->getCurrentWeather($lat, $lon);
    $currentWeather['location']['name'] = $locationName;
    
    // Fetch real 7-day forecast from API
    $forecastData = $weatherService->getForecast(7, $lat, $lon);
    
    // Soil data from database (robot sensors)
    $soilQuery = "SELECT * FROM soil_data ORDER BY recorded_at DESC LIMIT 1";
    $soil = $db->query($soilQuery)->fetch();
    
    // Generate farming recommendations based on weather
    $recommendations = generateRecommendations($currentWeather, $forecastData['forecast']);
    
    $response = [
        'current_weather' => $currentWeather,
        'forecast' => $forecastData['forecast'],
        'soil' => [
            'moisture' => (float)($soil['moisture'] ?? 0),
            'temperature' => (float)($soil['temperature'] ?? 0),
            'ph' => (float)($soil['ph'] ?? 0),
            'nitrogen' => (int)($soil['nitrogen'] ?? 0),
            'phosphorus' => (int)($soil['phosphorus'] ?? 0),
            'potassium' => (int)($soil['potassium'] ?? 0),
            'recorded_at' => $soil['recorded_at'] ?? null
        ],
        'recommendations' => $recommendations,
        'location' => $currentWeather['location']
    ];
    
    Logger::logSuccess('/api/environment', 'Environment data fetched with real weather');
    Response::success($response);
} catch (Exception $e) {
    Logger::logError('/api/environment', $e->getMessage(), 500);
    Response::error('Failed to fetch environment data: ' . $e->getMessage(), 500);
}

/**
 * Generate farming recommendations based on weather conditions
 */
function generateRecommendations($current, $forecast) {
    $recommendations = [];
    
    // Temperature-based recommendations
    if ($current['temperature'] >= 20 && $current['temperature'] <= 30) {
        $recommendations[] = 'Temperature is optimal for most farming activities';
    } elseif ($current['temperature'] > 35) {
        $recommendations[] = 'High temperature - work early morning or evening';
    } elseif ($current['temperature'] < 10) {
        $recommendations[] = 'Low temperature - protect sensitive crops';
    }
    
    // Humidity-based recommendations
    if ($current['humidity'] > 80) {
        $recommendations[] = 'High humidity - watch for fungal diseases';
    } elseif ($current['humidity'] < 30) {
        $recommendations[] = 'Low humidity - increase irrigation';
    }
    
    // Wind-based recommendations
    if ($current['wind_speed'] > 20) {
        $recommendations[] = 'High wind - avoid spraying chemicals';
    } else {
        $recommendations[] = 'Wind conditions favorable for spraying';
    }
    
    // Condition-based recommendations
    $condition = strtolower($current['condition']);
    if (strpos($condition, 'rain') !== false || strpos($condition, 'drizzle') !== false) {
        $recommendations[] = 'Rain expected - delay outdoor work if possible';
    } elseif (strpos($condition, 'clear') !== false || strpos($condition, 'sunny') !== false) {
        $recommendations[] = 'Clear weather - ideal for field operations';
    }
    
    // Check upcoming precipitation
    $rainDays = 0;
    foreach ($forecast as $day) {
        if ($day['precipitation_chance'] > 50) {
            $rainDays++;
        }
    }
    if ($rainDays > 3) {
        $recommendations[] = 'Rain expected in coming days - plan indoor tasks';
    }
    
    // Best time recommendation
    $hour = (int)date('H');
    if ($hour >= 6 && $hour <= 10) {
        $recommendations[] = 'Currently in optimal morning work window (6-10 AM)';
    } elseif ($hour >= 16 && $hour <= 19) {
        $recommendations[] = 'Currently in optimal evening work window (4-7 PM)';
    }
    
    return $recommendations;
}
