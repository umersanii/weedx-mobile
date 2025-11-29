# WeedX - Copilot Instructions

> Keep responses short and concise, yet complete.

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

xampp/htdocs/backend/  # PHP REST API (deployed to Pi)
├── api/               # Endpoint handlers by feature (auth/, landing.php, monitoring/, etc.)
├── config/            # database.php
├── utils/             # response.php, auth.php, logger.php
└── database/          # Schema and migrations
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
| Backend endpoints | `xampp/htdocs/backend/api/` |

## Commands

```bash
# Build Android
./gradlew assembleDebug

# Test backend endpoints (from project root)
./scripts/test-backend.sh http://raspberrypi.mullet-bull.ts.net/weedx-backend

# Deploy backend to Pi
./scripts/deploy-to-pi.sh
```

## Backend API Structure

PHP endpoints follow pattern: `api/{feature}/{action}.php`
- Auth: `/auth/login`, `/auth/register`
- Dashboard: `/landing`, `/robot/status`, `/alerts/recent`
- Features: `/monitoring/*`, `/weed-logs/*`, `/environment/*`, `/reports/*`, `/gallery/*`, `/profile/*`

Each endpoint uses `utils/response.php` → `Response::success($data)` or `Response::error($msg, $code)`

## Conventions

- **State pattern**: Always use `Idle → Loading → Success/Error` for async operations
- **ViewModels**: Don't auto-load in `init{}` - let Activity call `loadData()` explicitly
- **Images**: Use `Constants.getFullImageUrl(path)` to resolve relative paths
- **Errors**: Catch exceptions at Repository level, return `NetworkResult.Error`
- **UI updates**: Collect StateFlow in `lifecycleScope.launch {}` blocks

## Docs to Maintain

Only maintain: `docs/` and `xampp/htdocs/backend/` directories
