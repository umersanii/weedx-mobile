# API Testing Guide

## Server Setup
```bash
cd xampp/htdocs/backend && php -S 0.0.0.0:8000
```

---

## Auth Endpoints

### POST /api/auth/login.php

**Valid Login:**
```bash
curl -s -X POST http://localhost:8000/api/auth/login.php \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@weedx.com", "password": "admin123", "firebaseToken": "test-token"}'
```
✅ Returns: `200` + JWT token + user data

**Wrong Password:**
```bash
curl -s -X POST http://localhost:8000/api/auth/login.php \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@weedx.com", "password": "wrong", "firebaseToken": "test-token"}'
```
✅ Returns: `401` - `"Invalid credentials"`

**Non-existent User:**
```bash
curl -s -X POST http://localhost:8000/api/auth/login.php \
  -H "Content-Type: application/json" \
  -d '{"email": "nobody@weedx.com", "password": "test", "firebaseToken": "test-token"}'
```
✅ Returns: `401` - `"Invalid credentials"`

**Missing Fields:**
```bash
curl -s -X POST http://localhost:8000/api/auth/login.php \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@weedx.com"}'
```
✅ Returns: `400` - Lists missing fields

---

## Test Credentials

| User | Email | Password |
|------|-------|----------|
| Admin | admin@weedx.com | admin123 |

---

## Profile Endpoints

### GET /api/profile/get.php
Requires: `Authorization: Bearer <token>`

**Get Profile (with token):**
```bash
curl -s http://localhost:8000/api/profile/get.php \
  -H "Authorization: Bearer $TOKEN" | jq .
```
✅ Returns: `200` + user, farm, settings data

**No Token:**
```bash
curl -s http://localhost:8000/api/profile/get.php
```
✅ Returns: `401` - `"Authorization token required"`

### PUT /api/profile/update.php
Requires: `Authorization: Bearer <token>`

**Update Profile:**
```bash
curl -s -X PUT http://localhost:8000/api/profile/update.php \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "New Name", "phone": "1234567890"}'
```
✅ Returns: `200` - `"Profile updated successfully"`

**Empty Update:**
```bash
curl -s -X PUT http://localhost:8000/api/profile/update.php \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{}'
```
✅ Returns: `400` - `"No fields to update"`

---

## Test Results Summary

| Endpoint | Test | Status | Date |
|----------|------|--------|------|
| `/api/auth/login.php` | Valid login | ✅ Pass | 2025-11-28 |
| `/api/auth/login.php` | Wrong password | ✅ Pass | 2025-11-28 |
| `/api/auth/login.php` | Non-existent user | ✅ Pass | 2025-11-28 |
| `/api/auth/login.php` | Missing fields | ✅ Pass | 2025-11-28 |
| `/api/profile/get.php` | Get with token | ✅ Pass | 2025-11-28 |
| `/api/profile/get.php` | Get without token | ✅ Pass | 2025-11-28 |
| `/api/profile/update.php` | Update fields | ✅ Pass | 2025-11-28 |
| `/api/profile/update.php` | Empty update | ✅ Pass | 2025-11-28 |
