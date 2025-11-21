<?php
/**
 * Authentication Helper
 * JWT token generation and validation
 */

class Auth {
    private static $secret_key = 'your-secret-key-change-this-in-production';
    private static $issuer = 'weedx-backend';
    
    /**
     * Generate JWT token
     */
    public static function generateToken($userId, $email) {
        $issuedAt = time();
        $expirationTime = $issuedAt + (60 * 60 * 24 * 30); // 30 days
        
        $payload = [
            'iss' => self::$issuer,
            'iat' => $issuedAt,
            'exp' => $expirationTime,
            'userId' => $userId,
            'email' => $email
        ];
        
        return self::encodeToken($payload);
    }
    
    /**
     * Validate JWT token from Authorization header
     */
    public static function validateToken() {
        $headers = getallheaders();
        $authHeader = $headers['Authorization'] ?? $headers['authorization'] ?? '';
        
        if (empty($authHeader)) {
            Response::error('Authorization token required', 401);
        }
        
        // Remove 'Bearer ' prefix
        $token = str_replace('Bearer ', '', $authHeader);
        
        $decoded = self::decodeToken($token);
        
        if (!$decoded) {
            Response::error('Invalid or expired token', 401);
        }
        
        // Check expiration
        if ($decoded['exp'] < time()) {
            Response::error('Token expired', 401);
        }
        
        return $decoded;
    }
    
    /**
     * Simple JWT encoding (base64)
     */
    private static function encodeToken($payload) {
        $header = json_encode(['typ' => 'JWT', 'alg' => 'HS256']);
        
        $base64UrlHeader = str_replace(['+', '/', '='], ['-', '_', ''], base64_encode($header));
        $base64UrlPayload = str_replace(['+', '/', '='], ['-', '_', ''], base64_encode(json_encode($payload)));
        
        $signature = hash_hmac('sha256', $base64UrlHeader . "." . $base64UrlPayload, self::$secret_key, true);
        $base64UrlSignature = str_replace(['+', '/', '='], ['-', '_', ''], base64_encode($signature));
        
        return $base64UrlHeader . "." . $base64UrlPayload . "." . $base64UrlSignature;
    }
    
    /**
     * Simple JWT decoding
     */
    private static function decodeToken($token) {
        $parts = explode('.', $token);
        
        if (count($parts) !== 3) {
            return false;
        }
        
        $payload = json_decode(base64_decode(str_replace(['-', '_'], ['+', '/'], $parts[1])), true);
        
        // Verify signature
        $signature = hash_hmac('sha256', $parts[0] . "." . $parts[1], self::$secret_key, true);
        $base64UrlSignature = str_replace(['+', '/', '='], ['-', '_', ''], base64_encode($signature));
        
        if ($base64UrlSignature !== $parts[2]) {
            return false;
        }
        
        return $payload;
    }
    
    /**
     * Hash password
     */
    public static function hashPassword($password) {
        return password_hash($password, PASSWORD_BCRYPT);
    }
    
    /**
     * Verify password
     */
    public static function verifyPassword($password, $hash) {
        return password_verify($password, $hash);
    }
}
