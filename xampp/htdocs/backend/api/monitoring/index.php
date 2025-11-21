<?php
/**
 * Monitoring Overview Endpoint
 * GET /api/monitoring
 */

require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../utils/response.php';
require_once __DIR__ . '/../utils/auth.php';

$tokenData = Auth::validateToken();
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
            'herbicide_level' => (int)($metrics['herbicide_level'] ?? 0),
            'coverage' => (float)($metrics['area_covered_today'] ?? 0),
            'efficiency' => (float)($metrics['efficiency'] ?? 0)
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
    
    Response::success($response);
} catch (Exception $e) {
    Response::error('Failed to fetch monitoring data: ' . $e->getMessage(), 500);
}
