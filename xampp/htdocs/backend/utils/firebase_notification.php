<?php
/**
 * Firebase Cloud Messaging (FCM) Notification Service
 * 
 * Handles sending push notifications to Android devices via Firebase.
 * Requires Firebase Admin SDK and service account credentials.
 */

require_once __DIR__ . '/../vendor/autoload.php';
require_once __DIR__ . '/../config/database.php';

use Kreait\Firebase\Factory;
use Kreait\Firebase\Messaging\CloudMessage;
use Kreait\Firebase\Messaging\Notification;

class FirebaseNotificationService {
    private $messaging;
    private $db;
    
    public function __construct() {
        try {
            // Initialize Firebase with service account credentials
            $serviceAccountPath = __DIR__ . '/../config/firebase-service-account.json';
            
            if (!file_exists($serviceAccountPath)) {
                error_log("Firebase service account file not found at: {$serviceAccountPath}");
                throw new Exception("Firebase service account credentials not configured");
            }
            
            $factory = (new Factory)->withServiceAccount($serviceAccountPath);
            $this->messaging = $factory->createMessaging();
            
            // Initialize database connection
            $database = new Database();
            $this->db = $database->getConnection();
            
        } catch (Exception $e) {
            error_log("Firebase initialization error: " . $e->getMessage());
            throw $e;
        }
    }
    
    /**
     * Send notification to a specific user
     * 
     * @param int $userId User ID
     * @param string $title Notification title
     * @param string $body Notification message
     * @param array $data Optional data payload
     * @return bool Success status
     */
    public function sendToUser($userId, $title, $body, $data = []) {
        try {
            // Get user's FCM token(s) from database
            $tokens = $this->getUserTokens($userId);
            
            if (empty($tokens)) {
                error_log("No FCM tokens found for user {$userId}");
                return false;
            }
            
            // Send to all user's devices
            $successCount = 0;
            foreach ($tokens as $token) {
                if ($this->sendToToken($token, $title, $body, $data)) {
                    $successCount++;
                }
            }
            
            error_log("Sent notification to {$successCount} of " . count($tokens) . " devices for user {$userId}");
            return $successCount > 0;
            
        } catch (Exception $e) {
            error_log("Error sending notification to user {$userId}: " . $e->getMessage());
            return false;
        }
    }
    
    /**
     * Send notification to a specific device token
     * 
     * @param string $token FCM device token
     * @param string $title Notification title
     * @param string $body Notification message
     * @param array $data Optional data payload
     * @return bool Success status
     */
    public function sendToToken($token, $title, $body, $data = []) {
        try {
            $notification = Notification::create($title, $body);
            
            $message = CloudMessage::withTarget('token', $token)
                ->withNotification($notification)
                ->withData($data);
            
            $this->messaging->send($message);
            
            error_log("Notification sent successfully to token: " . substr($token, 0, 20) . "...");
            return true;
            
        } catch (Exception $e) {
            error_log("Error sending to token: " . $e->getMessage());
            
            // If token is invalid, remove it from database
            if (strpos($e->getMessage(), 'not-found') !== false || 
                strpos($e->getMessage(), 'invalid-registration-token') !== false) {
                $this->removeToken($token);
            }
            
            return false;
        }
    }
    
    /**
     * Send alert notification based on severity
     * 
     * @param int $userId User ID
     * @param string $type Alert type (battery, fault, maintenance, detection)
     * @param string $severity Alert severity (info, warning, critical)
     * @param string $message Alert message
     * @return bool Success status
     */
    public function sendAlert($userId, $type, $severity, $message) {
        // Set notification title based on severity
        $titleMap = [
            'critical' => '🚨 Critical Alert',
            'warning' => '⚠️ Warning',
            'info' => 'ℹ️ Notification'
        ];
        
        $title = isset($titleMap[$severity]) ? $titleMap[$severity] : 'WeedX Alert';
        
        // Add alert type context
        $typeMap = [
            'battery' => 'Battery Alert',
            'fault' => 'System Fault',
            'maintenance' => 'Maintenance Required',
            'detection' => 'Weed Detection'
        ];
        
        if (isset($typeMap[$type])) {
            $title = $typeMap[$type] . ' - ' . $title;
        }
        
        // Prepare data payload
        $data = [
            'type' => $type,
            'severity' => $severity,
            'timestamp' => date('Y-m-d H:i:s')
        ];
        
        return $this->sendToUser($userId, $title, $message, $data);
    }
    
    /**
     * Get all FCM tokens for a user
     * 
     * @param int $userId User ID
     * @return array Array of FCM tokens
     */
    private function getUserTokens($userId) {
        try {
            $query = "SELECT token FROM fcm_tokens 
                     WHERE user_id = :user_id AND active = 1
                     ORDER BY updated_at DESC";
            
            $stmt = $this->db->prepare($query);
            $stmt->bindParam(':user_id', $userId, PDO::PARAM_INT);
            $stmt->execute();
            
            $tokens = [];
            while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
                $tokens[] = $row['token'];
            }
            
            return $tokens;
            
        } catch (Exception $e) {
            error_log("Error fetching user tokens: " . $e->getMessage());
            return [];
        }
    }
    
    /**
     * Remove invalid token from database
     * 
     * @param string $token FCM token to remove
     */
    private function removeToken($token) {
        try {
            $query = "DELETE FROM fcm_tokens WHERE token = :token";
            $stmt = $this->db->prepare($query);
            $stmt->bindParam(':token', $token);
            $stmt->execute();
            
            error_log("Removed invalid token from database");
            
        } catch (Exception $e) {
            error_log("Error removing token: " . $e->getMessage());
        }
    }
    
    /**
     * Register or update FCM token for a user
     * 
     * @param int $userId User ID
     * @param string $token FCM device token
     * @param string $deviceInfo Optional device information
     * @return bool Success status
     */
    public function registerToken($userId, $token, $deviceInfo = null) {
        try {
            // Check if token already exists
            $checkQuery = "SELECT id FROM fcm_tokens WHERE token = :token";
            $checkStmt = $this->db->prepare($checkQuery);
            $checkStmt->bindParam(':token', $token);
            $checkStmt->execute();
            
            if ($checkStmt->rowCount() > 0) {
                // Update existing token
                $query = "UPDATE fcm_tokens SET 
                         user_id = :user_id,
                         device_info = :device_info,
                         active = 1,
                         updated_at = NOW()
                         WHERE token = :token";
            } else {
                // Insert new token
                $query = "INSERT INTO fcm_tokens 
                         (user_id, token, device_info, active, created_at, updated_at) 
                         VALUES (:user_id, :token, :device_info, 1, NOW(), NOW())";
            }
            
            $stmt = $this->db->prepare($query);
            $stmt->bindParam(':user_id', $userId, PDO::PARAM_INT);
            $stmt->bindParam(':token', $token);
            $stmt->bindParam(':device_info', $deviceInfo);
            $stmt->execute();
            
            return true;
            
        } catch (Exception $e) {
            error_log("Error registering token: " . $e->getMessage());
            return false;
        }
    }
    
    /**
     * Deactivate FCM token (e.g., on logout)
     * 
     * @param string $token FCM device token
     * @return bool Success status
     */
    public function deactivateToken($token) {
        try {
            $query = "UPDATE fcm_tokens SET active = 0, updated_at = NOW() WHERE token = :token";
            $stmt = $this->db->prepare($query);
            $stmt->bindParam(':token', $token);
            $stmt->execute();
            
            return true;
            
        } catch (Exception $e) {
            error_log("Error deactivating token: " . $e->getMessage());
            return false;
        }
    }
}
