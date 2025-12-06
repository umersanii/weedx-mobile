# WeedX - Copilot Instructions

> Keep responses short and concise, yet complete.
> This VS CODE Instance is running on the raspberry pi hosting the backend.

## ⚠️ CRITICAL: Backend File Locations

**Development Location** (where you edit):
- Path: `/home/umersani/weedx-mobile/xampp/htdocs/backend/`
- This is the source code in the Git repository
- Make ALL changes here

**Production Location** (where Apache serves from):
- Path: `/var/www/html/weedx-backend/`
- This is where the live backend runs
- **NEVER edit files here directly**

**Deployment Workflow**:
1. Edit files in `xampp/htdocs/backend/`
2. Run deployment script: `bash scripts/deploy-backend.sh`
3. Or manually copy: `sudo cp -r xampp/htdocs/backend/* /var/www/html/weedx-backend/`
4. Restart Apache: `sudo systemctl restart apache2`

**Always deploy after making backend changes!**

## Architecture Overview

**System flow:** Robot → MQTT → PHP Backend → MySQL → REST API → Android App

- **Android**: Kotlin + XML (Material 3) + MVVM + Hilt DI + ViewBinding
- **Backend**: PHP REST API + MySQL (MariaDB) on Raspberry Pi via Tailscale
- **Auth**: Firebase Auth in app, JWT tokens from backend
- **App is read-only dashboard** - no robot control commands

## Project Structure

```
app/src/main/java/com/example/weedx/
├── data/
│   ├── api/           # Retrofit interfaces (e.g., DashboardApiService.kt)
│   ├── models/
│   │   ├── request/   # POST body DTOs
│   │   └── response/  # API response data classes
│   └── repositories/  # Single source of truth, wraps API calls
├── di/                # Hilt modules: NetworkModule, ApiModule, RepositoryModule
├── presentation/
│   └── viewmodels/    # StateFlow-based ViewModels
├── utils/             # Constants.kt, NetworkResult.kt
└── *.kt               # Activities (DashboardActivity, etc.) + Adapters

xampp/htdocs/backend/  # PHP REST API source code (Git repo)
├── api/               # Endpoint handlers by feature (auth/, landing.php, monitoring/, etc.)
├── config/            # database.php
├── utils/             # response.php, auth.php, logger.php
└── database/          # Schema and migrations

/var/www/html/weedx-backend/  # DEPLOYED backend (Apache serves from here)
└── (Same structure as above - deployed via scripts/deploy-backend.sh)
```

## Critical Patterns

### API Service → Repository → ViewModel → Activity

1. **API Service** (`data/api/`): Retrofit interface returning `Response<ApiResponse<T>>`
   ```kotlin
   @GET("landing")
   suspend fun getDashboard(): Response<ApiResponse<DashboardResponse>>
   ```

2. **Repository** (`data/repositories/`): Returns `NetworkResult<T>`, handles errors
   ```kotlin
   suspend fun getDashboard(): NetworkResult<DashboardResponse> {
       return try {
           val response = apiService.getDashboard()
           if (response.isSuccessful && response.body()?.success == true) {
               NetworkResult.Success(response.body()!!.data!!)
           } else { NetworkResult.Error(response.body()?.message ?: "Error") }
       } catch (e: Exception) { NetworkResult.Error(e.message ?: "Unknown error") }
   }
   ```

3. **ViewModel** (`presentation/viewmodels/`): Sealed class states, StateFlow
   ```kotlin
   @HiltViewModel
   class DashboardViewModel @Inject constructor(private val repo: DashboardRepository) : ViewModel() {
       private val _state = MutableStateFlow<DashboardState>(DashboardState.Idle)
       val state: StateFlow<DashboardState> = _state
       
       sealed class DashboardState {
           object Idle : DashboardState()
           object Loading : DashboardState()
           data class Success(val data: DashboardResponse) : DashboardState()
           data class Error(val message: String) : DashboardState()
       }
   }
   ```

4. **Activity**: `@AndroidEntryPoint`, `by viewModels()`, collect StateFlow in `lifecycleScope`

### API Response Wrapper

All backend responses use: `ApiResponse<T>(success: Boolean, message: String?, data: T?)`

### Hilt DI Wiring

- `NetworkModule`: Provides OkHttpClient, Retrofit, AuthInterceptor
- `ApiModule`: Provides all `*ApiService` interfaces
- `RepositoryModule`: Binds repositories

## Key Files

| Purpose | File |
|---------|------|
| Backend URL | `utils/Constants.kt` - `BASE_URL` |
| Auth token storage | SharedPreferences via `AuthInterceptor.kt` |
| Network wrapper | `utils/NetworkResult.kt` - sealed class |
| Example Activity | `DashboardActivity.kt` - full MVVM pattern |
| Example ViewModel | `presentation/viewmodels/DashboardViewModel.kt` |
| Backend source | `xampp/htdocs/backend/api/` (edit here) |
| Backend production | `/var/www/html/weedx-backend/api/` (deployed here) |

## Commands

```bash
# Build Android
./gradlew assembleDebug

# Test backend endpoints (from project root)
./scripts/test-backend.sh http://raspberrypi.mullet-bull.ts.net/weedx-backend

# Deploy backend changes to production (REQUIRED after backend edits)
bash scripts/deploy-backend.sh
# Or manual: sudo cp -r xampp/htdocs/backend/* /var/www/html/weedx-backend/

# Deploy to remote Pi (if not running on Pi)
./scripts/deploy-to-pi.sh
```

## Backend API Structure

PHP endpoints follow pattern: `api/{feature}/{action}.php`
- Auth: `/auth/login`, `/auth/register`
- Dashboard: `/landing`, `/robot/status`, `/alerts/recent`
- Features: `/monitoring/*`, `/weed-logs/*`, `/environment/*`, `/reports/*`, `/gallery/*`, `/profile/*`

Each endpoint uses `utils/response.php` → `Response::success($data)` or `Response::error($msg, $code)`

**Security - User Data Isolation**:
- All endpoints that query `weed_detections` table MUST filter by `user_id`
- Extract user ID from JWT: `$tokenData = Auth::validateToken();` → `$tokenData['userId']`
- Add to queries: `WHERE user_id = :user_id` with `$stmt->bindParam(':user_id', $tokenData['userId'], PDO::PARAM_INT)`
- This ensures users only see their own data (gallery, reports, logs, etc.)

## Conventions

- **State pattern**: Always use `Idle → Loading → Success/Error` for async operations
- **ViewModels**: Don't auto-load in `init{}` - let Activity call `loadData()` explicitly
- **Images**: Use `Constants.getFullImageUrl(path)` to resolve relative paths
- **Errors**: Catch exceptions at Repository level, return `NetworkResult.Error`
- **UI updates**: Collect StateFlow in `lifecycleScope.launch {}` blocks

## Docs to Maintain

Only maintain: `docs/` and `xampp/htdocs/backend/` directories
