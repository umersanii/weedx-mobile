# API Endpoints Summary

## Auth
| Method | Endpoint           | Description                       | Status |
|--------|------------------|-----------------------------------|--------|
| POST   | /auth/login       | Log in user                        | ✅ |
| POST   | /auth/logout      | Log out user                       | ✅ |
| POST   | /auth/refresh     | Refresh auth token                 | ✅ |

---

## Profile
| Method | Endpoint             | Description                         | Status |
|--------|--------------------|-------------------------------------|--------|
| GET    | /profile            | Get full profile (user, farm, settings) | ✅ |
| PUT    | /profile            | Update user info (name, email)       | ✅ |
| PATCH  | /profile/avatar     | Update user avatar                   | ✅ |
| GET    | /profile/farm       | Get farm info                        | ✅ |
| PUT    | /profile/farm       | Update farm info                     | ✅ |
| GET    | /profile/settings   | Get app settings                     | ✅ |
| PUT    | /profile/settings   | Update app settings                  | ✅ |

---

## Landing Page
| Method | Endpoint           | Description                        | Status |
|--------|------------------|------------------------------------|--------|
| GET    | /landing          | Get robot status, today's summary, recent alerts | ✅ |
| GET    | /robot/status     | Get robot battery, location, speed | ✅ |
| GET    | /summary/today    | Get today's summary                | ✅ |
| GET    | /alerts/recent    | Get recent alerts                  | ✅ |

---

## Weather & Soil
| Method | Endpoint                            | Description                                   | Status |
|--------|-----------------------------------|-----------------------------------------------|--------|
| GET    | /environment                        | Get current weather, 7-day forecast, soil, today's recommendations | ✅ |
| GET    | /environment/weather/current        | Current weather                               | ✅ |
| GET    | /environment/weather/forecast       | 7-day forecast                                | ✅ |
| GET    | /environment/soil                   | Soil conditions (temp, pH)                    | ✅ |
| GET    | /environment/recommendations/today  | Today's farming recommendations               | ✅ |

---

## Live Monitoring
| Method | Endpoint              | Description                                  | Status |
|--------|---------------------|----------------------------------------------|--------|
| GET    | /monitoring          | Get metrics, activity timeline, location     | ✅ |
| GET    | /monitoring/metrics  | Get live metrics (battery, herbicide, coverage, efficiency) | ✅ |
| GET    | /monitoring/activity | Get robot activity timeline                  | ✅ |
| GET    | /monitoring/location | Get robot location                            | ✅ |

---

## Weed Logs
| Method | Endpoint               | Description                        | Status |
|--------|----------------------|------------------------------------|--------|
| GET    | /weed-logs            | Full weed log summary and detections | ✅ |
| GET    | /weed-logs/summary    | Count per weed category             | ✅ |
| GET    | /weed-logs/detections | Individual weed detections          | ✅ |

---

## Reports
| Method | Endpoint                   | Description                          | Status |
|--------|---------------------------|--------------------------------------|--------|
| GET    | /reports                   | Full report (widgets, trends, distribution) | ✅ |
| GET    | /reports/widgets           | Widgets: total weeds, area, herbicide, efficiency | ✅ |
| GET    | /reports/weed-trend        | Weed detection trend by days          | ✅ |
| GET    | /reports/weed-distribution | Weed type distribution per crop      | ✅ |
| GET    | /reports/export            | Export report (pdf, csv)             | ✅ |

---

## Image Gallery
| Method | Endpoint        | Description                  | Status |
|--------|----------------|------------------------------|--------|
| GET    | /gallery       | List images with full URLs   | ✅ |
| POST   | /gallery       | Upload image (base64)        | ✅ |
| GET    | /gallery/:id   | View single image details    | ✅ |
| DELETE | /gallery/:id   | Delete image                 | ✅ |

**Note:** 
- Images stored in `data/images/` folder or as base64 in database
- GET `/gallery` returns `url`, `thumbnail_url`, `image_url` with full HTTP paths
- Example URL: `http://raspberrypi.mullet-bull.ts.net/weedx-backend/data/images/image.png`

---

## Chatbot Assistant
| Method | Endpoint             | Description               | Status |
|--------|--------------------|---------------------------|--------|
| POST   | /assistant/query   | Send user query           | ✅ |
| GET    | /assistant/history | Get conversation history  | ✅ |

---

## Legend
| Symbol | Meaning |
|--------|---------|
| ✅ | Implemented |
| ❌ | Not Implemented |
| ⏳ | In Progress |
