<?php
/**
 * Farm Info Get Endpoint
 * GET /api/profile/farm
 */

require_once __DIR__ . '/../../../config/database.php';
require_once __DIR__ . '/../../../utils/response.php';
require_once __DIR__ . '/../../../utils/auth.php';

$tokenData = Auth::validateToken();
$database = new Database();
$db = $database->getConnection();

try {
    $userId = $tokenData['userId'];
    
    $query = "SELECT * FROM farms WHERE user_id = :user_id LIMIT 1";
    $stmt = $db->prepare($query);
    $stmt->bindParam(':user_id', $userId);
    $stmt->execute();
    
    $farm = $stmt->fetch();
    
    if (!$farm) {
        Response::error('No farm information found', 404);
    }
    
    $response = [
        'id' => (int)$farm['id'],
        'name' => $farm['name'],
        'location' => $farm['location'],
        'size' => (float)$farm['size'],
        'crop_types' => $farm['crop_types'],
        'created_at' => $farm['created_at']
    ];
    
    Response::success($response);
} catch (Exception $e) {
    Response::error('Failed to fetch farm info: ' . $e->getMessage(), 500);
}
