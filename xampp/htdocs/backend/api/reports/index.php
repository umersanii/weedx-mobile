<?php
/**
 * Reports Overview Endpoint
 * GET /api/reports
 */

require_once __DIR__ . '/../../config/database.php';
require_once __DIR__ . '/../../utils/response.php';
require_once __DIR__ . '/../../utils/auth.php';
require_once __DIR__ . '/../../utils/logger.php';

Logger::logRequest('/api/reports', 'GET');

$tokenData = Auth::validateToken();
Logger::logAuth('/api/reports', $tokenData['userId'] ?? null, true);
$database = new Database();
$db = $database->getConnection();

try {
    // Widgets
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
    
    // Weed trend (last 30 days)
    $trendQuery = "
        SELECT DATE(detected_at) as date, COUNT(*) as count 
        FROM weed_detections 
        WHERE user_id = :user_id AND detected_at >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
        GROUP BY DATE(detected_at)
        ORDER BY date ASC
    ";
    $trendStmt = $db->prepare($trendQuery);
    $trendStmt->bindParam(':user_id', $tokenData['userId'], PDO::PARAM_INT);
    $trendStmt->execute();
    $trend = $trendStmt->fetchAll();
    
    // Weed distribution by crop
    $distributionQuery = "
        SELECT crop_type, weed_type, COUNT(*) as count 
        FROM weed_detections 
        WHERE user_id = :user_id
        GROUP BY crop_type, weed_type
        ORDER BY crop_type, count DESC
    ";
    $distributionStmt = $db->prepare($distributionQuery);
    $distributionStmt->bindParam(':user_id', $tokenData['userId'], PDO::PARAM_INT);
    $distributionStmt->execute();
    $distribution = $distributionStmt->fetchAll();
    
    $response = [
        'widgets' => [
            'total_weeds' => (int)$totalWeeds['total'],
            'area_covered' => (float)$area['total'],
            'herbicide_used' => (float)$herbicide['total'],
            'efficiency' => 87.5
        ],
        'weed_trend' => array_map(function($item) {
            return [
                'date' => $item['date'],
                'count' => (int)$item['count']
            ];
        }, $trend),
        'weed_distribution' => $distribution
    ];
    
    Logger::logSuccess('/api/reports', 'Reports data fetched, Total weeds: ' . $response['widgets']['total_weeds']);
    Response::success($response);
} catch (Exception $e) {
    Logger::logError('/api/reports', $e->getMessage(), 500);
    Response::error('Failed to fetch reports: ' . $e->getMessage(), 500);
}
