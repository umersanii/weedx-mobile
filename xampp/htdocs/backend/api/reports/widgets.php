<?php
/**
 * Report Widgets Endpoint
 * GET /api/reports/widgets
 */

require_once __DIR__ . '/../../config/database.php';
require_once __DIR__ . '/../../utils/response.php';
require_once __DIR__ . '/../../utils/auth.php';

$tokenData = Auth::validateToken();
$database = new Database();
$db = $database->getConnection();

try {
    $totalWeedsQuery = "SELECT COUNT(*) as total FROM weed_detections";
    $totalWeeds = $db->query($totalWeedsQuery)->fetch();
    
    $areaQuery = "SELECT COALESCE(SUM(area_covered), 0) as total FROM robot_sessions";
    $area = $db->query($areaQuery)->fetch();
    
    $herbicideQuery = "SELECT COALESCE(SUM(herbicide_used), 0) as total FROM robot_sessions";
    $herbicide = $db->query($herbicideQuery)->fetch();
    
    $response = [
        'total_weeds' => (int)$totalWeeds['total'],
        'area_covered' => (float)$area['total'],
        'herbicide_used' => (float)$herbicide['total'],
        'efficiency' => 87.5
    ];
    
    Response::success($response);
} catch (Exception $e) {
    Response::error('Failed to fetch widgets: ' . $e->getMessage(), 500);
}
