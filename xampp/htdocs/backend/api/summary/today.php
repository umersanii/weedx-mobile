<?php
/**
 * Today's Summary Endpoint
 * GET /api/summary/today
 */

require_once __DIR__ . '/../../config/database.php';
require_once __DIR__ . '/../../utils/response.php';
require_once __DIR__ . '/../../utils/auth.php';
require_once __DIR__ . '/../../utils/logger.php';

Logger::logRequest('/api/summary/today', 'GET');

// Validate token
$tokenData = Auth::validateToken();
Logger::logAuth('/api/summary/today', $tokenData['userId'] ?? null, true);

// Connect to database
$database = new Database();
$db = $database->getConnection();

try {
    // Weeds detected today
    $weedsQuery = "SELECT COUNT(*) as count FROM weed_detections WHERE user_id = :user_id AND DATE(detected_at) = CURDATE()";
    $weedsStmt = $db->prepare($weedsQuery);
    $weedsStmt->bindParam(':user_id', $tokenData['userId'], PDO::PARAM_INT);
    $weedsStmt->execute();
    $weeds = $weedsStmt->fetch();
    
    // Area covered today
    $areaQuery = "SELECT COALESCE(SUM(area_covered), 0) as area FROM robot_sessions WHERE user_id = :user_id AND DATE(start_time) = CURDATE()";
    $areaStmt = $db->prepare($areaQuery);
    $areaStmt->bindParam(':user_id', $tokenData['userId'], PDO::PARAM_INT);
    $areaStmt->execute();
    $area = $areaStmt->fetch();
    
    // Herbicide used today
    $herbicideQuery = "SELECT COALESCE(SUM(herbicide_used), 0) as herbicide FROM robot_sessions WHERE user_id = :user_id AND DATE(start_time) = CURDATE()";
    $herbicideStmt = $db->prepare($herbicideQuery);
    $herbicideStmt->bindParam(':user_id', $tokenData['userId'], PDO::PARAM_INT);
    $herbicideStmt->execute();
    $herbicide = $herbicideStmt->fetch();
    
    // Operating hours today
    $hoursQuery = "
        SELECT COALESCE(SUM(TIMESTAMPDIFF(MINUTE, start_time, end_time)), 0) / 60 as hours 
        FROM robot_sessions 
        WHERE user_id = :user_id AND DATE(start_time) = CURDATE() AND end_time IS NOT NULL
    ";
    $hoursStmt = $db->prepare($hoursQuery);
    $hoursStmt->bindParam(':user_id', $tokenData['userId'], PDO::PARAM_INT);
    $hoursStmt->execute();
    $hours = $hoursStmt->fetch();
    
    $response = [
        'weeds_detected' => (int)$weeds['count'],
        'area_covered' => (float)$area['area'],
        'herbicide_used' => (float)$herbicide['herbicide'],
        'operating_hours' => round((float)$hours['hours'], 2)
    ];
    
    Logger::logSuccess('/api/summary/today', 'Weeds: ' . $response['weeds_detected'] . ', Area: ' . $response['area_covered']);
    Response::success($response);
    
} catch (Exception $e) {
    Logger::logError('/api/summary/today', $e->getMessage(), 500);
    Response::error('Failed to fetch summary: ' . $e->getMessage(), 500);
}
