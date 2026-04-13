# 🌱 WeedX - Precision Farming Dashboard

**WeedX** is an Android app for monitoring a precision farming robot that detects and manages weeds autonomously. The app provides real-time insights into robot status, weed detections, environmental conditions, and farming reports.

> **Note**: This is a **read-only dashboard**. The app does not control the robot.

---

## 🚀 Features

- **📊 Dashboard**: Overview of robot status, daily summary, and alerts
- **🤖 Robot Status**: Real-time battery, location, and activity
- **🌿 Weed Logs**: History of detected weeds with images and locations
- **📡 Live Monitoring**: Real-time metrics and activity timeline
- **🌤️ Weather & Soil**: Current conditions, forecasts, and soil data
- **📈 Reports**: Performance analytics with exportable charts (PDF/CSV)
- **🖼️ Image Gallery**: Browse and manage weed detection photos
- **💬 Assistant**: AI chatbot for farming advice
- **👤 Profile**: Manage user and farm information
- **🔔 Push Notifications**: Real-time alerts via Firebase Cloud Messaging

---

## 🛠️ Tech Stack

| Layer          | Technology                              |
|----------------|-----------------------------------------|
| **Language**   | Kotlin 2.0.21                           |
| **UI**         | XML Layouts (Material 3)                |
| **Architecture**| MVVM + Repository Pattern              |
| **DI**         | Hilt (KSP)                              |
| **Networking** | Retrofit + OkHttp + Gson                |
| **Async**      | Coroutines + StateFlow                  |
| **Auth**       | Firebase Authentication                 |
| **Notifications**| Firebase Cloud Messaging (FCM)        |
| **Images**     | Coil                                    |
| **Backend**    | PHP REST API + MySQL                    |

---

## 📐 Architecture

```
Robot/Script → MQTT → PHP Subscriber → MySQL + Firebase FCM → REST API → Android App
                                              ↓
                                    Push Notifications
```

### System Flow

1. **Robot/Script** publishes JSON to MQTT topics
2. **MQTT Subscriber** (`weedx-mqtt.service`) listens and auto-saves to MySQL
3. **Firebase Notifications** sent for critical alerts (battery, faults, detections)
4. **REST API** provides data to Android app
5. **Android App** displays dashboards, analytics, and receives push notifications

📖 **Documentation**: [docs/SETUP_GUIDE.md](docs/SETUP_GUIDE.md) | [docs/architecture.md](docs/architecture.md) | [docs/guide.md#firebase-push-notifications](docs/guide.md#firebase-push-notifications)

---

## 🏗️ Project Structure

```
app/src/main/java/com/example/weedx/
├── data/
│   ├── api/              # Retrofit API services
│   ├── models/           # Request/Response models
│   └── repositories/     # Data layer (Repository Pattern)
├── di/                   # Hilt dependency injection modules
├── presentation/
│   ├── activities/       # UI screens (Activities)
│   └── viewmodels/       # ViewModels (MVVM)
└── utils/                # Constants, helpers

docs/
├── architecture.md       # System architecture + API endpoints
└── guide.md              # Complete setup & usage guide
```

---

## 🚦 Getting Started

### Prerequisites

- Android Studio Ladybug (2024.2.1) or later
- JDK 17+
- Gradle 8.12+
- Android SDK API 24+ (minSdk) to API 36 (targetSdk)

### Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/umersanii/weedx-mobile.git
   cd weedx-mobile
   ```

2. **Configure Firebase**
   - Create a Firebase project at [console.firebase.google.com](https://console.firebase.google.com)
   - Enable Firebase Authentication and Cloud Messaging
   - Download `google-services.json` and place in `app/google-services.json`
   - Download service account key and place in backend: `/var/www/html/weedx-backend/config/firebase-service-account.json`
   - See [docs/guide.md#firebase-push-notifications](docs/guide.md#firebase-push-notifications) for full setup

3. **Configure Backend URL**
   
   Edit `app/src/main/java/com/example/weedx/utils/Constants.kt`:
   ```kotlin
   const val BASE_URL = "http://raspberrypi.mullet-bull.ts.net/weedx-backend/"
   ```

4. **Build & Run**
   ```bash
   ./gradlew build
   ./gradlew assembleDebug
   ```

   Or use the project starter script:
   ```bash
   bash scripts/start-project.sh all
   ```

5. **Deploy Backend** (if running on Pi)
   ```bash
   # Install Firebase Admin SDK (one-time setup)
   cd /var/www/html/weedx-backend
   sudo composer require kreait/firebase-php
   
   # Deploy and setup services
   bash scripts/deploy-backend.sh
   bash scripts/setup-mqtt.sh
   
   # Restart services to load Firebase
   sudo systemctl restart apache2
   sudo systemctl restart weedx-mqtt
   ```

---

## 🌐 Backend Deployment

**Status**: ✅ **LIVE** on Raspberry Pi via Tailscale

- **URL**: `http://raspberrypi.mullet-bull.ts.net/weedx-backend/`
- **Production**: `/var/www/html/weedx-backend/` (Apache serves from here)
- **Source**: `xampp/htdocs/backend/` (edit here, then deploy)

### Development Workflow

1. Edit files in `xampp/htdocs/backend/`
2. Deploy: `bash scripts/deploy-backend.sh`
3. Restart Apache: `sudo systemctl restart apache2`

---

## 🔌 MQTT Integration

**Robot → MQTT → Subscriber → MySQL + Firebase → API + Push Notifications → App**

The system uses MQTT for real-time data ingestion:

1. Robot publishes to topics (status, location, detections, soil, **alerts**)
2. PHP subscriber (`weedx-mqtt.service`) auto-saves to MySQL
3. **Critical alerts trigger Firebase push notifications** to user devices
4. REST API serves data to Android app
5. App receives real-time push notifications for alerts

### Publish Test Data

```bash
# Single message
bash scripts/mqtt-publisher.sh weed

# Test alert with push notification
mosquitto_pub -h localhost -p 1883 -t "weedx/alert" -m '{
    "user_id": 1,
    "type": "battery",
    "severity": "warning",
    "message": "Battery level at 20%. Please charge soon."
}'

# Batch test
bash scripts/mqtt-publisher.sh batch

# Monitor logs (watch for "Push notification sent")
sudo journalctl -u weedx-mqtt -f
```

📖 See [docs/SETUP_GUIDE.md](docs/SETUP_GUIDE.md) for full MQTT setup.
📖 See [docs/guide.md#firebase-push-notifications](docs/guide.md#firebase-push-notifications) for push notification setup.

---

## 🔐 Authentication

1. User logs in via Firebase Auth (email/password)
2. App sends Firebase token to backend
3. Backend validates token, returns app-specific JWT
4. JWT stored in SharedPreferences
5. All API requests include JWT via `AuthInterceptor`

---

## 🧪 Testing

```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

---

## 📦 Dependencies

Key libraries used:

- **Retrofit 2.11.0** - REST API client
- **Hilt 2.51** - Dependency injection
- **Firebase BOM 33.6.0** - Auth + FCM
- **Coroutines 1.8.0** - Async operations
- **Coil 2.6.0** - Image loading
- **Material 3** - UI components

See [gradle/libs.versions.toml](gradle/libs.versions.toml) for full list.

---

## 📋 Todo / Pending Tasks

### High Priority
- [ ] Avatar in profile page
- [ ] Location instead of RID in profile page
- [ ] Soil conditions in the weather page
- [ ] Image path in the gallery
- [ ] App image




### ✅ Recently Completed
- [x] Assistant feature (full Android integration with keyword-based backend)


---

## 🛣️ Roadmap

- [x] Backend deployed on Raspberry Pi via Tailscale
- [x] 51+ backend API endpoints with JWT auth
- [x] MQTT integration for real-time data
- [x] Firebase push notifications for alerts
- [x] Android project with Hilt DI
- [x] All 13 Activity screens
- [x] Complete data layer (API + repositories)
- [ ] 🔄 ViewModels for all modules
- [ ] 🔄 Activity ↔ ViewModel integration
- [ ] ⏳ End-to-end testing
- [ ] ⏳ Offline caching with Room
- [ ] ⏳ Real-time updates
- [ ] ⏳ Jetpack Compose migration

---

## 🤝 Contributing

This is a university project. Contributions are not currently accepted, but you can:

- Report bugs via [Issues](https://github.com/umersanii/weedx-mobile/issues)
- Suggest features
- Fork for your own projects

---

## 📄 License

This project is for educational purposes.  
© 2025 WeedX Team. All rights reserved.

---

## 👥 Team

**Developer**: Umer Sani  
**GitHub**: [@umersanii](https://github.com/umersanii)

---

## 📞 Support

For questions or support:
- **Issues**: [GitHub Issues](https://github.com/umersanii/weedx-mobile/issues)
- **Documentation**: [docs/](docs/)

---

**🌱 Making farming smarter, one weed at a time.**



## Memento Mori
```
adb uninstall com.example.weedx || true
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell monkey -p com.example.weedx -c android.intent.category.LAUNCHER 1
```

```
adb logcat -c && adb logcat -v time | grep --line-buffered -E "com.example.weedx|AndroidRuntime|FATAL EXCEPTION|OkHttp|Retrofit|System.err"
```