<?php
/**
 * Environment Overview Endpoint
 * GET /api/environment
 */

require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../utils/response.php';
require_once __DIR__ . '/../utils/auth.php';

$tokenData = Auth::validateToken();
$database = new Database();
$db = $database->getConnection();

try {
    // Current weather
    $weatherQuery = "SELECT * FROM weather_data ORDER BY recorded_at DESC LIMIT 1";
    $weather = $db->query($weatherQuery)->fetch();
    
    // 7-day forecast
    $forecastQuery = "SELECT * FROM weather_forecast ORDER BY forecast_date ASC LIMIT 7";
    $forecast = $db->query($forecastQuery)->fetchAll();
    
    // Soil data
    $soilQuery = "SELECT * FROM soil_data ORDER BY recorded_at DESC LIMIT 1";
    $soil = $db->query($soilQuery)->fetch();
    
    $response = [
        'current_weather' => [
            'temperature' => (float)($weather['temperature'] ?? 0),
            'humidity' => (int)($weather['humidity'] ?? 0),
            'condition' => $weather['weather_condition'] ?? 'Unknown',
            'wind_speed' => (float)($weather['wind_speed'] ?? 0),
            'recorded_at' => $weather['recorded_at'] ?? null
        ],
        'forecast' => array_map(function($day) {
            return [
                'date' => $day['forecast_date'],
                'temp_high' => (float)$day['temp_high'],
                'temp_low' => (float)$day['temp_low'],
                'condition' => $day['weather_condition'],
                'precipitation' => (int)$day['precipitation_chance']
            ];
        }, $forecast),
        'soil' => [
            'moisture' => (float)($soil['moisture'] ?? 0),
            'temperature' => (float)($soil['temperature'] ?? 0),
            'ph' => (float)($soil['ph'] ?? 0),
            'nitrogen' => (int)($soil['nitrogen'] ?? 0),
            'phosphorus' => (int)($soil['phosphorus'] ?? 0),
            'potassium' => (int)($soil['potassium'] ?? 0),
            'recorded_at' => $soil['recorded_at'] ?? null
        ],
        'recommendations' => [
            'Best time to work: Early morning (6-10 AM)',
            'Soil moisture is optimal for weeding',
            'Weather conditions favorable for spraying'
        ]
    ];
    
    Response::success($response);
} catch (Exception $e) {
    Response::error('Failed to fetch environment data: ' . $e->getMessage(), 500);
}
