<?php
/**
 * Today's Recommendations Endpoint
 * GET /api/environment/recommendations/today
 */

require_once __DIR__ . '/../../../config/database.php';
require_once __DIR__ . '/../../../utils/response.php';
require_once __DIR__ . '/../../../utils/auth.php';

$tokenData = Auth::validateToken();
$database = new Database();
$db = $database->getConnection();

try {
    // Get current conditions
    $weatherQuery = "SELECT * FROM weather_data ORDER BY recorded_at DESC LIMIT 1";
    $weather = $db->query($weatherQuery)->fetch();
    
    $soilQuery = "SELECT * FROM soil_data ORDER BY recorded_at DESC LIMIT 1";
    $soil = $db->query($soilQuery)->fetch();
    
    $recommendations = [];
    
    // Weather-based recommendations
    if ($weather) {
        $temp = (float)$weather['temperature'];
        $humidity = (int)$weather['humidity'];
        $windSpeed = (float)$weather['wind_speed'];
        
        if ($temp >= 15 && $temp <= 30) {
            $recommendations[] = 'Temperature is optimal for herbicide application';
        }
        
        if ($humidity > 80) {
            $recommendations[] = 'High humidity - ideal for soil preparation';
        }
        
        if ($windSpeed > 15) {
            $recommendations[] = 'High winds - avoid spraying operations';
        } else {
            $recommendations[] = 'Wind conditions favorable for spraying';
        }
    }
    
    // Soil-based recommendations
    if ($soil) {
        $moisture = (float)$soil['moisture'];
        $ph = (float)$soil['ph'];
        
        if ($moisture < 30) {
            $recommendations[] = 'Soil moisture low - consider irrigation';
        } elseif ($moisture > 70) {
            $recommendations[] = 'Soil moisture high - wait before heavy operations';
        } else {
            $recommendations[] = 'Soil moisture optimal for weeding operations';
        }
        
        if ($ph < 6.0) {
            $recommendations[] = 'Soil pH acidic - consider lime application';
        } elseif ($ph > 7.5) {
            $recommendations[] = 'Soil pH alkaline - monitor nutrient availability';
        }
    }
    
    // Time-based recommendations
    $hour = (int)date('H');
    if ($hour >= 6 && $hour < 10) {
        $recommendations[] = 'Best time for field operations - early morning';
    } elseif ($hour >= 16 && $hour < 19) {
        $recommendations[] = 'Good time for operations - late afternoon';
    } elseif ($hour >= 11 && $hour < 15) {
        $recommendations[] = 'Midday heat - avoid prolonged operations';
    }
    
    Response::success($recommendations);
} catch (Exception $e) {
    Response::error('Failed to generate recommendations: ' . $e->getMessage(), 500);
}
