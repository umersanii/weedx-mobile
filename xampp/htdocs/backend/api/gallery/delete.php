<?php
/**
 * Gallery Delete Image Endpoint
 * DELETE /api/gallery/:id
 */

require_once __DIR__ . '/../../config/database.php';
require_once __DIR__ . '/../../utils/response.php';
require_once __DIR__ . '/../../utils/auth.php';
require_once __DIR__ . '/../../utils/logger.php';

$imageId = $_GET['id'] ?? null;
Logger::logRequest('/api/gallery/' . $imageId, 'DELETE');

$tokenData = Auth::validateToken();
Logger::logAuth('/api/gallery/' . $imageId, $tokenData['userId'] ?? null, true);
$database = new Database();
$db = $database->getConnection();

if (!$imageId) {
    Logger::logError('/api/gallery/:id', 'Image ID required', 400);
    Response::error('Image ID required', 400);
}

try {
    // Get image path first (with user authorization)
    $selectQuery = "SELECT image_path FROM weed_detections WHERE id = :id AND user_id = :user_id LIMIT 1";
    $selectStmt = $db->prepare($selectQuery);
    $selectStmt->bindParam(':id', $imageId, PDO::PARAM_INT);
    $selectStmt->bindParam(':user_id', $tokenData['userId'], PDO::PARAM_INT);
    $selectStmt->execute();
    
    $image = $selectStmt->fetch();
    
    if (!$image) {
        Logger::logError('/api/gallery/' . $imageId, 'Image not found or unauthorized', 404);
        Response::error('Image not found or unauthorized', 404);
    }
    
    // Delete from database (with user authorization)
    $deleteQuery = "DELETE FROM weed_detections WHERE id = :id AND user_id = :user_id";
    $deleteStmt = $db->prepare($deleteQuery);
    $deleteStmt->bindParam(':id', $imageId, PDO::PARAM_INT);
    $deleteStmt->bindParam(':user_id', $tokenData['userId'], PDO::PARAM_INT);
    
    if ($deleteStmt->execute()) {
        // Delete physical file
        $filepath = __DIR__ . '/../../' . $image['image_path'];
        if (file_exists($filepath)) {
            unlink($filepath);
        }
        
        Logger::logSuccess('/api/gallery/' . $imageId, 'Image deleted');
        Response::success(null, 'Image deleted successfully');
    } else {
        Logger::logError('/api/gallery/' . $imageId, 'Failed to delete image', 500);
        Response::error('Failed to delete image', 500);
    }
} catch (Exception $e) {
    Logger::logError('/api/gallery/' . $imageId, $e->getMessage(), 500);
    Response::error('Failed to delete image: ' . $e->getMessage(), 500);
}
