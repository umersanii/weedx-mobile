<?php
/**
 * Profile Get Endpoint
 * GET /api/profile
 */

require_once __DIR__ . '/../../config/database.php';
require_once __DIR__ . '/../../utils/response.php';
require_once __DIR__ . '/../../utils/auth.php';
require_once __DIR__ . '/../../utils/image_helper.php';
require_once __DIR__ . '/../../utils/logger.php';

Logger::logRequest('/api/profile', 'GET');

$tokenData = Auth::validateToken();
Logger::logAuth('/api/profile', $tokenData['userId'] ?? null, true);
$database = new Database();
$db = $database->getConnection();

try {
    $userId = $tokenData['userId'];
    
    // Get user info
    $userQuery = "SELECT id, name, email, avatar, phone, created_at FROM users WHERE id = :id";
    $userStmt = $db->prepare($userQuery);
    $userStmt->bindParam(':id', $userId);
    $userStmt->execute();
    $user = $userStmt->fetch();
    
    // Get farm info
    $farmQuery = "SELECT * FROM farms WHERE user_id = :user_id LIMIT 1";
    $farmStmt = $db->prepare($farmQuery);
    $farmStmt->bindParam(':user_id', $userId);
    $farmStmt->execute();
    $farm = $farmStmt->fetch();
    
    // Get settings
    $settingsQuery = "SELECT * FROM user_settings WHERE user_id = :user_id LIMIT 1";
    $settingsStmt = $db->prepare($settingsQuery);
    $settingsStmt->bindParam(':user_id', $userId);
    $settingsStmt->execute();
    $settings = $settingsStmt->fetch();
    
    // Get full avatar URL if avatar exists
    $avatarUrl = $user['avatar'] ? ImageHelper::getFullUrl($user['avatar']) : null;
    
    $response = [
        'user' => [
            'id' => (int)$user['id'],
            'name' => $user['name'],
            'email' => $user['email'],
            'avatar' => $avatarUrl,
            'phone' => $user['phone'] ?? null,
            'joined' => $user['created_at']
        ],
        'farm' => $farm ? [
            'id' => (int)$farm['id'],
            'name' => $farm['name'],
            'location' => $farm['location'],
            'size' => (float)$farm['size'],
            'crop_types' => $farm['crop_types']
        ] : null,
        'settings' => $settings ? [
            'notifications_enabled' => (bool)$settings['notifications_enabled'],
            'email_alerts' => (bool)$settings['email_alerts'],
            'language' => $settings['language'] ?? 'en',
            'theme' => $settings['theme'] ?? 'light'
        ] : null
    ];
    
    Logger::logSuccess('/api/profile', 'Profile fetched for user: ' . $user['email']);
    Response::success($response);
} catch (Exception $e) {
    Logger::logError('/api/profile', $e->getMessage(), 500);
    Response::error('Failed to fetch profile: ' . $e->getMessage(), 500);
}
