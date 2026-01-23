# JWT and Refresh Token Authentication - Frontend Implementation Guide

**Version:** 1.0
**Last Updated:** 2025-12-16
**Backend Service:** jbh-iam (Identity and Access Management)

---

## Overview

This document describes the complete JWT and refresh token authentication lifecycle for the jbh-iam backend service.

Use this guide to verify that the frontend application correctly implements all required authentication flows.

### Architecture Summary

- **Authentication Method:** JWT (JSON Web Tokens) with Refresh Token Rotation
- **Token Storage:** HttpOnly, Secure cookies (automatic browser management)
- **Security Standard:** OAuth 2.0 compliant with token rotation
- **Session Strategy:** Multiple concurrent sessions allowed (one per device/browser)

---

## Core Concepts

### 1. Access Token (JWT)
- **Purpose:** Short-lived token for API authentication
- **Lifetime:** 15 minutes (900 seconds)
- **Storage:** HttpOnly cookie named `JBH_TOKEN` (local) or `__Secure-JBH_TOKEN` (production)
- **Usage:** Automatically sent with every API request by the browser
- **Format:** JWT Bearer token

### 2. Refresh Token
- **Purpose:** Long-lived token to obtain new access tokens
- **Lifetime:** 30 days (2,592,000 seconds)
- **Storage:** HttpOnly cookie named `JBH_REFRESH_TOKEN` (local) or `__Secure-JBH_REFRESH_TOKEN` (production)
- **Security:** Stored hashed in database, single-use (rotates on each refresh)
- **Tracking:** Includes device info and IP address for security auditing

### 3. Token Rotation
When a refresh token is used to get a new access token:
1. The old refresh token is **revoked** (becomes invalid)
2. A **new** refresh token is issued
3. A **new** access token is issued
4. Both new tokens are returned to the client

**Security Benefit:** If a refresh token is stolen, it can only be used once. The next legitimate refresh attempt will fail, alerting the system to potential compromise.

---

## API Endpoints

Base URL: `http://localhost:9999/auth` (local) or your production domain

### 1. Sign In (Login)

**Endpoint:** `POST /auth/signin`

**Request Body:**
```json
{
  "usernameOrEmail": "string",
  "password": "string"
}
```

**Success Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "tokenType": "Bearer",
  "error": null
}
```

**Error Response (401 Unauthorized):**
```json
{
  "accessToken": null,
  "refreshToken": null,
  "tokenType": "Bearer",
  "error": "User not found"
}
```

**Cookies Set:**
- `JBH_TOKEN` or `__Secure-JBH_TOKEN` (access token, 15 min)
- `JBH_REFRESH_TOKEN` or `__Secure-JBH_REFRESH_TOKEN` (refresh token, 30 days)

**Frontend Implementation Checklist:**
- [ ] Send credentials in request body
- [ ] Handle 200 response: tokens are in cookies (browser manages automatically)
- [ ] Handle 401 response: show error message to user
- [ ] Do NOT manually store tokens in localStorage/sessionStorage (cookies are HttpOnly)
- [ ] After successful login, redirect to /home

---

### 2. Refresh Access Token

**Endpoint:** `POST /auth/refresh`

**Request Body:** None (refresh token read from cookie automatically)

**Success Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "z9y8x7w6-v5u4-3210-zyxw-vu9876543210",
  "tokenType": "Bearer",
  "error": null
}
```

**Error Response (401 Unauthorized):**
```json
{
  "accessToken": null,
  "refreshToken": null,
  "tokenType": "Bearer",
  "error": "Invalid or expired refresh token"
}
```

**Cookies Updated:**
- `JBH_TOKEN` or `__Secure-JBH_TOKEN` (new access token, 15 min)
- `JBH_REFRESH_TOKEN` or `__Secure-JBH_REFRESH_TOKEN` (new refresh token, 30 days)

**Frontend Implementation Checklist:**
- [ ] Call this endpoint when receiving 401 from protected endpoints
- [ ] Implement automatic retry logic for failed requests after token refresh
- [ ] Handle 401 from refresh endpoint: redirect to login (session expired)
- [ ] Do NOT manually extract/send refresh token (browser handles cookie automatically)
- [ ] Update any stored access token display values

**Important Notes:**
- Old refresh token becomes invalid after successful refresh (token rotation)
- Do NOT retry failed refresh requests (indicates session expiry or security issue)
- Each refresh call returns BOTH new access token AND new refresh token

---

### 3. Logout

**Endpoint:** `POST /auth/logout`

**Request Body:** None

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Logged out successfully"
}
```

**Error Response (500 Internal Server Error):**
```json
{
  "success": false,
  "message": "Logout failed"
}
```

**Cookies Cleared:**
- Both `JBH_TOKEN` and `__Secure-JBH_TOKEN` removed
- Both `JBH_REFRESH_TOKEN` and `__Secure-JBH_REFRESH_TOKEN` removed

**Backend Actions:**
1. Extracts refresh token from cookie
2. Revokes refresh token in database (prevents reuse)
3. Invalidates server session
4. Clears Spring Security context
5. Clears both cookies

**Frontend Implementation Checklist:**
- [ ] Call this endpoint when user clicks logout button
- [ ] Clear any client-side state (user info, preferences, etc.)
- [ ] Redirect to login page after successful logout
- [ ] Handle errors gracefully (still redirect to login even if logout fails)
- [ ] Do NOT manually delete cookies (backend handles this)

---

### 4. Sign Up (Register)

**Endpoint:** `POST /auth/signup`

**Request Body:**
```json
{
  "username": "string",
  "email": "string",
  "password": "string"
}
```

**Success Response (201 Created):**
```json
{
  "success": true,
  "message": "User registered successfully"
}
```

**Error Response (400 Bad Request):**
```json
{
  "success": false,
  "message": "Email address already in use"
}
```

**Frontend Implementation Checklist:**
- [ ] Validate input fields before sending
- [ ] Handle 201 response: redirect to login page
- [ ] Handle 400 response: display error message to user
- [ ] Do NOT auto-login after signup (require explicit login)

---

### 5. Check Username Availability

**Endpoint:** `GET /auth/checkUsernameAvailability?nickName={username}`

**Success Response (200 OK):**
```json
{
  "available": true
}
```

**Frontend Implementation Checklist:**
- [ ] Call during registration form validation
- [ ] Show real-time feedback (e.g., "Username available" or "Username taken")
- [ ] Debounce requests (wait 300-500ms after user stops typing)

---

### 6. Check Email Availability

**Endpoint:** `GET /auth/checkEmailAvailability?email={email}`

**Success Response (200 OK):**
```json
{
  "available": false
}
```

**Frontend Implementation Checklist:**
- [ ] Call during registration form validation
- [ ] Show real-time feedback (e.g., "Email available" or "Email already registered")
- [ ] Debounce requests (wait 300-500ms after user stops typing)

---

### 7. Get Current User Info

**Endpoint:** `GET /users/userinfo`

**Authentication:** Requires valid access token (JWT cookie)

**Success Response (200 OK):**
```json
{
  "id": "uuid",
  "username": "string",
  "email": "string",
  "roles": ["USER"]
}
```

**Error Response (401 Unauthorized):**
If access token is expired or invalid.

**Frontend Implementation Checklist:**
- [ ] Call this endpoint after login to get user details
- [ ] Call on app initialization to check if user is still logged in
- [ ] Handle 401: automatically call `/auth/refresh` to get new tokens
- [ ] If refresh fails, redirect to login

---

## Complete Authentication Flow

### Initial Login Flow

```
User enters credentials
        ↓
POST /auth/signin
        ↓
Backend validates credentials
        ↓
Backend creates refresh token (stored in DB)
        ↓
Backend generates JWT access token
        ↓
Backend sets two HttpOnly cookies:
  - JBH_TOKEN (access token, 15 min)
  - JBH_REFRESH_TOKEN (refresh token, 30 days)
        ↓
Frontend receives response
        ↓
Frontend redirects to dashboard
```

---

### Protected API Request Flow

```
Frontend makes API request
        ↓
Browser automatically includes JBH_TOKEN cookie
        ↓
Backend validates JWT from cookie
        ↓
┌──────────────────────────────────┐
│ Valid JWT?                       │
├──────────────────────────────────┤
│ Yes → Process request → 200      │
│ No  → Return 401                 │
└──────────────────────────────────┘
```

---

### Token Refresh Flow (Access Token Expired)

```
API request with expired access token
        ↓
Backend returns 401 Unauthorized
        ↓
Frontend intercepts 401 response
        ↓
Frontend calls POST /auth/refresh
        ↓
Browser sends JBH_REFRESH_TOKEN cookie
        ↓
Backend validates refresh token
        ↓
Backend revokes old refresh token (token rotation)
        ↓
Backend creates NEW refresh token
        ↓
Backend generates NEW access token
        ↓
Backend sets updated cookies
        ↓
Frontend receives new tokens
        ↓
Frontend retries original API request
        ↓
Request succeeds with new access token
```

---

### Logout Flow

```
User clicks logout button
        ↓
Frontend calls POST /auth/logout
        ↓
Backend revokes refresh token in database
        ↓
Backend invalidates session
        ↓
Backend clears Security context
        ↓
Backend clears both cookies
        ↓
Frontend redirects to login page
```

---

## Cookie Configuration

### Development/Local Environment

| Cookie Name             | Value        | HttpOnly | Secure | SameSite | MaxAge      |
|------------------------|--------------|----------|--------|----------|-------------|
| `JBH_TOKEN`            | JWT string   | true     | false  | Lax      | 900s (15m)  |
| `JBH_REFRESH_TOKEN`    | UUID string  | true     | false  | Lax      | 2592000s (30d) |

### Production Environment

| Cookie Name             | Value        | HttpOnly | Secure | SameSite | MaxAge      |
|------------------------|--------------|----------|--------|----------|-------------|
| `__Secure-JBH_TOKEN`   | JWT string   | true     | true   | None     | 900s (15m)  |
| `__Secure-JBH_REFRESH_TOKEN` | UUID string  | true     | true   | None     | 2592000s (30d) |

**Key Differences:**
- **Production** uses `__Secure-` prefix (HTTPS required)
- **Production** sets `Secure=true` (cookies only sent over HTTPS)
- **Production** sets `SameSite=None` (allows cross-origin with HTTPS)
- **Local** uses `SameSite=Lax` (works with HTTP on localhost)

---

## Frontend Implementation Requirements

### 1. HTTP Client Configuration

**Required Settings:**
```javascript
// Axios example
axios.defaults.withCredentials = true;

// Fetch example
fetch(url, {
  credentials: 'include'
});
```

**Why:** Ensures cookies are sent with cross-origin requests.

---

### 2. Automatic Token Refresh Interceptor

The frontend MUST implement an HTTP interceptor to handle token refresh automatically.

**Pseudocode:**
```javascript
// Response interceptor
onResponse401Error(error) {
  const originalRequest = error.config;

  // Prevent infinite retry loops
  if (originalRequest._retry) {
    // Redirect to login (session expired)
    redirectToLogin();
    return Promise.reject(error);
  }

  originalRequest._retry = true;

  // Try to refresh tokens
  return refreshToken()
    .then(() => {
      // Retry original request with new tokens (in cookies)
      return httpClient(originalRequest);
    })
    .catch(() => {
      // Refresh failed, session expired
      redirectToLogin();
      return Promise.reject(error);
    });
}

function refreshToken() {
  return httpClient.post('/auth/refresh');
}
```

**Frontend Implementation Checklist:**
- [ ] Implement response interceptor for 401 errors
- [ ] Prevent infinite retry loops (use `_retry` flag)
- [ ] Call `/auth/refresh` on 401
- [ ] Retry original request if refresh succeeds
- [ ] Redirect to login if refresh fails
- [ ] Handle concurrent requests (queue them during refresh)

---

### 3. Session Persistence Check

On application initialization, check if user is still authenticated:

**Pseudocode:**
```javascript
async function initializeApp() {
  try {
    const userInfo = await getUserInfo(); // GET /users/userinfo
    setCurrentUser(userInfo);
    redirectToDashboard();
  } catch (error) {
    // No valid session, redirect to login
    redirectToLogin();
  }
}
```

**Frontend Implementation Checklist:**
- [ ] Call `/users/userinfo` on app load
- [ ] If successful: user is authenticated, proceed to app
- [ ] If 401: automatically try `/auth/refresh`
- [ ] If refresh succeeds: retry `/users/userinfo`
- [ ] If refresh fails: redirect to login

---

### 4. Security Best Practices

**DO:**
- ✅ Use `credentials: 'include'` for all API requests
- ✅ Let the browser manage cookies automatically
- ✅ Implement automatic token refresh on 401 errors
- ✅ Clear client-side state on logout
- ✅ Use HTTPS in production
- ✅ Handle refresh failures gracefully (redirect to login)

**DON'T:**
- ❌ Store tokens in localStorage or sessionStorage
- ❌ Manually extract tokens from cookies using JavaScript
- ❌ Send tokens in Authorization header (cookies do this automatically)
- ❌ Retry failed refresh requests
- ❌ Implement custom cookie management

---

## Error Handling Matrix

| Endpoint | Status Code | Error Message | Frontend Action |
|----------|-------------|---------------|-----------------|
| `/auth/signin` | 401 | "User not found" | Show error, stay on login page |
| `/auth/refresh` | 401 | "Invalid or expired refresh token" | Redirect to login |
| `/auth/logout` | 500 | "Logout failed" | Clear state, redirect to login anyway |
| `/users/userinfo` | 401 | - | Try refresh, if fails redirect to login |
| Any protected endpoint | 401 | - | Try refresh, if succeeds retry request |

---

## Security Features

### 1. HttpOnly Cookies
- Prevents JavaScript access (XSS protection)
- Browser automatically manages sending/receiving
- Cannot be stolen via XSS attacks

### 2. Secure Flag (Production)
- Cookies only sent over HTTPS
- Prevents man-in-the-middle attacks

### 3. SameSite Protection
- `Lax` (local): CSRF protection for state-changing requests
- `None` (production): Cross-origin support with HTTPS

### 4. Refresh Token Rotation
- Each refresh generates new tokens
- Old refresh token immediately revoked
- Detects token theft (legitimate user's refresh will fail)

### 5. Token Hashing
- Refresh tokens hashed before database storage
- Even if database is compromised, tokens are useless

### 6. Device Tracking
- Each refresh token tracks device info and IP
- Enables security auditing and session management
- Future feature: "Active Sessions" page for users

### 7. Automatic Cleanup
- Scheduled task removes expired refresh tokens
- Prevents database bloat
- Reduces attack surface

---

## Testing Checklist

### Basic Authentication Flow
- [ ] User can sign up successfully
- [ ] User can log in successfully
- [ ] Cookies are set after login
- [ ] User can access protected endpoints after login
- [ ] User can log out successfully
- [ ] Cookies are cleared after logout

### Token Refresh Flow
- [ ] Access token expires after 15 minutes
- [ ] Refresh automatically happens on 401
- [ ] Original request retries after refresh
- [ ] New tokens are received after refresh
- [ ] Old refresh token becomes invalid after refresh

### Session Expiry
- [ ] After 30 days, refresh token expires
- [ ] User is redirected to login when refresh fails
- [ ] User can log in again after session expiry

### Multiple Sessions
- [ ] User can log in from multiple devices
- [ ] Each device has its own refresh token
- [ ] Logging out from one device doesn't affect others
- [ ] Each device can refresh independently

### Security
- [ ] Tokens are not accessible via JavaScript
- [ ] Tokens are sent automatically with requests
- [ ] HTTPS is enforced in production
- [ ] Cross-origin requests work correctly

### Error Handling
- [ ] Invalid credentials show error message
- [ ] Expired session redirects to login
- [ ] Network errors are handled gracefully
- [ ] Concurrent requests during refresh are queued

---

## Common Frontend Mistakes to Avoid

### 1. ❌ Storing Tokens in localStorage
```javascript
// WRONG - Security vulnerability
localStorage.setItem('accessToken', response.data.accessToken);
```
**Why wrong:** Vulnerable to XSS attacks. Use HttpOnly cookies instead.

### 2. ❌ Manually Setting Authorization Header
```javascript
// WRONG - Not needed with cookies
axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
```
**Why wrong:** Cookies are sent automatically. This creates redundancy and confusion.

### 3. ❌ Forgetting `credentials: 'include'`
```javascript
// WRONG - Cookies won't be sent
fetch('http://localhost:9999/users/userinfo');
```
**Correct:**
```javascript
fetch('http://localhost:9999/users/userinfo', { credentials: 'include' });
```

### 4. ❌ Retrying Failed Refresh Requests
```javascript
// WRONG - Creates security issues
if (refreshError) {
  retryRefresh(); // Don't do this!
}
```
**Why wrong:** Failed refresh means session expired or security breach. Redirect to login instead.

### 5. ❌ Not Implementing Refresh Interceptor
Without automatic refresh, users must log in every 15 minutes (access token lifetime).

### 6. ❌ Manually Deleting Cookies on Logout
```javascript
// WRONG - Backend handles this
document.cookie = 'JBH_TOKEN=; expires=Thu, 01 Jan 1970 00:00:00 UTC;';
```
**Why wrong:** Backend clears cookies and revokes tokens. Frontend just needs to call `/auth/logout`.

---

## Environment Variables (Backend Reference)

Frontend developers should be aware of these backend configurations:

| Variable | Default | Description |
|----------|---------|-------------|
| `INSTANCE_PORT` | 9999 | Backend server port |
| `INSTANCE_CONTEXT_PATH` | `/` | Base path for all endpoints |
| `JWT_EXPIRATION_IN_MS` | 604800000 | JWT expiration (7 days in config, but cookies expire in 15 min) |
| `app.refreshTokenExpirationInMs` | 2592000000 | Refresh token expiration (30 days) |
| `CONSUL_PORT` | 8500 | Service discovery port |

---

## Troubleshooting

### Issue: Cookies not being sent with requests

**Solution:**
1. Ensure `credentials: 'include'` is set
2. Check CORS configuration on backend
3. Verify frontend and backend are on same domain or CORS is properly configured

### Issue: 401 errors on all requests after login

**Solution:**
1. Check if cookies are being set (browser DevTools → Application → Cookies)
2. Verify cookie names match environment (`JBH_TOKEN` vs `__Secure-JBH_TOKEN`)
3. Ensure `withCredentials: true` in HTTP client

### Issue: Infinite refresh loop

**Solution:**
1. Ensure `_retry` flag is set to prevent multiple refresh attempts
2. Check if `/auth/refresh` endpoint is excluded from retry logic
3. Verify refresh error redirects to login instead of retrying

### Issue: User logged out immediately after login

**Solution:**
1. Check if token refresh is happening correctly
2. Verify backend clock is synchronized (JWT timestamp validation)
3. Check browser console for CORS or cookie errors

---

## References

- OAuth 2.0 RFC: https://tools.ietf.org/html/rfc6749
- JWT Best Practices: https://tools.ietf.org/html/rfc8725
- OWASP Cookie Security: https://owasp.org/www-community/controls/SecureCookieAttribute

---

## Change Log

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2025-12-16 | Initial documentation with refresh token rotation |

---

## AI Assistant Verification Prompt

Use this prompt to verify frontend implementation:

> "Review the frontend authentication implementation and verify:
> 1. All API endpoints from this doc are correctly called
> 2. HTTP client has `credentials: 'include'` configured
> 3. Response interceptor handles 401 with automatic refresh
> 4. No tokens are stored in localStorage/sessionStorage
> 5. Logout clears client state and redirects to login
> 6. Session persistence check on app initialization
> 7. Error handling matches the error matrix
> 8. No manual cookie management is implemented
>
> Report any missing implementations or security issues found."
