<?php
/**
 * Monitoring Overview Endpoint
 * GET /api/monitoring
 */

require_once __DIR__ . '/../../config/database.php';
require_once __DIR__ . '/../../utils/response.php';
require_once __DIR__ . '/../../utils/auth.php';
require_once __DIR__ . '/../../utils/logger.php';

Logger::logRequest('/api/monitoring', 'GET');

$tokenData = Auth::validateToken();
Logger::logAuth('/api/monitoring', $tokenData['userId'] ?? null, true);
$database = new Database();
$db = $database->getConnection();

try {
    // Get latest metrics
    $metricsQuery = "SELECT * FROM robot_status ORDER BY updated_at DESC LIMIT 1";
    $metrics = $db->query($metricsQuery)->fetch();
    
    // Get activity timeline (last 10 activities)
    $activityQuery = "SELECT * FROM robot_activity_log ORDER BY timestamp DESC LIMIT 10";
    $activities = $db->query($activityQuery)->fetchAll();
    
    $response = [
        'metrics' => [
            'battery' => (int)($metrics['battery_level'] ?? 0),
            'herbicide_level' => (float)($metrics['herbicide_level'] ?? 0),
            'coverage' => (float)($metrics['area_covered_today'] ?? 0),
            'efficiency' => (float)($metrics['efficiency'] ?? 0),
            'status' => $metrics['status'] ?? 'offline',
            'speed' => (float)($metrics['speed'] ?? 0),
            'heading' => (float)($metrics['heading'] ?? 0),
            'activity' => $metrics['activity'] ?? null
        ],
        'activity_timeline' => array_map(function($activity) {
            return [
                'id' => (int)$activity['id'],
                'action' => $activity['action'],
                'description' => $activity['description'],
                'timestamp' => $activity['timestamp']
            ];
        }, $activities),
        'location' => [
            'latitude' => (float)($metrics['latitude'] ?? 0),
            'longitude' => (float)($metrics['longitude'] ?? 0)
        ]
    ];
    
    Logger::logSuccess('/api/monitoring', 'Battery: ' . $response['metrics']['battery'] . '%, Coverage: ' . $response['metrics']['coverage']);
    Response::success($response);
} catch (Exception $e) {
    Logger::logError('/api/monitoring', $e->getMessage(), 500);
    Response::error('Failed to fetch monitoring data: ' . $e->getMessage(), 500);
}
