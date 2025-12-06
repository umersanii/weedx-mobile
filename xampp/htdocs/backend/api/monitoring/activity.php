<?php
/**
 * Monitoring Activity Timeline Endpoint
 * GET /api/monitoring/activity
 */

require_once __DIR__ . '/../../config/database.php';
require_once __DIR__ . '/../../utils/response.php';
require_once __DIR__ . '/../../utils/auth.php';
require_once __DIR__ . '/../../utils/logger.php';

Logger::logRequest('/api/monitoring/activity', 'GET');

$tokenData = Auth::validateToken();
Logger::logAuth('/api/monitoring/activity', $tokenData['userId'] ?? null, true);
$database = new Database();
$db = $database->getConnection();

try {
    $limit = $_GET['limit'] ?? 20;
    
    $query = "SELECT * FROM robot_activity_log WHERE user_id = :user_id ORDER BY timestamp DESC LIMIT :limit";
    $stmt = $db->prepare($query);
    $stmt->bindParam(':user_id', $tokenData['userId'], PDO::PARAM_INT);
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
    
    Logger::logSuccess('/api/monitoring/activity', 'Fetched ' . count($response) . ' activities');
    Response::success($response);
} catch (Exception $e) {
    Logger::logError('/api/monitoring/activity', $e->getMessage(), 500);
    Response::error('Failed to fetch activity: ' . $e->getMessage(), 500);
}
