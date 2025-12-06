<?php
/**
 * Report Widgets Endpoint
 * GET /api/reports/widgets
 */

require_once __DIR__ . '/../../config/database.php';
require_once __DIR__ . '/../../utils/response.php';
require_once __DIR__ . '/../../utils/auth.php';
require_once __DIR__ . '/../../utils/logger.php';

Logger::logRequest('/api/reports/widgets', 'GET');

$tokenData = Auth::validateToken();
Logger::logAuth('/api/reports/widgets', $tokenData['userId'] ?? null, true);
$database = new Database();
$db = $database->getConnection();

try {
    $totalWeedsQuery = "SELECT COUNT(*) as total FROM weed_detections WHERE user_id = :user_id";
    $totalWeedsStmt = $db->prepare($totalWeedsQuery);
    $totalWeedsStmt->bindParam(':user_id', $tokenData['userId'], PDO::PARAM_INT);
    $totalWeedsStmt->execute();
    $totalWeeds = $totalWeedsStmt->fetch();
    
    $areaQuery = "SELECT COALESCE(SUM(area_covered), 0) as total FROM robot_sessions WHERE user_id = :user_id";
    $areaStmt = $db->prepare($areaQuery);
    $areaStmt->bindParam(':user_id', $tokenData['userId'], PDO::PARAM_INT);
    $areaStmt->execute();
    $area = $areaStmt->fetch();
    
    $herbicideQuery = "SELECT COALESCE(SUM(herbicide_used), 0) as total FROM robot_sessions WHERE user_id = :user_id";
    $herbicideStmt = $db->prepare($herbicideQuery);
    $herbicideStmt->bindParam(':user_id', $tokenData['userId'], PDO::PARAM_INT);
    $herbicideStmt->execute();
    $herbicide = $herbicideStmt->fetch();
    
    $response = [
        'total_weeds' => (int)$totalWeeds['total'],
        'area_covered' => (float)$area['total'],
        'herbicide_used' => (float)$herbicide['total'],
        'efficiency' => 87.5
    ];
    
    Logger::logSuccess('/api/reports/widgets', 'Widgets fetched, Total weeds: ' . $response['total_weeds']);
    Response::success($response);
} catch (Exception $e) {
    Logger::logError('/api/reports/widgets', $e->getMessage(), 500);
    Response::error('Failed to fetch widgets: ' . $e->getMessage(), 500);
}
