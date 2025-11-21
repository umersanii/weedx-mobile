

# **Architecture**

1. Kotlin Android app with backend in PHP + MySQL
2. Firebase Auth for user authentication
3. Firebase FCM for notifications only
4. Robot sends data → MQTT → PHP subscriber → MySQL
5. Android app reads data via REST API (no robot control)
6. Modular code structure
7. Follow best practices for readability and maintainability
8. Keep all architecture, diagrams, and references inside the `.docs/` folder
9. If unsure about any requirement, ask before implementing
10. Responses must stay concise, direct, and complete

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

