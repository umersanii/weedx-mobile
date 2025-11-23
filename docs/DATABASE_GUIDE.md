# Database Management Guide

## Access Database

```bash
mysql -u root -p
# Enter password: root
```

## View Database

```sql
USE weedx;
SHOW TABLES;
DESCRIBE table_name;
SELECT * FROM table_name LIMIT 10;
```

## Common Queries

### View Users
```sql
SELECT id, username, email, role FROM users;
```

### View Robot Status
```sql
SELECT * FROM robot_status ORDER BY timestamp DESC LIMIT 1;
```

### View Recent Weed Detections
```sql
SELECT * FROM weed_detections ORDER BY detection_time DESC LIMIT 10;
```

### View Active Alerts
```sql
SELECT * FROM alerts WHERE status = 'active' ORDER BY created_at DESC;
```

## Modify Data

### Update User
```sql
UPDATE users SET email = 'new@email.com' WHERE username = 'demo';
```

### Insert Test Data
```sql
INSERT INTO weed_detections (user_id, weed_type, confidence, location_lat, location_lon) 
VALUES (1, 'Dandelion', 0.95, 31.5204, 74.3587);
```

### Delete Records
```sql
DELETE FROM alerts WHERE created_at < DATE_SUB(NOW(), INTERVAL 30 DAY);
```

## Reset Database

```bash
cd /home/umersani/weedx-mobile/xampp/htdocs/backend/database
mysql -u root -p < schema.sql
```

## Database Credentials

- **Database**: `weedx`
- **User**: `weedx_user`
- **Password**: `weedx_pass_2024`
- **Root Password**: `root`

## Exit MySQL

```sql
EXIT;
```
