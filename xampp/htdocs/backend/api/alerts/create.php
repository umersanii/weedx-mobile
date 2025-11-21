<?php
/**
 * Create Alert Endpoint
 * POST /api/alerts/create
 * Use this endpoint to test real-time alert notifications
 */

require_once __DIR__ . '/../../config/database.php';
require_once __DIR__ . '/../../utils/response.php';

// Allow CORS
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    Response::error('Method not allowed', 405);
}

// Connect to database
$database = new Database();
$db = $database->getConnection();

try {
    // Get JSON input
    $data = json_decode(file_get_contents('php://input'), true);
    
    // Set defaults if not provided
    $type = $data['type'] ?? 'test';
    $severity = $data['severity'] ?? 'info';
    $message = $data['message'] ?? 'Test alert created at ' . date('Y-m-d H:i:s');
    
    // Validate severity
    $valid_severities = ['info', 'warning', 'critical'];
    if (!in_array($severity, $valid_severities)) {
        Response::error('Invalid severity. Must be: info, warning, or critical', 400);
    }
    
    // Insert alert
    $query = "INSERT INTO alerts (type, severity, message, is_read) 
              VALUES (:type, :severity, :message, FALSE)";
    $stmt = $db->prepare($query);
    $stmt->bindParam(':type', $type);
    $stmt->bindParam(':severity', $severity);
    $stmt->bindParam(':message', $message);
    $stmt->execute();
    
    $alertId = $db->lastInsertId();
    
    Response::success([
        'id' => (int)$alertId,
        'type' => $type,
        'severity' => $severity,
        'message' => $message,
        'created_at' => date('Y-m-d H:i:s')
    ], 'Alert created successfully');
    
} catch (Exception $e) {
    Response::error('Failed to create alert: ' . $e->getMessage(), 500);
}
