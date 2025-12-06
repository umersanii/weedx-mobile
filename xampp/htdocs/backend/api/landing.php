<?php
/**
 * Landing Page Endpoint
 * GET /api/landing
 * Returns: robot status, today's summary, recent alerts
 */

require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../utils/response.php';
require_once __DIR__ . '/../utils/auth.php';
require_once __DIR__ . '/../utils/logger.php';

Logger::logRequest('/api/landing', 'GET');

// Validate token
$tokenData = Auth::validateToken();
Logger::logAuth('/api/landing', $tokenData['userId'] ?? null, true);

// Connect to database
$database = new Database();
$db = $database->getConnection();

try {
    // Get robot status
    $robotQuery = "SELECT * FROM robot_status ORDER BY updated_at DESC LIMIT 1";
    $robotStmt = $db->query($robotQuery);
    $robotStatus = $robotStmt->fetch();
    
    // Get today's summary (weeds detected)
    $summaryQuery = "
        SELECT 
            COUNT(*) as total_weeds,
            SUM(CASE WHEN DATE(detected_at) = CURDATE() THEN 1 ELSE 0 END) as today_weeds,
            ROUND(AVG(CASE WHEN DATE(detected_at) = CURDATE() THEN confidence ELSE NULL END), 2) as avg_confidence
        FROM weed_detections
        WHERE user_id = :user_id
    ";
    $summaryStmt = $db->prepare($summaryQuery);
    $summaryStmt->bindParam(':user_id', $tokenData['userId'], PDO::PARAM_INT);
    $summaryStmt->execute();
    $summary = $summaryStmt->fetch();
    
    // Get area covered and herbicide used today from robot_sessions
    $sessionQuery = "
        SELECT 
            COALESCE(SUM(area_covered), 0) as area_covered,
            COALESCE(SUM(herbicide_used), 0) as herbicide_used,
            COALESCE(SUM(TIMESTAMPDIFF(MINUTE, start_time, IFNULL(end_time, NOW()))), 0) as session_duration
        FROM robot_sessions 
        WHERE DATE(start_time) = CURDATE()
    ";
    $sessionStmt = $db->query($sessionQuery);
    $sessionData = $sessionStmt->fetch();
    
    // Get recent alerts (last 5)
    $alertsQuery = "SELECT * FROM alerts ORDER BY created_at DESC LIMIT 5";
    $alertsStmt = $db->query($alertsQuery);
    $alerts = $alertsStmt->fetchAll();
    
    $response = [
        'robot_status' => [
            'status' => $robotStatus['status'] ?? 'offline',
            'battery' => (int)($robotStatus['battery_level'] ?? 0),
            'latitude' => (float)($robotStatus['latitude'] ?? 0),
            'longitude' => (float)($robotStatus['longitude'] ?? 0),
            'speed' => (float)($robotStatus['speed'] ?? 0),
            'last_updated' => $robotStatus['updated_at'] ?? null
        ],
        'todays_summary' => [
            'weeds_detected' => (int)($summary['today_weeds'] ?? 0),
            'area_covered' => (float)($sessionData['area_covered'] ?? 0),
            'herbicide_used' => (float)($sessionData['herbicide_used'] ?? 0),
            'session_duration' => (int)($sessionData['session_duration'] ?? 0),
            'avg_confidence' => (float)($summary['avg_confidence'] ?? 0),
            'total_weeds_alltime' => (int)($summary['total_weeds'] ?? 0)
        ],
        'recent_alerts' => array_map(function($alert) {
            return [
                'id' => (int)$alert['id'],
                'type' => $alert['type'],
                'severity' => $alert['severity'],
                'message' => $alert['message'],
                'timestamp' => $alert['created_at'],
                'is_read' => (bool)($alert['is_read'] ?? false)
            ];
        }, $alerts)
    ];
    
    Logger::logSuccess('/api/landing', 'Landing data fetched');
    Response::success($response);
    
} catch (Exception $e) {
    Logger::logError('/api/landing', $e->getMessage(), 500);
    Response::error('Failed to fetch landing data: ' . $e->getMessage(), 500);
}
