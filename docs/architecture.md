# 🏗️ WeedX Architecture

## Overview

WeedX is a **read-only dashboard** for monitoring a precision farming robot. The app displays real-time robot status, weed detections, environmental data, and farming reports. Users **cannot** control the robot from the app.

---

## System Flow

```
┌─────────────────┐
│  Robot/Script   │  Publishes JSON data
└────────┬────────┘
         │
         ↓
┌─────────────────┐
│  MQTT Broker    │  (Mosquitto on localhost:1883)
└────────┬────────┘
         │
         ↓
┌─────────────────┐
│ PHP Subscriber  │  (weedx-mqtt.service - always running)
│  Listens to     │  Auto-saves to MySQL
│  6 topics       │
└────────┬────────┘
         │
         ↓
┌─────────────────┐
│  MySQL Database │  (weedx)
│  12 tables      │
└────────┬────────┘
         │
         ↓
┌─────────────────┐
│  REST API       │  (PHP backend - 51+ endpoints)
│  Apache/Pi      │
└────────┬────────┘
         │
         ↓
┌─────────────────┐
│  Android App    │  (Kotlin MVVM)
│  Read-only      │
│  Dashboard      │
└─────────────────┘
```

**Key**: Robot → MQTT → Subscriber → MySQL → API → App

---

## Components

### 1. **Robot/External Script**
- Publishes JSON data to MQTT topics
- Data: GPS, battery, weed detections, soil readings
- No direct connection to app or database

### 2. **MQTT Broker** (Mosquitto)
- Runs on `localhost:1883`
- Message queue between robot and backend
- 6 active topics (status, location, battery, weed detection, soil, alerts)

### 3. **PHP MQTT Subscriber** (`weedx-mqtt.service`)
- Systemd service (always running in background)
- Subscribes to all MQTT topics
- Automatically saves incoming data to MySQL
- No manual intervention needed

### 4. **MySQL Database**
- 12 tables for users, robots, detections, sensors
- User data isolation (user_id filtering)
- Stores images as base64

### 5. **PHP REST API Backend**
- 51+ endpoints organized by feature
- JWT authentication with user isolation
- Apache serves from `/var/www/html/weedx-backend/`
- Weather fetched via API, not MQTT

### 6. **Android App**
- Read-only dashboard
- Firebase Auth for login
- Retrofit for REST API calls
- No MQTT interaction

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

---

## API Endpoints Summary

### Auth
| Method | Endpoint           | Description                       |
|--------|------------------|-----------------------------------|
| POST   | /auth/login       | Log in user                        |
| POST   | /auth/logout      | Log out user                       |
| POST   | /auth/refresh     | Refresh auth token                 |

### Profile
| Method | Endpoint             | Description                         |
|--------|--------------------|-------------------------------------|
| GET    | /profile            | Get full profile (user, farm, settings) |
| PUT    | /profile            | Update user info (name, email)       |
| PATCH  | /profile/avatar     | Update user avatar                   |
| GET    | /profile/farm       | Get farm info                        |
| PUT    | /profile/farm       | Update farm info                     |
| GET    | /profile/settings   | Get app settings                     |
| PUT    | /profile/settings   | Update app settings                  |

### Landing Page
| Method | Endpoint           | Description                        |
|--------|------------------|------------------------------------|
| GET    | /landing          | Get robot status, today's summary, recent alerts |
| GET    | /robot/status     | Get robot battery, location, speed |
| GET    | /summary/today    | Get today's summary                |
| GET    | /alerts/recent    | Get recent alerts                  |

### Alerts & Notifications
| Method | Endpoint           | Description                        |
|--------|------------------|------------------------------------|
| GET    | /alerts/all       | Get all alerts with pagination     |
| GET    | /alerts/recent    | Get recent alerts (limit 5)        |
| POST   | /alerts/create    | Create new alert                   |

### Weather & Soil
| Method | Endpoint                            | Description                                   |
|--------|-----------------------------------|-----------------------------------------------|
| GET    | /environment                        | Get current weather, 7-day forecast, soil, today's recommendations |
| GET    | /environment/weather/current        | Current weather                               |
| GET    | /environment/weather/forecast       | 7-day forecast                                |
| GET    | /environment/soil                   | Soil conditions (temp, pH)                    |
| GET    | /environment/recommendations/today  | Today's farming recommendations               |

### Live Monitoring
| Method | Endpoint              | Description                                  |
|--------|---------------------|----------------------------------------------|
| GET    | /monitoring          | Get metrics, activity timeline, location     |
| GET    | /monitoring/metrics  | Get live metrics (battery, herbicide, coverage, efficiency) |
| GET    | /monitoring/activity | Get robot activity timeline                  |
| GET    | /monitoring/location | Get robot location                            |

### Weed Logs
| Method | Endpoint               | Description                        |
|--------|----------------------|------------------------------------|
| GET    | /weed-logs            | Full weed log summary and detections |
| GET    | /weed-logs/summary    | Count per weed category             |
| GET    | /weed-logs/detections | Individual weed detections          |

### Reports
| Method | Endpoint                   | Description                          |
|--------|---------------------------|--------------------------------------|
| GET    | /reports                   | Full report (widgets, trends, distribution) |
| GET    | /reports/widgets           | Widgets: total weeds, area, herbicide, efficiency |
| GET    | /reports/weed-trend        | Weed detection trend by days          |
| GET    | /reports/weed-distribution | Weed type distribution per crop      |
| GET    | /reports/export            | Export report (pdf, csv)             |

### Image Gallery
| Method | Endpoint        | Description                  |
|--------|----------------|------------------------------|
| GET    | /gallery       | List images with full URLs   |
| POST   | /gallery       | Upload image (base64)        |
| GET    | /gallery/:id   | View single image details    |
| DELETE | /gallery/:id   | Delete image                 |

> **Note:** Images stored in `data/images/` folder or as base64 in database. GET `/gallery` returns `url`, `thumbnail_url`, `image_url` with full HTTP paths.

### Chatbot Assistant
| Method | Endpoint             | Description               |
|--------|--------------------|---------------------------|
| POST   | /assistant/query   | Send user query           |
| GET    | /assistant/history | Get conversation history  |
