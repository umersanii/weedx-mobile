<?php
/**
 * Refresh Token Endpoint
 * POST /api/auth/refresh
 */

require_once __DIR__ . '/../../config/database.php';
require_once __DIR__ . '/../../utils/response.php';
require_once __DIR__ . '/../../utils/auth.php';

// Validate current token
$tokenData = Auth::validateToken();

// Generate new token
$newToken = Auth::generateToken($tokenData['userId'], $tokenData['email']);

Response::success([
    'token' => $newToken
], 'Token refreshed successfully');
