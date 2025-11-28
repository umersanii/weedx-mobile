# 🏗️ WeedX Architecture

## Overview

WeedX is a **read-only dashboard** for monitoring a precision farming robot. The app displays real-time robot status, weed detections, environmental data, and farming reports. Users **cannot** control the robot from the app.

---

## System Flow

```
┌─────────────────┐
│  Farming Robot  │  (Raspberry Pi + ROS2)
│  - Camera       │
│  - GPS          │
│  - Sensors      │
└────────┬────────┘
         │ Publishes telemetry & weed detections
         ↓
┌─────────────────┐
│  MQTT Broker    │  (Mosquitto)
└────────┬────────┘
         │ Subscribes
         ↓
┌─────────────────┐
│  PHP Backend    │
│  - MQTT Sub     │
│  - REST API     │────→ Sends FCM notifications
│  - MySQL        │
└────────┬────────┘
         │ REST API calls
         ↓
┌─────────────────┐
│  Android App    │  (Kotlin)
│  - Dashboard    │
│  - Monitoring   │
│  - Reports      │
└─────────────────┘
```

---

## Components

### 1. **Farming Robot**
- Raspberry Pi running ROS2
- Publishes data via MQTT:
  - GPS location
  - Battery level
  - Weed detections (image + coordinates)
  - Sensor readings (soil, weather)

### 2. **MQTT Broker**
- Receives all robot data
- No direct connection to Android app
- Acts as message queue between robot and backend

### 3. **PHP Backend**
- **MQTT Subscriber**: Listens to robot topics, saves to MySQL
- **REST API**: Provides endpoints for Android app
- **FCM**: Sends push notifications for alerts
- **MySQL**: Stores all historical data

### 4. **Android App**
- **Firebase Auth**: User login/signup
- **Multi-step Registration**: User info, Farm info, App settings
- **Retrofit**: Fetches data from REST API
- **FCM**: Receives push notifications
- **UI**: Displays dashboards, logs, reports

---

## App Architecture (MVVM)

```
┌─────────────────────────────────────┐
│         Presentation Layer          │
│  ┌─────────────┐  ┌──────────────┐ │
│  │  Activities │  │  ViewModels  │ │
│  └─────────────┘  └──────────────┘ │
└──────────────┬──────────────────────┘
               │ StateFlow
┌──────────────▼──────────────────────┐
│           Domain Layer              │
│  ┌─────────────────────────────┐   │
│  │  Use Cases (if needed)      │   │
│  └─────────────────────────────┘   │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│            Data Layer               │
│  ┌─────────────┐  ┌──────────────┐ │
│  │ Repositories│  │  API Services│ │
│  └─────────────┘  └──────────────┘ │
│  ┌─────────────┐  ┌──────────────┐ │
│  │   Models    │  │ Interceptors │ │
│  └─────────────┘  └──────────────┘ │
└─────────────────────────────────────┘
```

### Layers Explained

#### **Presentation Layer**
- **Activities**: UI screens (LoginActivity, DashboardActivity, etc.)
- **ViewModels**: Manage UI state, handle user actions
- **StateFlow**: Reactive data streams to UI

#### **Domain Layer** (optional)
- **Use Cases**: Business logic (e.g., "Get Today's Summary")
- Currently minimal, may expand later

#### **Data Layer**
- **Repositories**: Single source of truth, abstracts data sources
- **API Services**: Retrofit interfaces for REST calls
- **Models**: Request/Response data classes
- **Interceptors**: Add auth tokens to requests

---

## Tech Stack

| Component        | Technology                          |
|------------------|-------------------------------------|
| Language         | Kotlin 2.0.21                       |
| UI               | XML Layouts (Material 3)            |
| Architecture     | MVVM + Repository Pattern           |
| DI               | Hilt (KSP)                          |
| Networking       | Retrofit + OkHttp + Gson            |
| Async            | Coroutines + StateFlow              |
| Auth             | Firebase Auth                       |
| Notifications    | Firebase FCM                        |
| Image Loading    | Coil                                |
| Local Storage    | SharedPreferences + Room (future)   |

---

## Key Design Decisions

### 1. **No Robot Control**
- App is purely observational
- All commands happen through separate interface (not this app)

### 2. **Firebase Auth Only**
- Backend has separate auth system
- Firebase used for user identity, token stored locally

### 3. **REST API (Not MQTT)**
- Simpler than managing MQTT client in app
- Backend handles all MQTT complexity
- Polling for near-realtime updates

### 4. **Repository Pattern**
- Single source of truth for each data type
- Easy to mock for testing
- Decouples ViewModels from API details

### 5. **StateFlow over LiveData**
- More Kotlin-idiomatic
- Better coroutine integration
- Lifecycle-aware in compose/modern UI

---

## Data Flow Example: Dashboard Screen

```
1. User opens app
   ↓
2. DashboardActivity created
   ↓
3. Hilt injects DashboardViewModel
   ↓
4. ViewModel calls repository.getDashboard()
   ↓
5. Repository calls API service via Retrofit
   ↓
6. API service hits PHP backend: GET /landing
   ↓
7. Backend queries MySQL, returns JSON
   ↓
8. Retrofit parses to DashboardResponse model
   ↓
9. Repository returns to ViewModel
   ↓
10. ViewModel updates StateFlow (Loading → Success/Error)
    ↓
11. Activity collects StateFlow, updates UI
```

---

## Security

- **Token-based Auth**: JWT/Bearer tokens from backend
- **AuthInterceptor**: Adds token to all API requests
- **SharedPreferences**: Stores token locally (encrypted in production)
- **Firebase Auth**: Manages user sessions
- **HTTPS**: All API calls encrypted

---

## Scalability

- **Modular**: Each feature (Dashboard, Logs, Reports) is isolated
- **Testable**: Repository pattern allows easy mocking
- **Extensible**: Easy to add new API endpoints/screens
- **Offline-ready**: Can add Room cache layer later

---

## Future Enhancements

- [ ] Jetpack Compose migration
- [ ] Offline caching with Room
- [ ] Real-time updates via WebSockets
- [ ] Multi-farm support
- [ ] Advanced analytics dashboard
- [ ] Export reports (PDF/CSV)
