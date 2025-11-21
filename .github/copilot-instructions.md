Keep the responses short and consise, yet complete

# **Architecture**

- Kotlin Android app + PHP/MySQL backend
- Firebase Auth + FCM only (no MQTT in app)
- Robot → MQTT → PHP → MySQL → REST API → Android
- App is read-only dashboard (no robot control)
- MVVM + Repository pattern, Hilt DI
- See `docs/` for details

---

# 📋 **Status**

## ✅ Done - Backend (Nov 21, 2025)
- ✅ **PHP Backend Deployed** on Arch Linux LAMP stack
- ✅ Apache httpd + MariaDB + PHP 8.4.15 configured
- ✅ Database schema with 12 tables (users, robot_status, weed_detections, weather_data, etc.)
- ✅ 50+ REST API endpoints implemented
- ✅ JWT authentication working (`/auth/login`)
- ✅ CORS enabled, mod_rewrite configured
- ✅ Sample data loaded, demo user created
- ✅ Backend URL: `http://192.168.1.8/weedx-backend/`
- ✅ Database: `weedx` (user: `weedx_user`, password: `weedx_pass_2024`)

## ✅ Done - Android
- Dependencies: Retrofit, Hilt/KSP, Firebase, Coroutines, Room, Coil
- Data layer: `data/api/`, `data/models/`, `data/repositories/`
- DI modules: Network, App, API, Repository
- Auth: API service, repository, ViewModel, interceptor
- ViewBinding enabled
- Docs: README, architecture, use cases, API endpoints
- Constants.kt updated with backend URL

## ⚠️ Pending - Android Integration
- 7 API services: Dashboard, WeedLogs, Monitoring, Environment, Reports, Gallery, Assistant, Profile
- 8 repositories (same list)
- 8 ViewModels (same list)
- Activity integration: `@AndroidEntryPoint`, inject ViewModels, collect StateFlow, add Loading/Success/Error UI
- Test endpoints with real backend
- Configure real `google-services.json` for Firebase

---

# 🎯 **Next Steps**

## Phase 1: Test Backend Connection
1. Test login from Android app with backend at `http://192.168.1.8/weedx-backend/`
2. Verify JWT token generation and storage
3. Test authenticated endpoint (e.g., `/robot/status`)

## Phase 2: Build API Services (Priority Order)
1. **DashboardApiService** - `/dashboard/stats`, `/dashboard/recent-activity`
2. **RobotApiService** - `/robot/status`, `/robot/sessions`, `/robot/activity-log`
3. **WeedLogsApiService** - `/weed-logs/recent`, `/weed-logs/details/{id}`
4. **MonitoringApiService** - `/monitoring/sensors`, `/monitoring/alerts`
5. **EnvironmentApiService** - `/environment/soil`, `/environment/weather`
6. **ReportsApiService** - `/reports/list`, `/reports/generate`
7. **GalleryApiService** - `/gallery/list`, `/gallery/upload`
8. **ProfileApiService** - `/profile/view`, `/profile/update`

## Phase 3: Each Module Pattern
1. Create API service interface
2. Implement Repository
3. Build ViewModel with StateFlow
4. Update Activity with `@AndroidEntryPoint`
5. Collect StateFlow, handle Loading/Success/Error states
6. Test with real backend data

---

# 📱 **Mobile App Architecture (Kotlin + PHP + Firebase)**

The app is a **dashboard-only** client for the Precision Farming Robot.
It shows robot status, weed detections, logs, and reports.
It does **not** send any control commands.

## ⚙ Overview

```
Robot → MQTT → PHP Backend → MySQL → Android App (REST API)
                           ↑
                     Firebase Auth + FCM
```

---

# 🧩 **Components**

### **Robot (Raspberry Pi + ROS2)**

* Publishes telemetry, sensor data, weed detections → MQTT
* Sends logs/session details

### **MQTT Broker (Mosquitto)**

* Robot publishes all data here
* No direct Android connection

### **PHP Backend** ✅ DEPLOYED

* **Location**: `/srv/http/weedx-backend/` on Arch Linux
* **URL**: `http://192.168.1.8/weedx-backend/`
* **Database**: MariaDB `weedx` (12 tables with sample data)
* **Authentication**: JWT with bcrypt password hashing
* MQTT subscriber ready at `mqtt/subscriber.php` (to be configured)
* Provides 50+ REST APIs:
  * `/auth/login`, `/auth/register`, `/auth/logout`
  * `/dashboard/stats`, `/dashboard/recent-activity`
  * `/robot/status`, `/robot/sessions`, `/robot/activity-log`
  * `/weed-logs/*`, `/monitoring/*`, `/environment/*`
  * `/reports/*`, `/gallery/*`, `/profile/*`
* FCM notification structure ready (needs Firebase config)

### **MySQL**

* Stores all telemetry, logs, detections, reports

### **Android App (Kotlin)**

* Jetpack Compose + MVVM
* Firebase Auth for login
* Retrofit for PHP APIs
* FCM for push notifications
* Shows:

  * dashboard
  * weed detections
  * logs
  * reports

---

# 🛠 **Tech Stack**

**Android:** Kotlin, Jetpack Compose, MVVM, Retrofit, Firebase Auth, FCM
**Backend:** PHP, MySQL, MQTT subscriber
**Robot:** ROS2, Python, MQTT

---

# 🎯 **Key Points**

* App is **read-only**
* No robot control
* No multi-language support
* Near real-time updates via REST polling
* Firebase = auth + notifications only

---

# 📂 **Code Organization (Android)**

```
data/
  - api/
  - repositories/
  - models/

domain/
  - usecases/

presentation/
  - screens/
  - viewmodels/
```

**Practices:**

* Repository pattern
* StateFlow for UI state
* Coroutines for async
* DI (Hilt/Koin recommended)
* Clean, pure, reusable Composables

---

# 💻 **Coding Standards**

* Consistent Loading / Success / Error states
* Use Material 3
* Keep UI state in ViewModels
* Avoid logic inside Composables
* Ask for clarification before implementing unclear features

