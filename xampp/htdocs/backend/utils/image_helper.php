<?php
/**
 * Image Helper Utility
 * Handles image storage, URLs, and category-based organization
 */

class ImageHelper {
    
    // Image categories and their folder paths
    const CATEGORY_AVATAR = 'avatars';
    const CATEGORY_GALLERY = 'gallery';
    const CATEGORY_THUMBNAIL = 'thumbnails';
    const CATEGORY_REPORT = 'reports';
    
    private static $basePath = __DIR__ . '/../uploads/';
    
    /**
     * Get the full filesystem path for an image category
     */
    public static function getCategoryPath(string $category): string {
        return self::$basePath . $category . '/';
    }
    
    /**
     * Get the relative URL path for an image category
     */
    public static function getCategoryUrlPath(string $category): string {
        return '/uploads/' . $category . '/';
    }
    
    /**
     * Build full URL for an image
     */
    public static function getFullUrl(string $relativePath): string {
        $protocol = (!empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off') ? 'https' : 'http';
        $host = $_SERVER['HTTP_HOST'];
        
        // Get base directory (e.g., /weedx-backend)
        $scriptPath = $_SERVER['SCRIPT_NAME'];
        $baseDir = dirname($scriptPath);
        if (strpos($baseDir, '/api') !== false) {
            $baseDir = dirname(dirname($scriptPath));
        }
        
        // Handle already full URLs
        if (strpos($relativePath, 'http://') === 0 || strpos($relativePath, 'https://') === 0) {
            return $relativePath;
        }
        
        // Handle base64 images - return as-is (for backward compatibility)
        if (strpos($relativePath, 'data:image') === 0) {
            return $relativePath;
        }
        
        // Ensure path starts with /
        if (strpos($relativePath, '/') !== 0) {
            $relativePath = '/' . $relativePath;
        }
        
        return $protocol . '://' . $host . $baseDir . $relativePath;
    }
    
    /**
     * Generate a unique filename for a category
     */
    public static function generateFilename(string $category, string $extension, ?string $prefix = null): string {
        $prefix = $prefix ?? self::getCategoryPrefix($category);
        return $prefix . '_' . uniqid('', true) . '.' . strtolower($extension);
    }
    
    /**
     * Get default prefix for a category
     */
    private static function getCategoryPrefix(string $category): string {
        switch ($category) {
            case self::CATEGORY_AVATAR:
                return 'avatar';
            case self::CATEGORY_GALLERY:
                return 'weed';
            case self::CATEGORY_THUMBNAIL:
                return 'thumb';
            case self::CATEGORY_REPORT:
                return 'report';
            default:
                return 'file';
        }
    }
    
    /**
     * Validate and save an uploaded image
     */
    public static function saveUploadedImage(
        array $file,
        string $category,
        ?string $customFilename = null,
        int $maxSizeMb = 5
    ): array {
        // Validate file type
        $allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'];
        if (!in_array($file['type'], $allowedTypes)) {
            return ['success' => false, 'error' => 'Invalid file type. Only JPEG, PNG, GIF, and WebP allowed.'];
        }
        
        // Validate file size
        $maxBytes = $maxSizeMb * 1024 * 1024;
        if ($file['size'] > $maxBytes) {
            return ['success' => false, 'error' => "File too large. Maximum {$maxSizeMb}MB."];
        }
        
        // Get upload directory
        $uploadDir = self::getCategoryPath($category);
        if (!file_exists($uploadDir)) {
            mkdir($uploadDir, 0755, true);
        }
        
        // Generate filename
        $extension = pathinfo($file['name'], PATHINFO_EXTENSION);
        $filename = $customFilename ?? self::generateFilename($category, $extension);
        $filepath = $uploadDir . $filename;
        
        // Move uploaded file
        if (!move_uploaded_file($file['tmp_name'], $filepath)) {
            return ['success' => false, 'error' => 'Failed to save file.'];
        }
        
        // Return success with paths
        $relativePath = self::getCategoryUrlPath($category) . $filename;
        return [
            'success' => true,
            'filename' => $filename,
            'relative_path' => $relativePath,
            'full_url' => self::getFullUrl($relativePath),
            'filesystem_path' => $filepath
        ];
    }
    
    /**
     * Delete an image from the filesystem
     */
    public static function deleteImage(string $relativePath): bool {
        // Get filesystem path from relative path
        $basePath = __DIR__ . '/..';
        $filepath = $basePath . $relativePath;
        
        if (file_exists($filepath)) {
            return unlink($filepath);
        }
        return false;
    }
    
    /**
     * Check if a path is a valid image URL (not base64)
     */
    public static function isValidImagePath(string $path): bool {
        if (empty($path)) {
            return false;
        }
        // Base64 images are not "proper" file paths
        if (strpos($path, 'data:image') === 0) {
            return false;
        }
        return true;
    }
}
