<?php
/**
 * Gallery Delete Image Endpoint
 * DELETE /api/gallery/:id
 */

require_once __DIR__ . '/../../config/database.php';
require_once __DIR__ . '/../../utils/response.php';
require_once __DIR__ . '/../../utils/auth.php';

$tokenData = Auth::validateToken();
$database = new Database();
$db = $database->getConnection();

$imageId = $_GET['id'] ?? null;

if (!$imageId) {
    Response::error('Image ID required', 400);
}

try {
    // Get image path first
    $selectQuery = "SELECT image_path FROM weed_detections WHERE id = :id LIMIT 1";
    $selectStmt = $db->prepare($selectQuery);
    $selectStmt->bindParam(':id', $imageId, PDO::PARAM_INT);
    $selectStmt->execute();
    
    $image = $selectStmt->fetch();
    
    if (!$image) {
        Response::error('Image not found', 404);
    }
    
    // Delete from database
    $deleteQuery = "DELETE FROM weed_detections WHERE id = :id";
    $deleteStmt = $db->prepare($deleteQuery);
    $deleteStmt->bindParam(':id', $imageId, PDO::PARAM_INT);
    
    if ($deleteStmt->execute()) {
        // Delete physical file
        $filepath = __DIR__ . '/../../' . $image['image_path'];
        if (file_exists($filepath)) {
            unlink($filepath);
        }
        
        Response::success(null, 'Image deleted successfully');
    } else {
        Response::error('Failed to delete image', 500);
    }
} catch (Exception $e) {
    Response::error('Failed to delete image: ' . $e->getMessage(), 500);
}
