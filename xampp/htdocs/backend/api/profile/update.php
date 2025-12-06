<?php
/**
 * Profile Update Endpoint
 * PUT /api/profile
 */

require_once __DIR__ . '/../../config/database.php';
require_once __DIR__ . '/../../utils/response.php';
require_once __DIR__ . '/../../utils/auth.php';
require_once __DIR__ . '/../../utils/image_helper.php';
require_once __DIR__ . '/../../utils/logger.php';

Logger::logRequest('/api/profile', 'PUT');

$tokenData = Auth::validateToken();
Logger::logAuth('/api/profile', $tokenData['userId'] ?? null, true);
$database = new Database();
$db = $database->getConnection();

$data = json_decode(file_get_contents("php://input"), true);

try {
    $userId = $tokenData['userId'];
    
    $name = $data['name'] ?? null;
    $email = $data['email'] ?? null;
    $phone = $data['phone'] ?? null;
    
    $query = "UPDATE users SET";
    $updates = [];
    $params = [];
    
    if ($name) {
        $updates[] = " name = :name";
        $params[':name'] = $name;
    }
    
    if ($email) {
        $updates[] = " email = :email";
        $params[':email'] = $email;
    }
    
    if ($phone) {
        $updates[] = " phone = :phone";
        $params[':phone'] = $phone;
    }
    
    if (empty($updates)) {
        Response::error('No fields to update', 400);
    }
    
    $query .= implode(',', $updates) . " WHERE id = :id";
    $params[':id'] = $userId;
    
    $stmt = $db->prepare($query);
    
    foreach ($params as $key => $value) {
        $stmt->bindValue($key, $value);
    }
    
    if ($stmt->execute()) {
        // Fetch updated user data
        $userQuery = "SELECT id, name, email, avatar, phone, created_at FROM users WHERE id = :id";
        $userStmt = $db->prepare($userQuery);
        $userStmt->bindParam(':id', $userId);
        $userStmt->execute();
        $user = $userStmt->fetch();
        
        // Get full avatar URL if avatar exists
        $avatarUrl = $user['avatar'] ? ImageHelper::getFullUrl($user['avatar']) : null;
        
        $response = [
            'id' => (int)$user['id'],
            'name' => $user['name'],
            'email' => $user['email'],
            'avatar' => $avatarUrl,
            'phone' => $user['phone'] ?? null,
            'joined' => $user['created_at']
        ];
        
        Logger::logSuccess('/api/profile', 'Profile updated for user ID: ' . $userId);
        Response::success($response, 'Profile updated successfully');
    } else {
        Logger::logError('/api/profile', 'Failed to update profile', 500);
        Response::error('Failed to update profile', 500);
    }
} catch (Exception $e) {
    Logger::logError('/api/profile', $e->getMessage(), 500);
    Response::error('Failed to update profile: ' . $e->getMessage(), 500);
}
