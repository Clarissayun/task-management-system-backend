# Backend API Documentation

Spring Boot REST API for the Task Management System.

## Stack

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-%236DB33F.svg?style=for-the-badge&logo=spring-boot&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-%234ea94b.svg?style=for-the-badge&logo=mongodb&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-black?style=for-the-badge&logo=JSON%20web%20tokens)

## Written in

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=java&logoColor=white)

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+ (or use mvnw.cmd)
- MongoDB 6+

### Setup

1. Go to backend folder.

```bash
cd task-management-system-backend
```

2. Configure MongoDB and JWT/mail settings.

Use `src/main/resources/application.properties` and `src/main/resources/application-secrets.properties`.

### Email provider for OTP and email features

Email-related features depend on SMTP configuration:

- OTP request and verification flows
- Dev email test endpoints

For local development, Mailtrap is the default-friendly option.

Current config keys used by the app:

- `spring.mail.host`
- `spring.mail.port`
- `spring.mail.username`
- `spring.mail.password`

Environment/property fallback in `application.properties` points to Mailtrap host and port by default.
You can replace Mailtrap with Gmail or any SMTP provider by changing these values.

If SMTP is not configured correctly, API endpoints may still run, but OTP and email sending will fail.

3. Build and run.

```bash
mvnw.cmd clean package -DskipTests
java -jar target/task-management-system-backend-0.0.1-SNAPSHOT.jar
```

4. Base URL.

```text
http://localhost:8080/api
```

## Authentication and Access Rules

Public endpoints:

- POST `/api/auth/register`
- POST `/api/auth/login`
- POST `/api/auth/refresh`
- POST `/api/auth/otp/request`
- POST `/api/auth/otp/verify`
- POST `/api/auth/register/otp/request`
- POST `/api/auth/register/otp/verify`

Dev-only endpoints (only when `dev` profile is active):

- POST `/api/dev/email/test`
- POST `/api/dev/email/otp-test`
- POST `/api/dev/email/welcome-test`

All other endpoints require JWT Bearer token.

Header format:

```text
Authorization: Bearer <accessToken>
```

## DTO Contracts

### AuthResponse

Fields returned by auth-related endpoints:

- `message`
- `userId`
- `username`
- `email`
- `tokenType`
- `accessToken`
- `refreshToken`
- `accessTokenExpiresIn`
- `refreshTokenExpiresIn`

Note: `update-password` returns a shorter AuthResponse (`message`, `userId`, `username`, `email`) without new tokens.

### UserResponse

- `id`
- `username`
- `email`
- `avatar`
- `createdAt`

### ProjectResponse

- `id`
- `name`
- `description`
- `status`
- `startDate`
- `dueDate`
- `createdAt`
- `updatedAt`

### TaskResponse

- `id`
- `userId`
- `projectId`
- `title`
- `description`
- `dueDate`
- `status`
- `priority`
- `createdAt`
- `updatedAt`

## API Endpoints

### Auth Endpoints

#### POST /auth/register

Request body (`RegisterRequest`):

```json
{
  "username": "johndoe",
  "email": "johndoe@test.com",
  "password": "asdASD123123@"
}
```

Response: `201 Created`, body is `AuthResponse`.

#### POST /auth/login

Request body (`LoginRequest`):

```json
{
  "username": "johndoe",
  "password": "asdASD123123@"
}
```

Response: `200 OK`, body is `AuthResponse`.

#### POST /auth/refresh

Request body (`RefreshTokenRequest`):

```json
{
  "refreshToken": "<refresh-token>"
}
```

Response: `200 OK`, body is `AuthResponse`.

#### POST /auth/otp/request

Request body (`OtpRequest`):

```json
{
  "email": "johndoe@test.com"
}
```

Response: `200 OK`, plain string body.

#### POST /auth/otp/verify

Request body (`OtpVerifyRequest`):

```json
{
  "email": "johndoe@test.com",
  "otp": "123456"
}
```

Response: `200 OK`, body is `AuthResponse`.

#### POST /auth/register/otp/request

Request body (`RegisterRequest`):

```json
{
  "username": "johndoe",
  "email": "johndoe@test.com",
  "password": "asdASD123123@"
}
```

Response: `200 OK`, plain string body.

#### POST /auth/register/otp/verify

Request body (`OtpVerifyRequest`):

```json
{
  "email": "johndoe@test.com",
  "otp": "123456"
}
```

Response: `201 Created`, body is `AuthResponse`.

#### GET /auth/user/{userId}

Response: `200 OK`, body is `UserResponse`.

#### GET /auth/user/username/{username}

Response: `200 OK`, body is `UserResponse`.

#### PUT /auth/user/{userId}

Request body (`UserUpdateRequest`):

```json
{
  "username": "johndoe2",
  "email": "johndoe2@test.com"
}
```

Both fields are optional.

Response: `200 OK`, body is `UserResponse`.

#### POST /auth/update-password/{userId}

Request body (`PasswordUpdateRequest`):

```json
{
  "oldPassword": "oldPass123@",
  "newPassword": "newPass123@ABC"
}
```

Response: `200 OK`, body is AuthResponse with:

```json
{
  "message": "Password updated successfully",
  "userId": "...",
  "username": "...",
  "email": "..."
}
```

#### POST /auth/user/{userId}/avatar

Request type: `multipart/form-data` with field name `file`.

Response: `200 OK`, body is `UserResponse`.

#### DELETE /auth/user/{userId}/avatar

Response: `200 OK`, body is `UserResponse`.

### Project Endpoints

#### POST /projects

Request body (`ProjectRequest`):

```json
{
  "name": "Website Redesign",
  "description": "Update landing page and dashboard",
  "status": "ACTIVE",
  "startDate": "2026-04-01",
  "dueDate": "2026-06-30"
}
```

Required fields: `name`, `startDate`, `dueDate`.

Response: `201 Created`, body is `ProjectResponse`.

#### GET /projects

Optional query params:

- `status` (`ACTIVE`, `ARCHIVED`, `COMPLETED`)
- `search`
- `startDateFrom` (`YYYY-MM-DD`)
- `startDateTo` (`YYYY-MM-DD`)
- `dueDateFrom` (`YYYY-MM-DD`)
- `dueDateTo` (`YYYY-MM-DD`)

Response: `200 OK`, body is `ProjectResponse[]`.

#### GET /projects/paginated

Optional query params:

- `status`
- `search`
- `startDateFrom`
- `startDateTo`
- `dueDateFrom`
- `dueDateTo`
- `page`
- `size`
- `sort`

Response: `200 OK`, body is Spring `Page<ProjectResponse>` JSON.

#### GET /projects/status/{status}

Response: `200 OK`, body is `ProjectResponse[]`.

#### GET /projects/{projectId}

Response: `200 OK`, body is `ProjectResponse`.

#### PUT /projects/{projectId}

Request body (`ProjectRequest`) has same shape and validation as create.

Response: `200 OK`, body is `ProjectResponse`.

#### DELETE /projects/{projectId}

Response: `204 No Content`.

### Task Endpoints

#### POST /tasks

Optional query params:

- `userId` (optional, normally inferred from JWT)
- `projectId` (optional)

Request body (`TaskRequest`):

```json
{
  "title": "Write API docs",
  "description": "Document all endpoints",
  "priority": "MEDIUM",
  "projectId": "<optional-project-id>",
  "dueDate": "2026-04-28"
}
```

Required fields: `title`, `dueDate`.

Response: `201 Created`, body is `TaskResponse`.

Notes:

- Task status is set to `TODO` on create by service logic.
- If query `projectId` is present, it takes precedence over body `projectId`.

#### GET /tasks

Optional query params:

- `userId`
- `projectId`
- `status` (`TODO`, `IN_PROGRESS`, `DONE`)
- `priority` (`LOW`, `MEDIUM`, `HIGH`)
- `search`
- `dueDateFrom`
- `dueDateTo`

Response: `200 OK`, body is `TaskResponse[]`.

#### GET /tasks/paginated

Optional query params:

- `userId`
- `projectId`
- `status`
- `priority`
- `search`
- `dueDateFrom`
- `dueDateTo`
- `page`
- `size`
- `sort`

Response: `200 OK`, body is Spring `Page<TaskResponse>` JSON.

#### GET /tasks/status

Required query param:

- `status`

Optional query params:

- `userId`
- `projectId`
- `dueDateFrom`
- `dueDateTo`

Response: `200 OK`, body is `TaskResponse[]`.

#### GET /tasks/priority

Required query param:

- `priority`

Optional query params:

- `userId`
- `projectId`
- `dueDateFrom`
- `dueDateTo`

Response: `200 OK`, body is `TaskResponse[]`.

#### GET /tasks/{taskId}

Response: `200 OK`, body is `TaskResponse`.

#### PUT /tasks/{taskId}

Request body (`TaskRequest`) same as create.

Response: `200 OK`, body is `TaskResponse`.

#### PUT /tasks/{taskId}/status

Query param:

- `status` (`TODO`, `IN_PROGRESS`, `DONE`)

Example:

```http
PUT /tasks/{taskId}/status?status=DONE
```

Response: `200 OK`, body is `TaskResponse`.

#### DELETE /tasks/{taskId}

Response: `204 No Content`.

#### DELETE /tasks

Optional query params:

- `userId`
- `projectId`

Behavior:

- If `projectId` is present, deletes tasks for that project only.
- If `projectId` is absent, deletes standalone tasks (`projectId == null`) for that user.

Response: `204 No Content`.

### Dev Email Endpoints (dev profile only)

Base path: `/dev/email`

#### POST /dev/email/test?email=<email>

Response: `200 OK`, plain string on success.

#### POST /dev/email/otp-test?email=<email>

Response: `200 OK`, plain string on success.

#### POST /dev/email/welcome-test?email=<email>&name=<name>

Response: `200 OK`, plain string on success.

## Validation Rules from DTOs

### RegisterRequest

- `username`: required, 3-30 chars
- `email`: required, valid email, max 254 chars
- `password`: required, 12-128 chars

### LoginRequest

- `username`: required
- `password`: required

### OtpVerifyRequest

- `email`: required, valid email, max 254 chars
- `otp`: required, exactly 6 digits

### UserUpdateRequest

- `username`: optional, 3-30 chars when present
- `email`: optional, valid email, max 254 chars when present

### PasswordUpdateRequest

- `oldPassword`: required
- `newPassword`: required, 12-128 chars

### ProjectRequest

- `name`: required
- `startDate`: required
- `dueDate`: required
- `description`: optional
- `status`: optional (`ACTIVE`, `ARCHIVED`, `COMPLETED`)

### TaskRequest

- `title`: required, max 120 chars
- `description`: optional, max 2000 chars
- `priority`: optional (`LOW`, `MEDIUM`, `HIGH`)
- `projectId`: optional, max 100 chars
- `dueDate`: required

## Error Response Format

The global exception handler returns this shape:

```json
{
  "timestamp": "2026-04-21T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "fields": {
    "email": "Email must be a valid email address"
  },
  "path": "/api/auth/register"
}
```

Notes:

- `fields` is present for validation errors (`MethodArgumentNotValidException`).
- Other handled exceptions return: `timestamp`, `status`, `error`, `message`, `path`.

## Data Model Snapshot

### User

- `id`
- `username`
- `email`
- `password`
- `avatar`
- `createdAt`
- `updatedAt`

### Project

- `id`
- `userId`
- `name`
- `description`
- `status`
- `startDate`
- `dueDate`
- `createdAt`
- `updatedAt`

### Task

- `id`
- `userId`
- `projectId`
- `title`
- `description`
- `dueDate`
- `status`
- `priority`
- `createdAt`
- `updatedAt`

## Quick Local Test Examples

```bash
# Register (direct)
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","password":"StrongPass123@"}'

# Login (username + password)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"StrongPass123@"}'
```

## Version

- Version: 0.0.1-SNAPSHOT
- Framework: Spring Boot 3.x
- Java: 17+
- Database: MongoDB
