<?php
/**
 * Settings Get Endpoint
 * GET /api/profile/settings
 */

require_once __DIR__ . '/../../../config/database.php';
require_once __DIR__ . '/../../../utils/response.php';
require_once __DIR__ . '/../../../utils/auth.php';
require_once __DIR__ . '/../../../utils/logger.php';

Logger::logRequest('/api/profile/settings', 'GET');

$tokenData = Auth::validateToken();
Logger::logAuth('/api/profile/settings', $tokenData['userId'] ?? null, true);
$database = new Database();
$db = $database->getConnection();

try {
    $userId = $tokenData['userId'];
    
    $query = "SELECT * FROM user_settings WHERE user_id = :user_id LIMIT 1";
    $stmt = $db->prepare($query);
    $stmt->bindParam(':user_id', $userId);
    $stmt->execute();
    
    $settings = $stmt->fetch();
    
    if (!$settings) {
        // Return defaults if no settings found
        Logger::logSuccess('/api/profile/settings', 'Returning default settings');
        Response::success([
            'notifications_enabled' => true,
            'email_alerts' => true,
            'language' => 'en',
            'theme' => 'light'
        ]);
    }
    
    $response = [
        'notifications_enabled' => (bool)$settings['notifications_enabled'],
        'email_alerts' => (bool)$settings['email_alerts'],
        'language' => $settings['language'],
        'theme' => $settings['theme']
    ];
    
    Logger::logSuccess('/api/profile/settings', 'Settings fetched');
    Response::success($response);
} catch (Exception $e) {
    Logger::logError('/api/profile/settings', $e->getMessage(), 500);
    Response::error('Failed to fetch settings: ' . $e->getMessage(), 500);
}
