<?php
/**
 * Assistant Query Endpoint
 * POST /api/assistant/query
 */

require_once __DIR__ . '/../../config/database.php';
require_once __DIR__ . '/../../utils/response.php';
require_once __DIR__ . '/../../utils/auth.php';
require_once __DIR__ . '/../../utils/logger.php';

Logger::logRequest('/api/assistant/query', 'POST');

$tokenData = Auth::validateToken();
Logger::logAuth('/api/assistant/query', $tokenData['userId'] ?? null, true);
$database = new Database();
$db = $database->getConnection();

$data = json_decode(file_get_contents("php://input"), true);

Response::validateRequired($data, ['query']);

$query = $data['query'];
$userId = $tokenData['userId'];

try {
    // Save user query to history
    $insertQuery = "INSERT INTO chat_history (user_id, message, is_user, created_at) 
                    VALUES (:user_id, :message, 1, NOW())";
    $stmt = $db->prepare($insertQuery);
    $stmt->bindParam(':user_id', $userId);
    $stmt->bindParam(':message', $query);
    $stmt->execute();
    
    // TODO: Integrate with actual AI/LLM API (OpenAI, Anthropic, etc.)
    // For now, provide simple keyword-based responses
    
    $response = generateResponse($query);
    
    // Save bot response to history
    $insertResponse = "INSERT INTO chat_history (user_id, message, is_user, created_at) 
                       VALUES (:user_id, :message, 0, NOW())";
    $respStmt = $db->prepare($insertResponse);
    $respStmt->bindParam(':user_id', $userId);
    $respStmt->bindParam(':message', $response);
    $respStmt->execute();
    
    Logger::logSuccess('/api/assistant/query', 'Query processed: ' . substr($query, 0, 50) . '...');
    Response::success([
        'query' => $query,
        'response' => $response,
        'timestamp' => date('Y-m-d H:i:s')
    ]);
} catch (Exception $e) {
    Logger::logError('/api/assistant/query', $e->getMessage(), 500);
    Response::error('Failed to process query: ' . $e->getMessage(), 500);
}

function generateResponse($query) {
    $query = strtolower($query);
    
    // Weed detection queries
    if ((strpos($query, 'weed') !== false && strpos($query, 'today') !== false) ||
        strpos($query, 'how many weeds') !== false) {
        return "Today, 142 weeds have been detected across Field A. The most common types are Dandelion (45%), Crabgrass (30%), and Thistle (25%). The detection rate has increased by 12% compared to yesterday. Would you like details on any specific weed type?";
    }
    
    if (strpos($query, 'weed') !== false) {
        return "Based on your recent data, I've detected several common weeds in your field. The most prevalent are broadleaf weeds, which are best treated early in the morning. Your robot has treated 89% of detected weeds successfully today. Would you like specific treatment recommendations?";
    }
    
    // Battery queries
    if (strpos($query, 'battery') !== false) {
        return "The robot's battery level is at 87%. Current status:\n• Estimated runtime: ~4 hours remaining\n• Charging status: Not charging\n• Battery health: Good\n• Last full charge: 6 hours ago\n\nThe battery is sufficient to complete the current field operation.";
    }
    
    // Robot status queries
    if ((strpos($query, 'robot') !== false && strpos($query, 'doing') !== false) ||
        strpos($query, 'robot') !== false && strpos($query, 'status') !== false ||
        strpos($query, 'what is the robot') !== false) {
        return "The robot is currently active in Field A, Zone 12.\n\n📍 Location: A-12 (coordinates: 45.123, -93.456)\n⚡ Speed: 2.4 km/h\n🔋 Battery: 87%\n🌱 Weeds treated: 47 today\n⏱️ Uptime: 4.2 hours\n\nAll systems are functioning normally. The robot is actively detecting and treating weeds.";
    }
    
    // Weekly summary queries
    if (strpos($query, 'weekly') !== false || strpos($query, 'summary') !== false ||
        strpos($query, 'week') !== false) {
        return "📊 Weekly Summary Report (Nov 22-28, 2024)\n\n🌱 Weeds Detected: 856 total\n   • Dandelion: 385\n   • Crabgrass: 257\n   • Thistle: 214\n\n📏 Area Covered: 42.5 hectares\n💧 Herbicide Used: 18.6L (targeted treatment)\n⚡ Efficiency: 95.2%\n⏱️ Total Operating Hours: 34.5h\n\nTrend: Weed detection is down 8% compared to last week, indicating effective treatment.";
    }
    
    // Weather queries
    if (strpos($query, 'weather') !== false) {
        return "Current weather conditions are favorable for farming operations:\n\n🌡️ Temperature: 22°C\n💨 Wind: 12 km/h NW\n💧 Humidity: 65%\n☀️ UV Index: 4 (Moderate)\n\nConditions are optimal for herbicide application. No rain expected for the next 6 hours.";
    }
    
    // Soil queries
    if (strpos($query, 'soil') !== false) {
        return "Current soil conditions:\n\n💧 Moisture: 42% (Optimal range: 35-50%)\n🌡️ Temperature: 18°C\n📊 pH Level: 6.8 (Good)\n🧪 Nitrogen: Moderate\n\nRecommendation: Consider nitrogen supplementation in the northern section of your farm for better crop yield.";
    }
    
    // Crop queries
    if (strpos($query, 'crop') !== false) {
        return "Your crops are in good health based on recent monitoring:\n\n🌾 Growth Stage: Vegetative\n💚 Health Index: 92%\n⚠️ Stress Level: Low\n\nTips:\n• Monitor for early signs of stress in hot weather\n• Ensure adequate watering during peak growth periods\n• Current weed pressure is moderate";
    }
    
    // Help/general queries
    if (strpos($query, 'help') !== false || strpos($query, 'what can you') !== false) {
        return "I can help you with:\n\n🌱 Weed Information - Detection counts, types, treatment status\n🤖 Robot Status - Location, battery, speed, activity\n📊 Reports - Daily/weekly summaries, efficiency metrics\n🌤️ Weather - Current conditions, forecasts\n🌍 Soil - Moisture, pH, nutrients\n🌾 Crops - Health status, recommendations\n\nJust ask me anything about your farm!";
    }
    
    return "I'm here to help you with your precision farming needs! You can ask me about:\n\n• Weed detection and treatment\n• Robot status and battery\n• Weekly/daily summaries\n• Weather conditions\n• Soil health\n• Crop recommendations\n\nWhat would you like to know?";
}
