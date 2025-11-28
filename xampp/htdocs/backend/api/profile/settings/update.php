<?php
/**
 * Settings Update Endpoint
 * PUT /api/profile/settings
 */

require_once __DIR__ . '/../../../config/database.php';
require_once __DIR__ . '/../../../utils/response.php';
require_once __DIR__ . '/../../../utils/auth.php';
require_once __DIR__ . '/../../../utils/logger.php';

Logger::logRequest('/api/profile/settings', 'PUT');

$tokenData = Auth::validateToken();
Logger::logAuth('/api/profile/settings', $tokenData['userId'] ?? null, true);
$database = new Database();
$db = $database->getConnection();

$data = json_decode(file_get_contents("php://input"), true);

try {
    $userId = $tokenData['userId'];
    
    // Check if settings exist
    $checkQuery = "SELECT id FROM user_settings WHERE user_id = :user_id";
    $checkStmt = $db->prepare($checkQuery);
    $checkStmt->bindParam(':user_id', $userId);
    $checkStmt->execute();
    $existing = $checkStmt->fetch();
    
    $notificationsEnabled = isset($data['notifications_enabled']) ? (int)$data['notifications_enabled'] : null;
    $emailAlerts = isset($data['email_alerts']) ? (int)$data['email_alerts'] : null;
    $language = $data['language'] ?? null;
    $theme = $data['theme'] ?? null;
    
    if ($existing) {
        // Update existing settings
        $updates = [];
        $params = [];
        
        if ($notificationsEnabled !== null) {
            $updates[] = " notifications_enabled = :notifications";
            $params[':notifications'] = $notificationsEnabled;
        }
        
        if ($emailAlerts !== null) {
            $updates[] = " email_alerts = :email_alerts";
            $params[':email_alerts'] = $emailAlerts;
        }
        
        if ($language) {
            $updates[] = " language = :language";
            $params[':language'] = $language;
        }
        
        if ($theme) {
            $updates[] = " theme = :theme";
            $params[':theme'] = $theme;
        }
        
        if (empty($updates)) {
            Response::error('No fields to update', 400);
        }
        
        $query = "UPDATE user_settings SET" . implode(',', $updates) . " WHERE user_id = :user_id";
        $params[':user_id'] = $userId;
        
        $stmt = $db->prepare($query);
        
        foreach ($params as $key => $value) {
            $stmt->bindValue($key, $value);
        }
        
        if ($stmt->execute()) {
            Logger::logSuccess('/api/profile/settings', 'Settings updated for user ID: ' . $userId);
            Response::success(null, 'Settings updated successfully');
        } else {
            Logger::logError('/api/profile/settings', 'Failed to update settings', 500);
            Response::error('Failed to update settings', 500);
        }
    } else {
        // Create new settings
        $query = "INSERT INTO user_settings 
                  (user_id, notifications_enabled, email_alerts, language, theme, created_at) 
                  VALUES (:user_id, :notifications, :email_alerts, :language, :theme, NOW())";
        
        $stmt = $db->prepare($query);
        $stmt->bindParam(':user_id', $userId);
        $stmt->bindParam(':notifications', $notificationsEnabled);
        $stmt->bindParam(':email_alerts', $emailAlerts);
        $stmt->bindParam(':language', $language);
        $stmt->bindParam(':theme', $theme);
        
        if ($stmt->execute()) {
            Logger::logSuccess('/api/profile/settings', 'Settings created for user ID: ' . $userId);
            Response::success(null, 'Settings created successfully', 201);
        } else {
            Logger::logError('/api/profile/settings', 'Failed to create settings', 500);
            Response::error('Failed to create settings', 500);
        }
    }
} catch (Exception $e) {
    Logger::logError('/api/profile/settings', $e->getMessage(), 500);
    Response::error('Failed to update settings: ' . $e->getMessage(), 500);
}
