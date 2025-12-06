#!/usr/bin/env python3
"""
WeedX Live Monitoring Data Simulator
Generates realistic dummy data and publishes to MQTT topics for testing the live monitoring feature.
Runs continuously with configurable intervals (default: 60 seconds).
"""

import json
import time
import random
import argparse
import sys
from datetime import datetime
from faker import Faker
import paho.mqtt.client as mqtt

# Initialize Faker
fake = Faker()

class WeedXSimulator:
    def __init__(self, mqtt_host='localhost', mqtt_port=1883, user_id=1, interval=60):
        self.mqtt_host = mqtt_host
        self.mqtt_port = mqtt_port
        self.user_id = user_id
        self.interval = interval
        self.client = None
        
        # Simulation state
        self.battery = 100
        self.herbicide_level = 50.0
        self.coverage = 0.0
        self.latitude = 34.0522  # Starting point (Los Angeles as example)
        self.longitude = -118.2437
        self.heading = 0
        self.speed = 2.5
        self.total_weeds = 0
        self.session_start = datetime.now()
        
    def connect_mqtt(self):
        """Connect to MQTT broker"""
        try:
            self.client = mqtt.Client(client_id=f"weedx_simulator_{self.user_id}")
            self.client.on_connect = self._on_connect
            self.client.on_disconnect = self._on_disconnect
            
            print(f"🔌 Connecting to MQTT broker at {self.mqtt_host}:{self.mqtt_port}...")
            self.client.connect(self.mqtt_host, self.mqtt_port, 60)
            self.client.loop_start()
            time.sleep(1)  # Wait for connection
            return True
        except Exception as e:
            print(f"❌ Failed to connect to MQTT broker: {e}")
            return False
    
    def _on_connect(self, client, userdata, flags, rc):
        if rc == 0:
            print(f"✅ Connected to MQTT broker successfully")
        else:
            print(f"❌ Connection failed with code {rc}")
    
    def _on_disconnect(self, client, userdata, rc):
        if rc != 0:
            print(f"⚠️ Unexpected disconnection from MQTT broker")
    
    def publish(self, topic, payload):
        """Publish message to MQTT topic"""
        try:
            result = self.client.publish(topic, json.dumps(payload))
            if result.rc == mqtt.MQTT_ERR_SUCCESS:
                print(f"📤 Published to {topic}")
                return True
            else:
                print(f"⚠️ Failed to publish to {topic}")
                return False
        except Exception as e:
            print(f"❌ Error publishing to {topic}: {e}")
            return False
    
    def update_robot_state(self):
        """Update robot internal state for realistic simulation"""
        # Battery decreases slowly
        self.battery = max(0, self.battery - random.uniform(0.1, 0.5))
        
        # Herbicide level decreases with weed detections
        self.herbicide_level = max(0, self.herbicide_level - random.uniform(0.05, 0.2))
        
        # Coverage increases over time
        self.coverage += random.uniform(0.01, 0.05)
        
        # Robot moves (simulated GPS coordinates)
        self.latitude += random.uniform(-0.0001, 0.0001)
        self.longitude += random.uniform(-0.0001, 0.0001)
        self.heading = (self.heading + random.uniform(-15, 15)) % 360
        self.speed = max(0, min(5, self.speed + random.uniform(-0.5, 0.5)))
    
    def publish_robot_status(self):
        """Publish robot status update"""
        payload = {
            "user_id": self.user_id,
            "battery_level": round(self.battery, 1),
            "operational_status": "active" if self.battery > 20 else "low_battery",
            "mode": random.choice(["autonomous", "manual", "scanning"]),
            "speed": round(self.speed, 2),
            "timestamp": datetime.now().isoformat()
        }
        self.publish("weedx/robot/status", payload)
    
    def publish_robot_location(self):
        """Publish GPS location update"""
        payload = {
            "user_id": self.user_id,
            "latitude": round(self.latitude, 6),
            "longitude": round(self.longitude, 6),
            "heading": round(self.heading, 2),
            "speed": round(self.speed, 2),
            "accuracy": round(random.uniform(1.0, 3.0), 2),
            "timestamp": datetime.now().isoformat()
        }
        self.publish("weedx/robot/location", payload)
    
    def publish_battery_herbicide(self):
        """Publish battery and herbicide levels"""
        payload = {
            "user_id": self.user_id,
            "battery_level": round(self.battery, 1),
            "herbicide_level": round(self.herbicide_level, 2),
            "herbicide_capacity": 50.0,
            "battery_voltage": round(random.uniform(11.5, 12.6), 2),
            "timestamp": datetime.now().isoformat()
        }
        self.publish("weedx/robot/battery", payload)
    
    def publish_weed_detection(self):
        """Publish weed detection event"""
        weed_count = random.randint(1, 8)
        self.total_weeds += weed_count
        
        weed_types = ["Broadleaf", "Grass", "Sedge", "Dandelion", "Thistle", "Clover"]
        confidence_levels = ["high", "medium", "low"]
        
        payload = {
            "user_id": self.user_id,
            "detection_id": fake.uuid4(),
            "weed_type": random.choice(weed_types),
            "confidence": random.choice(confidence_levels),
            "count": weed_count,
            "location": {
                "latitude": round(self.latitude, 6),
                "longitude": round(self.longitude, 6)
            },
            "size_cm2": round(random.uniform(5, 50), 2),
            "action_taken": random.choice(["sprayed", "marked", "skipped"]),
            "timestamp": datetime.now().isoformat()
        }
        self.publish("weedx/weed/detection", payload)
    
    def publish_soil_data(self):
        """Publish soil sensor data"""
        payload = {
            "user_id": self.user_id,
            "moisture": round(random.uniform(20, 60), 1),
            "temperature": round(random.uniform(15, 30), 1),
            "ph_level": round(random.uniform(5.5, 7.5), 2),
            "nitrogen": round(random.uniform(10, 50), 1),
            "phosphorus": round(random.uniform(10, 50), 1),
            "potassium": round(random.uniform(10, 50), 1),
            "timestamp": datetime.now().isoformat()
        }
        self.publish("weedx/sensor/soil", payload)
    
    def publish_alert(self):
        """Publish system alert (randomly)"""
        if random.random() < 0.3:  # 30% chance to send alert
            alert_types = [
                ("info", "System checkpoint reached"),
                ("warning", "Herbicide level below 30%"),
                ("critical", "High weed density detected"),
                ("info", "Zone transition complete"),
                ("warning", "Battery level below 40%")
            ]
            
            severity, message = random.choice(alert_types)
            
            payload = {
                "user_id": self.user_id,
                "type": "system",
                "severity": severity,
                "message": message,
                "location": {
                    "latitude": round(self.latitude, 6),
                    "longitude": round(self.longitude, 6)
                },
                "timestamp": datetime.now().isoformat()
            }
            self.publish("weedx/alert", payload)
    
    def publish_all_data(self):
        """Publish data to all relevant topics"""
        print(f"\n{'='*60}")
        print(f"⏰ Publishing cycle at {datetime.now().strftime('%H:%M:%S')}")
        print(f"👤 User ID: {self.user_id}")
        print(f"🔋 Battery: {self.battery:.1f}% | 💧 Herbicide: {self.herbicide_level:.1f}L")
        print(f"📍 Coverage: {self.coverage:.2f}ha | 🌿 Total Weeds: {self.total_weeds}")
        print(f"{'='*60}")
        
        # Update internal state
        self.update_robot_state()
        
        # Publish to all topics
        self.publish_robot_status()
        time.sleep(0.2)
        
        self.publish_robot_location()
        time.sleep(0.2)
        
        self.publish_battery_herbicide()
        time.sleep(0.2)
        
        # Publish weed detection (70% chance)
        if random.random() < 0.7:
            self.publish_weed_detection()
            time.sleep(0.2)
        
        # Publish soil data (50% chance)
        if random.random() < 0.5:
            self.publish_soil_data()
            time.sleep(0.2)
        
        # Publish alert (30% chance)
        self.publish_alert()
        
        print(f"✅ Cycle complete. Waiting {self.interval}s for next cycle...")
    
    def run(self):
        """Main loop - run simulator continuously"""
        if not self.connect_mqtt():
            print("❌ Cannot start simulator without MQTT connection")
            sys.exit(1)
        
        print(f"\n{'='*60}")
        print(f"🚀 WeedX Live Monitoring Simulator Started")
        print(f"{'='*60}")
        print(f"📡 MQTT Broker: {self.mqtt_host}:{self.mqtt_port}")
        print(f"👤 User ID: {self.user_id}")
        print(f"⏱️  Interval: {self.interval} seconds")
        print(f"🕐 Started at: {self.session_start.strftime('%Y-%m-%d %H:%M:%S')}")
        print(f"{'='*60}\n")
        print("Press Ctrl+C to stop...\n")
        
        cycle_count = 0
        try:
            while True:
                cycle_count += 1
                print(f"\n🔄 Cycle #{cycle_count}")
                self.publish_all_data()
                time.sleep(self.interval)
        except KeyboardInterrupt:
            print(f"\n\n{'='*60}")
            print("⏹️  Simulator stopped by user")
            print(f"{'='*60}")
            print(f"📊 Session Summary:")
            print(f"   • Total Cycles: {cycle_count}")
            print(f"   • Total Weeds Detected: {self.total_weeds}")
            print(f"   • Total Coverage: {self.coverage:.2f}ha")
            print(f"   • Final Battery: {self.battery:.1f}%")
            print(f"   • Final Herbicide: {self.herbicide_level:.1f}L")
            print(f"{'='*60}\n")
        finally:
            if self.client:
                self.client.loop_stop()
                self.client.disconnect()


def main():
    parser = argparse.ArgumentParser(
        description='WeedX Live Monitoring Data Simulator',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  # Run with default settings (localhost, user_id=1, 60s interval)
  python3 live_monitoring_simulator.py

  # Run with custom user ID and interval
  python3 live_monitoring_simulator.py -u 2 -i 30

  # Run with custom MQTT broker
  python3 live_monitoring_simulator.py --host 192.168.1.100 -p 1883

  # Quick testing mode (10 second intervals)
  python3 live_monitoring_simulator.py -i 10
        """
    )
    
    parser.add_argument('--host', default='localhost',
                        help='MQTT broker host (default: localhost)')
    parser.add_argument('-p', '--port', type=int, default=1883,
                        help='MQTT broker port (default: 1883)')
    parser.add_argument('-u', '--user-id', type=int, default=1,
                        help='User ID for the simulated data (default: 1)')
    parser.add_argument('-i', '--interval', type=int, default=60,
                        help='Interval between data publications in seconds (default: 60)')
    
    args = parser.parse_args()
    
    simulator = WeedXSimulator(
        mqtt_host=args.host,
        mqtt_port=args.port,
        user_id=args.user_id,
        interval=args.interval
    )
    
    simulator.run()


if __name__ == '__main__':
    main()
