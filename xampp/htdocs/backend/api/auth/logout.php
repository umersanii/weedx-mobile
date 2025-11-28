<?php
/**
 * Logout Endpoint
 * POST /api/auth/logout
 */

require_once __DIR__ . '/../../utils/response.php';
require_once __DIR__ . '/../../utils/auth.php';
require_once __DIR__ . '/../../utils/logger.php';

Logger::logRequest('/api/auth/logout', 'POST');

// Validate token
$tokenData = Auth::validateToken();
Logger::logAuth('/api/auth/logout', $tokenData['userId'] ?? null, true);

// In a real app, you might want to blacklist the token
// For now, just return success

Logger::logSuccess('/api/auth/logout', 'User logged out');
Response::success(null, 'Logged out successfully');
