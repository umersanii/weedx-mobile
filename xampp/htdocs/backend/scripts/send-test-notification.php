<?php
/**
 * Send a test push notification to a user by email.
 * Uses FCM HTTP v1 API directly (no composer packages required).
 * Usage: php send-test-notification.php <email>
 */

require_once __DIR__ . '/../config/database.php';

$email = $argv[1] ?? null;

if (!$email) {
    echo "Usage: php send-test-notification.php <email>\n";
    exit(1);
}

// ── 1. Resolve user from DB ───────────────────────────────────────────────────
$database = new Database();
$db = $database->getConnection();

$stmt = $db->prepare("SELECT id, name FROM users WHERE email = :email LIMIT 1");
$stmt->bindParam(':email', $email);
$stmt->execute();
$user = $stmt->fetch(PDO::FETCH_ASSOC);

if (!$user) {
    echo "Error: No user found with email '{$email}'\n";
    exit(1);
}
echo "Found user: {$user['name']} (id={$user['id']})\n";

// ── 2. Get active FCM tokens ──────────────────────────────────────────────────
$tokenStmt = $db->prepare("SELECT token, device_info FROM fcm_tokens WHERE user_id = :uid AND active = 1");
$tokenStmt->bindParam(':uid', $user['id'], PDO::PARAM_INT);
$tokenStmt->execute();
$tokens = $tokenStmt->fetchAll(PDO::FETCH_ASSOC);

if (empty($tokens)) {
    echo "Error: No active FCM tokens registered for this user.\n";
    exit(1);
}
echo "Active tokens: " . count($tokens) . "\n";
foreach ($tokens as $t) {
    echo "  - " . substr($t['token'], 0, 30) . "... ({$t['device_info']})\n";
}

// ── 3. Load service account ───────────────────────────────────────────────────
$serviceAccountPath = __DIR__ . '/../config/firebase-service-account.json';
$sa = json_decode(file_get_contents($serviceAccountPath), true);
$projectId = $sa['project_id'];

// ── 4. Build & sign JWT to get OAuth2 access token ───────────────────────────
function buildJwt(array $sa): string {
    $now = time();
    $header  = base64_url_encode(json_encode(['alg' => 'RS256', 'typ' => 'JWT']));
    $payload = base64_url_encode(json_encode([
        'iss'   => $sa['client_email'],
        'scope' => 'https://www.googleapis.com/auth/firebase.messaging',
        'aud'   => $sa['token_uri'],
        'iat'   => $now,
        'exp'   => $now + 3600,
    ]));

    $signing_input = "{$header}.{$payload}";
    $key = openssl_pkey_get_private($sa['private_key']);
    openssl_sign($signing_input, $signature, $key, 'SHA256');

    return "{$signing_input}." . base64_url_encode($signature);
}

function base64_url_encode(string $data): string {
    return rtrim(strtr(base64_encode($data), '+/', '-_'), '=');
}

function getAccessToken(array $sa): string {
    $jwt = buildJwt($sa);

    $ch = curl_init($sa['token_uri']);
    curl_setopt_array($ch, [
        CURLOPT_POST           => true,
        CURLOPT_RETURNTRANSFER => true,
        CURLOPT_POSTFIELDS     => http_build_query([
            'grant_type' => 'urn:ietf:params:oauth:grant-type:jwt-bearer',
            'assertion'  => $jwt,
        ]),
    ]);
    $response = json_decode(curl_exec($ch), true);
    curl_close($ch);

    if (empty($response['access_token'])) {
        echo "Error getting access token: " . json_encode($response) . "\n";
        exit(1);
    }
    return $response['access_token'];
}

echo "Fetching OAuth2 access token...\n";
$accessToken = getAccessToken($sa);
echo "Access token obtained.\n";

// ── 5. Send notification to each token ───────────────────────────────────────
$url = "https://fcm.googleapis.com/v1/projects/{$projectId}/messages:send";

$successCount = 0;
foreach ($tokens as $t) {
    $payload = json_encode([
        'message' => [
            'token' => $t['token'],
            'notification' => [
                'title' => 'WeedX Test Notification',
                'body'  => 'This is a test notification from the WeedX backend.',
            ],
            'data' => [
                'type'     => 'detection',
                'severity' => 'info',
            ],
        ],
    ]);

    $ch = curl_init($url);
    curl_setopt_array($ch, [
        CURLOPT_POST           => true,
        CURLOPT_RETURNTRANSFER => true,
        CURLOPT_HTTPHEADER     => [
            'Authorization: Bearer ' . $accessToken,
            'Content-Type: application/json',
        ],
        CURLOPT_POSTFIELDS     => $payload,
    ]);
    $result   = json_decode(curl_exec($ch), true);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);

    $device = substr($t['token'], 0, 20) . '...';
    if ($httpCode === 200) {
        echo "  Sent to {$device} ✓\n";
        $successCount++;
    } else {
        echo "  Failed for {$device}: " . json_encode($result) . "\n";
    }
}

echo "\nDone: {$successCount}/" . count($tokens) . " notifications sent.\n";
