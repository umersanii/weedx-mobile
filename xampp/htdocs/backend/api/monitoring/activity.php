<?php
/**
 * Monitoring Activity Timeline Endpoint
 * GET /api/monitoring/activity
 */

require_once __DIR__ . '/../../config/database.php';
require_once __DIR__ . '/../../utils/response.php';
require_once __DIR__ . '/../../utils/auth.php';

$tokenData = Auth::validateToken();
$database = new Database();
$db = $database->getConnection();

try {
    $limit = $_GET['limit'] ?? 20;
    
    $query = "SELECT * FROM robot_activity_log ORDER BY timestamp DESC LIMIT :limit";
    $stmt = $db->prepare($query);
    $stmt->bindParam(':limit', $limit, PDO::PARAM_INT);
    $stmt->execute();
    
    $activities = $stmt->fetchAll();
    
    $response = array_map(function($activity) {
        return [
            'id' => (int)$activity['id'],
            'action' => $activity['action'],
            'description' => $activity['description'],
            'status' => $activity['status'] ?? 'completed',
            'timestamp' => $activity['timestamp']
        ];
    }, $activities);
    
    Response::success($response);
} catch (Exception $e) {
    Response::error('Failed to fetch activity: ' . $e->getMessage(), 500);
}
