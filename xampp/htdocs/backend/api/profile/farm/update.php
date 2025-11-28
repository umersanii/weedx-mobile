<?php
/**
 * Farm Info Update Endpoint
 * PUT /api/profile/farm
 */

require_once __DIR__ . '/../../../config/database.php';
require_once __DIR__ . '/../../../utils/response.php';
require_once __DIR__ . '/../../../utils/auth.php';
require_once __DIR__ . '/../../../utils/logger.php';

Logger::logRequest('/api/profile/farm', 'PUT');

$tokenData = Auth::validateToken();
Logger::logAuth('/api/profile/farm', $tokenData['userId'] ?? null, true);
$database = new Database();
$db = $database->getConnection();

$data = json_decode(file_get_contents("php://input"), true);

try {
    $userId = $tokenData['userId'];
    
    // Check if farm exists
    $checkQuery = "SELECT id FROM farms WHERE user_id = :user_id";
    $checkStmt = $db->prepare($checkQuery);
    $checkStmt->bindParam(':user_id', $userId);
    $checkStmt->execute();
    $existing = $checkStmt->fetch();
    
    $name = $data['name'] ?? null;
    $location = $data['location'] ?? null;
    $size = $data['size'] ?? null;
    $cropTypes = $data['crop_types'] ?? null;
    
    if ($existing) {
        // Update existing farm
        $query = "UPDATE farms SET";
        $updates = [];
        $params = [];
        
        if ($name) {
            $updates[] = " name = :name";
            $params[':name'] = $name;
        }
        
        if ($location) {
            $updates[] = " location = :location";
            $params[':location'] = $location;
        }
        
        if ($size) {
            $updates[] = " size = :size";
            $params[':size'] = $size;
        }
        
        if ($cropTypes) {
            $updates[] = " crop_types = :crop_types";
            $params[':crop_types'] = $cropTypes;
        }
        
        if (empty($updates)) {
            Response::error('No fields to update', 400);
        }
        
        $query .= implode(',', $updates) . " WHERE user_id = :user_id";
        $params[':user_id'] = $userId;
        
        $stmt = $db->prepare($query);
        
        foreach ($params as $key => $value) {
            $stmt->bindValue($key, $value);
        }
        
        if ($stmt->execute()) {
            Logger::logSuccess('/api/profile/farm', 'Farm info updated for user ID: ' . $userId);
            Response::success(null, 'Farm info updated successfully');
        } else {
            Logger::logError('/api/profile/farm', 'Failed to update farm info', 500);
            Response::error('Failed to update farm info', 500);
        }
    } else {
        // Create new farm
        Response::validateRequired($data, ['name', 'location', 'size']);
        
        $query = "INSERT INTO farms (user_id, name, location, size, crop_types, created_at) 
                  VALUES (:user_id, :name, :location, :size, :crop_types, NOW())";
        
        $stmt = $db->prepare($query);
        $stmt->bindParam(':user_id', $userId);
        $stmt->bindParam(':name', $name);
        $stmt->bindParam(':location', $location);
        $stmt->bindParam(':size', $size);
        $stmt->bindParam(':crop_types', $cropTypes);
        
        if ($stmt->execute()) {
            Logger::logSuccess('/api/profile/farm', 'Farm created for user ID: ' . $userId);
            Response::success(['id' => (int)$db->lastInsertId()], 'Farm created successfully', 201);
        } else {
            Logger::logError('/api/profile/farm', 'Failed to create farm', 500);
            Response::error('Failed to create farm', 500);
        }
    }
} catch (Exception $e) {
    Logger::logError('/api/profile/farm', $e->getMessage(), 500);
    Response::error('Failed to update farm: ' . $e->getMessage(), 500);
}
