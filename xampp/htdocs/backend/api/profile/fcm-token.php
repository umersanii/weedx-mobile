<?php
/**
 * FCM Token Management API
 * Handles registration and management of Firebase Cloud Messaging tokens
 */

header('Content-Type: application/json');
require_once __DIR__ . '/../../config/database.php';
require_once __DIR__ . '/../../utils/response.php';
require_once __DIR__ . '/../../utils/auth.php';
require_once __DIR__ . '/../../utils/firebase_notification.php';

// Validate authentication
$tokenData = Auth::validateToken();
if (!$tokenData) {
    Response::error('Unauthorized', 401);
}

$userId = $tokenData['userId'];
$method = $_SERVER['REQUEST_METHOD'];

try {
    $firebaseService = new FirebaseNotificationService();
    
    switch ($method) {
        case 'POST':
            // Register new FCM token
            $data = json_decode(file_get_contents('php://input'), true);
            
            if (!isset($data['token']) || empty($data['token'])) {
                Response::error('FCM token is required', 400);
            }
            
            $token = $data['token'];
            $deviceInfo = isset($data['device_info']) ? $data['device_info'] : null;
            
            if ($firebaseService->registerToken($userId, $token, $deviceInfo)) {
                Response::success([
                    'message' => 'FCM token registered successfully'
                ]);
            } else {
                Response::error('Failed to register FCM token', 500);
            }
            break;
            
        case 'DELETE':
            // Deactivate FCM token (on logout)
            $data = json_decode(file_get_contents('php://input'), true);
            
            if (!isset($data['token']) || empty($data['token'])) {
                Response::error('FCM token is required', 400);
            }
            
            $token = $data['token'];
            
            if ($firebaseService->deactivateToken($token)) {
                Response::success([
                    'message' => 'FCM token deactivated successfully'
                ]);
            } else {
                Response::error('Failed to deactivate FCM token', 500);
            }
            break;
            
        default:
            Response::error('Method not allowed', 405);
    }
    
} catch (Exception $e) {
    Response::error($e->getMessage(), 500);
}
