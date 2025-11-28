<?php
/**
 * Refresh Token Endpoint
 * POST /api/auth/refresh
 */

require_once __DIR__ . '/../../config/database.php';
require_once __DIR__ . '/../../utils/response.php';
require_once __DIR__ . '/../../utils/auth.php';
require_once __DIR__ . '/../../utils/logger.php';

Logger::logRequest('/api/auth/refresh', 'POST');

// Validate current token
$tokenData = Auth::validateToken();
Logger::logAuth('/api/auth/refresh', $tokenData['userId'] ?? null, true);

// Generate new token
$newToken = Auth::generateToken($tokenData['userId'], $tokenData['email']);

Logger::logSuccess('/api/auth/refresh', 'Token refreshed for user: ' . $tokenData['email']);
Response::success([
    'token' => $newToken
], 'Token refreshed successfully');
