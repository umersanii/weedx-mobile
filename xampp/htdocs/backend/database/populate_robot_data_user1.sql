-- Populate Robot Data for User ID 1
-- Date: 2025-12-08
-- Description: Creates sample robot sessions and activity logs for testing

USE weedx;

-- =====================================================
-- ROBOT SESSIONS for User ID 1
-- =====================================================

-- Session 1: Completed morning session (3 days ago)
INSERT INTO robot_sessions (user_id, start_time, end_time, area_covered, herbicide_used, weeds_detected, battery_start, battery_end, status, notes, created_at)
VALUES (
    1,
    DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 8 HOUR,
    DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 11 HOUR + INTERVAL 30 MINUTE,
    2.5,
    1.8,
    45,
    95,
    42,
    'completed',
    'Morning patrol - North field',
    DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 8 HOUR
);

-- Session 2: Completed afternoon session (3 days ago)
INSERT INTO robot_sessions (user_id, start_time, end_time, area_covered, herbicide_used, weeds_detected, battery_start, battery_end, status, notes, created_at)
VALUES (
    1,
    DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 14 HOUR,
    DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 17 HOUR + INTERVAL 15 MINUTE,
    3.2,
    2.3,
    67,
    100,
    35,
    'completed',
    'Afternoon patrol - South field',
    DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 14 HOUR
);

-- Session 3: Completed session (2 days ago)
INSERT INTO robot_sessions (user_id, start_time, end_time, area_covered, herbicide_used, weeds_detected, battery_start, battery_end, status, notes, created_at)
VALUES (
    1,
    DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 9 HOUR,
    DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 13 HOUR + INTERVAL 45 MINUTE,
    4.1,
    2.9,
    89,
    98,
    28,
    'completed',
    'Extended patrol - East field',
    DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 9 HOUR
);

-- Session 4: Interrupted session (yesterday) - battery low
INSERT INTO robot_sessions (user_id, start_time, end_time, area_covered, herbicide_used, weeds_detected, battery_start, battery_end, status, notes, created_at)
VALUES (
    1,
    DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 10 HOUR,
    DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 12 HOUR + INTERVAL 20 MINUTE,
    1.8,
    1.2,
    34,
    87,
    15,
    'interrupted',
    'Session interrupted - low battery',
    DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 10 HOUR
);

-- Session 5: Completed session (yesterday afternoon)
INSERT INTO robot_sessions (user_id, start_time, end_time, area_covered, herbicide_used, weeds_detected, battery_start, battery_end, status, notes, created_at)
VALUES (
    1,
    DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 15 HOUR,
    DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 18 HOUR + INTERVAL 30 MINUTE,
    3.5,
    2.6,
    72,
    100,
    38,
    'completed',
    'Post-charge patrol - West field',
    DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 15 HOUR
);

-- Session 6: Today's morning session (completed)
INSERT INTO robot_sessions (user_id, start_time, end_time, area_covered, herbicide_used, weeds_detected, battery_start, battery_end, status, notes, created_at)
VALUES (
    1,
    CURDATE() + INTERVAL 7 HOUR + INTERVAL 30 MINUTE,
    CURDATE() + INTERVAL 11 HOUR + INTERVAL 15 MINUTE,
    3.8,
    2.8,
    81,
    96,
    32,
    'completed',
    'Today morning patrol - Central field',
    CURDATE() + INTERVAL 7 HOUR + INTERVAL 30 MINUTE
);

-- Session 7: Today's active session (currently running)
INSERT INTO robot_sessions (user_id, start_time, end_time, area_covered, herbicide_used, weeds_detected, battery_start, battery_end, status, notes, created_at)
VALUES (
    1,
    CURDATE() + INTERVAL 13 HOUR,
    NULL,
    1.2,
    0.9,
    23,
    100,
    NULL,
    'active',
    'Current afternoon patrol - in progress',
    CURDATE() + INTERVAL 13 HOUR
);

-- =====================================================
-- ROBOT ACTIVITY LOG for User ID 1
-- =====================================================

-- Activities from 3 days ago
INSERT INTO robot_activity_log (user_id, action, description, status, metadata, timestamp)
VALUES 
(1, 'System Startup', 'Robot powered on and system initialized', 'completed', '{"battery": 95, "location": "charging_station"}', DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 8 HOUR),
(1, 'Navigation Started', 'Autonomous navigation to North field initiated', 'completed', '{"destination": "north_field", "distance": "0.5km"}', DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 8 HOUR + INTERVAL 5 MINUTE),
(1, 'Weed Detection', 'Detected weed cluster at coordinates', 'completed', '{"weed_type": "broadleaf", "confidence": 94.5}', DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 8 HOUR + INTERVAL 25 MINUTE),
(1, 'Herbicide Application', 'Targeted herbicide spray applied', 'completed', '{"amount": "0.15L", "area": "2.5m²"}', DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 8 HOUR + INTERVAL 26 MINUTE),
(1, 'Patrol Completed', 'Morning patrol session completed successfully', 'completed', '{"area_covered": "2.5ha", "weeds_treated": 45}', DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 11 HOUR + INTERVAL 30 MINUTE),
(1, 'Return to Base', 'Returning to charging station', 'completed', '{"battery_remaining": 42}', DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 11 HOUR + INTERVAL 45 MINUTE),
(1, 'Charging Started', 'Battery charging initiated', 'completed', '{"initial_charge": 42, "target": 100}', DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 12 HOUR);

-- Activities from 2 days ago
INSERT INTO robot_activity_log (user_id, action, description, status, metadata, timestamp)
VALUES 
(1, 'System Startup', 'Robot powered on after full charge', 'completed', '{"battery": 98}', DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 9 HOUR),
(1, 'Navigation Started', 'Route planned to East field', 'completed', '{"waypoints": 12, "estimated_time": "4.5h"}', DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 9 HOUR + INTERVAL 10 MINUTE),
(1, 'Weed Detection', 'Multiple weed clusters identified', 'completed', '{"total_detections": 89, "avg_confidence": 91.2}', DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 10 HOUR + INTERVAL 30 MINUTE),
(1, 'Obstacle Detected', 'Obstacle avoidance system activated', 'completed', '{"obstacle_type": "rock", "action": "path_adjusted"}', DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 11 HOUR + INTERVAL 15 MINUTE),
(1, 'Tank Refill Alert', 'Herbicide tank level low - 15% remaining', 'completed', '{"tank_level": 15, "alert_threshold": 20}', DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 12 HOUR + INTERVAL 45 MINUTE),
(1, 'Patrol Completed', 'Extended patrol session finished', 'completed', '{"area_covered": "4.1ha", "duration": "4h45m"}', DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 13 HOUR + INTERVAL 45 MINUTE);

-- Activities from yesterday
INSERT INTO robot_activity_log (user_id, action, description, status, metadata, timestamp)
VALUES 
(1, 'System Startup', 'Robot initialized for daily operations', 'completed', '{"battery": 87, "system_check": "passed"}', DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 10 HOUR),
(1, 'Weed Detection', 'Active scanning in progress', 'completed', '{"scan_rate": "5/min", "area": "west_quadrant"}', DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 10 HOUR + INTERVAL 45 MINUTE),
(1, 'Low Battery Warning', 'Battery level critical - returning to base', 'completed', '{"battery_level": 15, "distance_to_base": "0.8km"}', DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 12 HOUR + INTERVAL 15 MINUTE),
(1, 'Session Interrupted', 'Patrol interrupted due to low battery', 'failed', '{"reason": "battery_critical", "area_covered": "1.8ha"}', DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 12 HOUR + INTERVAL 20 MINUTE),
(1, 'Charging Started', 'Emergency charge cycle initiated', 'completed', '{"charge_level": 15, "estimated_time": "2.5h"}', DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 12 HOUR + INTERVAL 35 MINUTE),
(1, 'System Startup', 'Robot reactivated after charging', 'completed', '{"battery": 100, "status": "ready"}', DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 15 HOUR),
(1, 'Navigation Started', 'Resuming patrol operations', 'completed', '{"target_field": "west_field", "priority": "high"}', DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 15 HOUR + INTERVAL 10 MINUTE),
(1, 'Patrol Completed', 'Afternoon session completed successfully', 'completed', '{"area_covered": "3.5ha", "weeds_treated": 72}', DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 18 HOUR + INTERVAL 30 MINUTE);

-- Activities from today
INSERT INTO robot_activity_log (user_id, action, description, status, metadata, timestamp)
VALUES 
(1, 'System Startup', 'Daily system initialization complete', 'completed', '{"battery": 96, "weather_check": "optimal", "soil_moisture": "adequate"}', CURDATE() + INTERVAL 7 HOUR + INTERVAL 30 MINUTE),
(1, 'Navigation Started', 'Morning patrol route activated', 'completed', '{"field": "central_field", "pattern": "systematic_grid"}', CURDATE() + INTERVAL 7 HOUR + INTERVAL 40 MINUTE),
(1, 'Weed Detection', 'High weed density area identified', 'completed', '{"density": "high", "weed_types": ["pigweed", "lambsquarters"]}', CURDATE() + INTERVAL 8 HOUR + INTERVAL 30 MINUTE),
(1, 'Herbicide Application', 'Precision spray application in progress', 'completed', '{"spray_pattern": "spot_treatment", "efficiency": 92.5}', CURDATE() + INTERVAL 8 HOUR + INTERVAL 32 MINUTE),
(1, 'Sensor Calibration', 'Camera sensors auto-calibrated for lighting', 'completed', '{"light_level": "optimal", "calibration_status": "success"}', CURDATE() + INTERVAL 9 HOUR + INTERVAL 45 MINUTE),
(1, 'Patrol Completed', 'Morning session completed - returning to base', 'completed', '{"area_covered": "3.8ha", "weeds_detected": 81, "battery_remaining": 32}', CURDATE() + INTERVAL 11 HOUR + INTERVAL 15 MINUTE),
(1, 'Charging Started', 'Mid-day recharge initiated', 'completed', '{"charge_time_est": "1.5h", "next_session": "13:00"}', CURDATE() + INTERVAL 11 HOUR + INTERVAL 30 MINUTE),
(1, 'System Startup', 'Afternoon session preparation', 'completed', '{"battery": 100, "tank_level": 85, "status": "ready"}', CURDATE() + INTERVAL 13 HOUR),
(1, 'Navigation Started', 'Current patrol in progress', 'started', '{"current_location": "central_field_south", "progress": "35%"}', CURDATE() + INTERVAL 13 HOUR + INTERVAL 5 MINUTE),
(1, 'Weed Detection', 'Active weed scanning', 'completed', '{"detections_so_far": 23, "scan_coverage": "1.2ha"}', CURDATE() + INTERVAL 14 HOUR);

-- =====================================================
-- Summary Statistics
-- =====================================================

SELECT 
    '=== ROBOT SESSIONS SUMMARY ===' as summary,
    COUNT(*) as total_sessions,
    SUM(CASE WHEN status = 'completed' THEN 1 ELSE 0 END) as completed_sessions,
    SUM(CASE WHEN status = 'active' THEN 1 ELSE 0 END) as active_sessions,
    SUM(CASE WHEN status = 'interrupted' THEN 1 ELSE 0 END) as interrupted_sessions,
    ROUND(SUM(area_covered), 2) as total_area_covered,
    ROUND(SUM(herbicide_used), 2) as total_herbicide_used,
    SUM(weeds_detected) as total_weeds_detected
FROM robot_sessions 
WHERE user_id = 1;

SELECT 
    '=== TODAY\'S SUMMARY ===' as summary,
    COUNT(*) as todays_sessions,
    ROUND(SUM(area_covered), 2) as area_covered_today,
    ROUND(SUM(herbicide_used), 2) as herbicide_used_today,
    SUM(weeds_detected) as weeds_detected_today
FROM robot_sessions 
WHERE user_id = 1 AND DATE(start_time) = CURDATE();

SELECT 
    '=== ACTIVITY LOG SUMMARY ===' as summary,
    COUNT(*) as total_activities,
    SUM(CASE WHEN status = 'completed' THEN 1 ELSE 0 END) as completed_activities,
    SUM(CASE WHEN status = 'started' THEN 1 ELSE 0 END) as in_progress_activities,
    SUM(CASE WHEN status = 'failed' THEN 1 ELSE 0 END) as failed_activities
FROM robot_activity_log 
WHERE user_id = 1;
