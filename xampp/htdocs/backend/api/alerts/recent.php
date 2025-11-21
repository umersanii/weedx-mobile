<?php
/**
 * Recent Alerts Endpoint
 * GET /api/alerts/recent
 */

require_once __DIR__ . '/../../config/database.php';
require_once __DIR__ . '/../../utils/response.php';
require_once __DIR__ . '/../../utils/auth.php';

// Validate token
$tokenData = Auth::validateToken();

// Connect to database
$database = new Database();
$db = $database->getConnection();

try {
    $limit = $_GET['limit'] ?? 10;
    
    $query = "SELECT * FROM alerts ORDER BY created_at DESC LIMIT :limit";
    $stmt = $db->prepare($query);
    $stmt->bindParam(':limit', $limit, PDO::PARAM_INT);
    $stmt->execute();
    
    $alerts = $stmt->fetchAll();
    
    $response = array_map(function($alert) {
        return [
            'id' => (int)$alert['id'],
            'type' => $alert['type'],
            'severity' => $alert['severity'],
            'message' => $alert['message'],
            'read' => (bool)$alert['is_read'],
            'timestamp' => $alert['created_at']
        ];
    }, $alerts);
    
    Response::success($response);
    
} catch (Exception $e) {
    Response::error('Failed to fetch alerts: ' . $e->getMessage(), 500);
}
