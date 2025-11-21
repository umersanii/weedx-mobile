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
Robot → MQTT → PHP Backend → MySQL
                    ↓
              REST API + FCM
                    ↓
              Android App (WeedX)
```

### System Components

1. **Farming Robot** (Raspberry Pi + ROS2)  
   - Publishes telemetry, GPS, and weed detections via MQTT

2. **PHP Backend**  
   - MQTT subscriber stores data in MySQL  
   - Provides REST APIs for mobile app  
   - Sends FCM push notifications

3. **Android App**  
   - Fetches data via REST API  
   - Firebase Auth for user login  
   - Displays dashboards, logs, and reports

📖 **Full Architecture**: [docs/architecture.md](docs/architecture.md)  
📖 **Use Case Flows**: [docs/usecase_flows.md](docs/usecase_flows.md)

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
├── api_endpoints.md      # Backend API documentation
├── architecture.md       # System architecture details
├── implementation_status.md  # Development progress
└── usecase_flows.md      # User flow diagrams
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
   - Download `google-services.json`
   - Place it in `app/google-services.json`

3. **Set Backend URL**
   - Open `app/src/main/java/com/example/weedx/utils/Constants.kt`
   - Replace `BASE_URL` with your PHP backend endpoint:
     ```kotlin
     const val BASE_URL = "https://your-backend.com/api/"
     ```
   - For Tailscale deployment (recommended):
     ```kotlin
     const val BASE_URL = "http://raspberrypi.mullet-bull.ts.net/weedx-backend/"
     ```
   - See [DEPLOYMENT_QUICK_START.md](DEPLOYMENT_QUICK_START.md) for deployment guide

4. **Build & Run**
   ```bash
   ./gradlew assembleDebug
   ```
   Or open in Android Studio and click ▶️ Run

5. **Deploy Backend to Raspberry Pi** (Optional)
   
   If you want to run the backend on Raspberry Pi 24/7 via Tailscale:
   ```bash
   # Interactive setup wizard (recommended)
   ./scripts/setup-wizard.sh
   
   # Or manual deployment
   ./scripts/deploy-to-pi.sh
   ```
   
   See [DEPLOYMENT_QUICK_START.md](DEPLOYMENT_QUICK_START.md) for details.

---

## 🌐 Tailscale Deployment ✅ OPERATIONAL

The backend is running on Raspberry Pi and accessible via Tailscale network:

- **Pi URL**: `http://raspberrypi.mullet-bull.ts.net/weedx-backend/` ✅ **LIVE**
- **Status**: All 30+ API endpoints working
- **Location**: `/var/www/html/weedx-backend/`
- **Database**: MySQL with 12 tables and sample data
- **Quick Setup**: Run `./scripts/setup-wizard.sh` (if redeploying)
- **Full Guide**: [docs/TAILSCALE_DEPLOYMENT.md](docs/TAILSCALE_DEPLOYMENT.md)

Benefits:
- ✅ Access backend from anywhere (no port forwarding)
- ✅ Secure encrypted connection
- ✅ Works on mobile/cellular networks
- ✅ 24/7 availability on Raspberry Pi

---

## 📱 Screenshots

_Coming soon..._

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

## 🛣️ Roadmap

- [x] Auth flow (Firebase + Backend)
- [x] Dashboard screen
- [ ] Complete all API integrations
- [ ] Activity ↔ ViewModel integration
- [ ] Jetpack Compose migration
- [ ] Offline caching with Room
- [ ] Real-time updates via WebSockets
- [ ] Multi-farm support
- [ ] Advanced analytics

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

**Developer**: Syed Ali Sada Sani  
**GitHub**: [@umersanii](https://github.com/umersanii)

---

## 📞 Support

For questions or support:
- **Issues**: [GitHub Issues](https://github.com/umersanii/weedx-mobile/issues)
- **Documentation**: [docs/](docs/)

---

**🌱 Making farming smarter, one weed at a time.**
