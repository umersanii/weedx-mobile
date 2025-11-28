<?php
/**
 * Assistant History Endpoint
 * GET /api/assistant/history
 */

require_once __DIR__ . '/../../config/database.php';
require_once __DIR__ . '/../../utils/response.php';
require_once __DIR__ . '/../../utils/auth.php';
require_once __DIR__ . '/../../utils/logger.php';

Logger::logRequest('/api/assistant/history', 'GET');

$tokenData = Auth::validateToken();
Logger::logAuth('/api/assistant/history', $tokenData['userId'] ?? null, true);
$database = new Database();
$db = $database->getConnection();

try {
    $userId = $tokenData['userId'];
    $limit = $_GET['limit'] ?? 50;
    
    $query = "SELECT * FROM chat_history 
              WHERE user_id = :user_id 
              ORDER BY created_at DESC 
              LIMIT :limit";
    
    $stmt = $db->prepare($query);
    $stmt->bindParam(':user_id', $userId);
    $stmt->bindParam(':limit', $limit, PDO::PARAM_INT);
    $stmt->execute();
    
    $history = $stmt->fetchAll();
    
    $response = array_map(function($msg) {
        return [
            'id' => (int)$msg['id'],
            'message' => $msg['message'],
            'is_user' => (bool)$msg['is_user'],
            'timestamp' => $msg['created_at']
        ];
    }, array_reverse($history)); // Reverse to show oldest first
    
    Logger::logSuccess('/api/assistant/history', 'Fetched ' . count($response) . ' chat messages');
    Response::success($response);
} catch (Exception $e) {
    Logger::logError('/api/assistant/history', $e->getMessage(), 500);
    Response::error('Failed to fetch chat history: ' . $e->getMessage(), 500);
}
