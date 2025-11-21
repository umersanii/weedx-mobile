<?php
/**
 * MQTT Subscriber for WeedX Robot
 * 
 * This script listens to MQTT topics published by the farming robot
 * and saves the data to MySQL database.
 * 
 * Requirements:
 * - php-mqtt/client library (install via composer)
 * - Mosquitto MQTT broker running
 * 
 * Usage:
 * php mqtt/subscriber.php
 */

require_once __DIR__ . '/../vendor/autoload.php';
require_once __DIR__ . '/../config/database.php';

use PhpMqtt\Client\MqttClient;
use PhpMqtt\Client\ConnectionSettings;

// MQTT Configuration
$mqttHost = 'localhost';
$mqttPort = 1883;
$mqttUsername = null; // Set if MQTT broker requires authentication
$mqttPassword = null;
$clientId = 'weedx-backend-' . uniqid();

// MQTT Topics
$topics = [
    'weedx/robot/status' => 1,
    'weedx/robot/location' => 1,
    'weedx/robot/battery' => 1,
    'weedx/weed/detection' => 1,
    'weedx/sensor/weather' => 1,
    'weedx/sensor/soil' => 1,
    'weedx/alert' => 1
];

// Database connection
$database = new Database();
$db = $database->getConnection();

echo "WeedX MQTT Subscriber Started...\n";
echo "Connecting to MQTT broker at {$mqttHost}:{$mqttPort}...\n";

try {
    $mqtt = new MqttClient($mqttHost, $mqttPort, $clientId);
    
    $connectionSettings = new ConnectionSettings();
    if ($mqttUsername && $mqttPassword) {
        $connectionSettings
            ->setUsername($mqttUsername)
            ->setPassword($mqttPassword);
    }
    
    $mqtt->connect($connectionSettings, true);
    
    echo "Connected to MQTT broker successfully!\n";
    echo "Subscribing to topics...\n";
    
    // Subscribe to all topics
    foreach ($topics as $topic => $qos) {
        $mqtt->subscribe($topic, function ($topic, $message) use ($db) {
            handleMessage($topic, $message, $db);
        }, $qos);
        echo "Subscribed to: {$topic}\n";
    }
    
    echo "Listening for messages... (Press Ctrl+C to stop)\n\n";
    
    // Keep listening
    $mqtt->loop(true);
    
    $mqtt->disconnect();
    
} catch (Exception $e) {
    echo "Error: " . $e->getMessage() . "\n";
    exit(1);
}

/**
 * Handle incoming MQTT messages
 */
function handleMessage($topic, $message, $db) {
    echo "[" . date('Y-m-d H:i:s') . "] Received message on topic: {$topic}\n";
    
    $data = json_decode($message, true);
    
    if (!$data) {
        echo "Invalid JSON data\n";
        return;
    }
    
    try {
        switch ($topic) {
            case 'weedx/robot/status':
                updateRobotStatus($data, $db);
                break;
                
            case 'weedx/robot/location':
                updateRobotLocation($data, $db);
                break;
                
            case 'weedx/robot/battery':
                updateBatteryLevel($data, $db);
                break;
                
            case 'weedx/weed/detection':
                saveWeedDetection($data, $db);
                break;
                
            case 'weedx/sensor/weather':
                saveWeatherData($data, $db);
                break;
                
            case 'weedx/sensor/soil':
                saveSoilData($data, $db);
                break;
                
            case 'weedx/alert':
                saveAlert($data, $db);
                break;
                
            default:
                echo "Unknown topic: {$topic}\n";
        }
        
        echo "Message processed successfully\n\n";
        
    } catch (Exception $e) {
        echo "Error processing message: " . $e->getMessage() . "\n\n";
    }
}

/**
 * Update robot status
 */
function updateRobotStatus($data, $db) {
    $query = "UPDATE robot_status SET 
              status = :status,
              activity = :activity,
              updated_at = NOW()
              WHERE id = 1";
              
    $stmt = $db->prepare($query);
    $stmt->bindParam(':status', $data['status']);
    $stmt->bindParam(':activity', $data['activity']);
    $stmt->execute();
    
    echo "Robot status updated: {$data['status']}\n";
}

/**
 * Update robot location
 */
function updateRobotLocation($data, $db) {
    $query = "UPDATE robot_status SET 
              latitude = :latitude,
              longitude = :longitude,
              speed = :speed,
              heading = :heading,
              updated_at = NOW()
              WHERE id = 1";
              
    $stmt = $db->prepare($query);
    $stmt->bindParam(':latitude', $data['latitude']);
    $stmt->bindParam(':longitude', $data['longitude']);
    $stmt->bindParam(':speed', $data['speed']);
    $stmt->bindParam(':heading', $data['heading']);
    $stmt->execute();
    
    echo "Robot location updated: ({$data['latitude']}, {$data['longitude']})\n";
}

/**
 * Update battery level
 */
function updateBatteryLevel($data, $db) {
    $query = "UPDATE robot_status SET 
              battery_level = :battery,
              herbicide_level = :herbicide,
              updated_at = NOW()
              WHERE id = 1";
              
    $stmt = $db->prepare($query);
    $stmt->bindParam(':battery', $data['battery']);
    $stmt->bindParam(':herbicide', $data['herbicide']);
    $stmt->execute();
    
    echo "Battery updated: {$data['battery']}%\n";
}

/**
 * Save weed detection
 */
function saveWeedDetection($data, $db) {
    $query = "INSERT INTO weed_detections 
              (weed_type, crop_type, confidence, latitude, longitude, image_path, detected_at) 
              VALUES (:weed_type, :crop_type, :confidence, :latitude, :longitude, :image_path, NOW())";
              
    $stmt = $db->prepare($query);
    $stmt->bindParam(':weed_type', $data['weed_type']);
    $stmt->bindParam(':crop_type', $data['crop_type']);
    $stmt->bindParam(':confidence', $data['confidence']);
    $stmt->bindParam(':latitude', $data['latitude']);
    $stmt->bindParam(':longitude', $data['longitude']);
    $stmt->bindParam(':image_path', $data['image_path']);
    $stmt->execute();
    
    echo "Weed detection saved: {$data['weed_type']} (confidence: {$data['confidence']}%)\n";
}

/**
 * Save weather data
 */
function saveWeatherData($data, $db) {
    $query = "INSERT INTO weather_data 
              (temperature, humidity, weather_condition, wind_speed, wind_direction, pressure, recorded_at) 
              VALUES (:temp, :humidity, :condition, :wind_speed, :wind_dir, :pressure, NOW())";
              
    $stmt = $db->prepare($query);
    $stmt->bindParam(':temp', $data['temperature']);
    $stmt->bindParam(':humidity', $data['humidity']);
    $stmt->bindParam(':condition', $data['condition']);
    $stmt->bindParam(':wind_speed', $data['wind_speed']);
    $stmt->bindParam(':wind_dir', $data['wind_direction']);
    $stmt->bindParam(':pressure', $data['pressure']);
    $stmt->execute();
    
    echo "Weather data saved: {$data['temperature']}°C, {$data['condition']}\n";
}

/**
 * Save soil data
 */
function saveSoilData($data, $db) {
    $query = "INSERT INTO soil_data 
              (moisture, temperature, ph, nitrogen, phosphorus, potassium, organic_matter, recorded_at) 
              VALUES (:moisture, :temp, :ph, :n, :p, :k, :om, NOW())";
              
    $stmt = $db->prepare($query);
    $stmt->bindParam(':moisture', $data['moisture']);
    $stmt->bindParam(':temp', $data['temperature']);
    $stmt->bindParam(':ph', $data['ph']);
    $stmt->bindParam(':n', $data['nitrogen']);
    $stmt->bindParam(':p', $data['phosphorus']);
    $stmt->bindParam(':k', $data['potassium']);
    $stmt->bindParam(':om', $data['organic_matter']);
    $stmt->execute();
    
    echo "Soil data saved: moisture {$data['moisture']}%, pH {$data['ph']}\n";
}

/**
 * Save alert
 */
function saveAlert($data, $db) {
    $query = "INSERT INTO alerts 
              (type, severity, message, created_at) 
              VALUES (:type, :severity, :message, NOW())";
              
    $stmt = $db->prepare($query);
    $stmt->bindParam(':type', $data['type']);
    $stmt->bindParam(':severity', $data['severity']);
    $stmt->bindParam(':message', $data['message']);
    $stmt->execute();
    
    echo "Alert saved: [{$data['severity']}] {$data['message']}\n";
}
