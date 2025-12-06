<?php
/**
 * Get All Alerts for User
 * GET /api/alerts/all
 * Returns all alerts associated with the authenticated user's account
 */

require_once __DIR__ . '/../../config/database.php';
require_once __DIR__ . '/../../utils/response.php';
require_once __DIR__ . '/../../utils/auth.php';
require_once __DIR__ . '/../../utils/logger.php';

Logger::logRequest('/api/alerts/all', 'GET');

// Validate token
$tokenData = Auth::validateToken();
Logger::logAuth('/api/alerts/all', $tokenData['userId'] ?? null, true);

// Connect to database
$database = new Database();
$db = $database->getConnection();

try {
    $page = isset($_GET['page']) ? (int)$_GET['page'] : 1;
    $limit = isset($_GET['limit']) ? (int)$_GET['limit'] : 50;
    $offset = ($page - 1) * $limit;
    
    // Get total count for pagination
    $countQuery = "SELECT COUNT(*) as total FROM alerts";
    $countStmt = $db->query($countQuery);
    $totalCount = $countStmt->fetch()['total'];
    
    // Get all alerts (sorted by newest first)
    $query = "SELECT * FROM alerts 
              ORDER BY created_at DESC 
              LIMIT :limit OFFSET :offset";
    $stmt = $db->prepare($query);
    $stmt->bindParam(':limit', $limit, PDO::PARAM_INT);
    $stmt->bindParam(':offset', $offset, PDO::PARAM_INT);
    $stmt->execute();
    
    $alerts = $stmt->fetchAll();
    
    $response = [
        'alerts' => array_map(function($alert) {
            return [
                'id' => (int)$alert['id'],
                'type' => $alert['type'],
                'severity' => $alert['severity'],
                'message' => $alert['message'],
                'is_read' => (bool)$alert['is_read'],
                'timestamp' => $alert['created_at']
            ];
        }, $alerts),
        'pagination' => [
            'total' => (int)$totalCount,
            'page' => $page,
            'limit' => $limit,
            'total_pages' => ceil($totalCount / $limit)
        ]
    ];
    
    Logger::logSuccess('/api/alerts/all', 'Fetched ' . count($alerts) . ' alerts');
    Response::success($response);
    
} catch (Exception $e) {
    Logger::logError('/api/alerts/all', $e->getMessage(), 500);
    Response::error('Failed to fetch alerts: ' . $e->getMessage(), 500);
}
