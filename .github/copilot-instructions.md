

# **Architecture**

- Kotlin Android app + PHP/MySQL backend
- Firebase Auth + FCM only (no MQTT in app)
- Robot → MQTT → PHP → MySQL → REST API → Android
- App is read-only dashboard (no robot control)
- MVVM + Repository pattern, Hilt DI
- See `docs/` for details

---

# 📋 **Status**

## ✅ Done
- Dependencies: Retrofit, Hilt/KSP, Firebase, Coroutines, Room, Coil
- Data layer: `data/api/`, `data/models/`, `data/repositories/`
- DI modules: Network, App, API, Repository
- Auth: API service, repository, ViewModel, interceptor
- ViewBinding enabled
- Docs: README, architecture, use cases, API endpoints

## ⚠️ Pending
- 7 API services: Dashboard, WeedLogs, Monitoring, Environment, Reports, Gallery, Assistant, Profile
- 8 repositories (same list)
- 8 ViewModels (same list)
- Activity integration: `@AndroidEntryPoint`, inject ViewModels, collect StateFlow, add Loading/Success/Error UI
- Config: Real `google-services.json` + `Constants.BASE_URL`

---

# 🎯 **Next**

1. Build test with Auth-only backend
2. Add modules one-by-one: Dashboard → WeedLogs → Monitoring → Weather → Reports → Gallery → Profile → Assistant
3. Each module: API service → Repository → ViewModel → Activity
4. Test Auth with LoginActivity first

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

### **PHP Backend**

* MQTT subscriber saves robot data to MySQL
* Provides REST APIs:

  * /status
  * /weeds
  * /logs
  * /reports
* Sends FCM notifications when needed (faults, events)

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

