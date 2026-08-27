# Auth Service

A secure, permission-based JWT authentication and authorization service built with Spring Boot. It handles user registration, credential-based login, JWT issuance/validation, and group-based permission resolution, exposing a consistent, predictable JSON API for consuming applications.

## Description

`auth-service` provides a standalone authentication backend for use as part of a larger microservice ecosystem or as the identity layer for a single application. It issues stateless JWT access tokens on login, embeds the authenticated user's resolved permissions directly in the token, and validates those tokens on every subsequent request via a custom Spring Security filter. Authorization is permission-based rather than role-based: users belong to **groups**, groups are assigned **permissions**, and a user's effective permission set is resolved at login time and mapped to Spring Security `GrantedAuthority` objects, ready for `@PreAuthorize` checks on protected endpoints.

## Specifications

- **User Registration:** Register users with hashed passwords (BCrypt) and validated, unique `email`, `username`, and `phoneNumber`.
- **Flexible Login:** Authenticate with any one of `email`, `username`, or `phoneNumber`, plus a password.
- **JWT Generation:** Issue signed HS256 access tokens on successful login, embedding user identity and resolved permissions as claims.
- **Stateless JWT Validation:** A custom `OncePerRequestFilter` validates the `Authorization: Bearer <token>` header on every request and populates the Spring Security context — no server-side sessions.
- **Group-Based Permission Resolution:** Permissions are resolved through `User → UserGroup → Group → GroupPermission → Permission` at login time and embedded in the token.
- **Centralized Error Handling:** A `@RestControllerAdvice` global exception handler and a dedicated `AuthenticationEntryPoint` / `AccessDeniedHandler` produce a consistent JSON error envelope for validation errors, conflicts, invalid credentials, and unauthorized/forbidden access.
- **Consistent API Envelope:** Every endpoint returns `ApiResponse<T>` with `code`, `status`, `message`, and `data`.

## Prerequisites

- Java 21 (JDK)
- Maven Wrapper (bundled — do not use a system-installed Maven; always use `./mvnw`)
- PostgreSQL 13+ (or a compatible instance) reachable at the configured `spring.datasource.url`

## Technologies Used

- **Spring Boot 4 (Web MVC):** Backend framework for building the REST API (`spring-boot-starter-web`).
- **Spring Data JPA / Hibernate:** ORM for managing database operations (`spring-boot-starter-data-jpa`).
- **PostgreSQL:** Relational database.
- **Spring Security:** Stateless, filter-based authentication and method-level authorization (`spring-boot-starter-security`, `@EnableMethodSecurity`).
- **JJWT (`jjwt-api` / `jjwt-impl` / `jjwt-jackson`):** JWT generation, signing (HMAC-SHA), and parsing.
- **Bean Validation (`spring-boot-starter-validation`):** Declarative request DTO validation.
- **Lombok:** Boilerplate reduction (`@Getter`, `@Setter`, `@Builder`, constructors) on JPA entities.
- **Spring Boot DevTools:** Live reload for local development.
- **Maven:** Build and dependency management (via the wrapper, `./mvnw`).

## Project Structure

```
src/main/java/com/samora/authservice/
  AuthServiceApplication.java   # @SpringBootApplication entry point
  config/                       # Security config, JWT filter, authenticated-user principal
  exception/                    # Custom exceptions + centralized handlers
  utils/                        # JwtUtils — token parsing/validation
  common/                       # ApiResponse<T> envelope
  app/
    user/                       # User entity, service, repository, DTOs
    auth/                       # AuthController, AuthService, Group/Permission entities & repos
src/main/resources/
  application.properties        # Shared config, activates the `dev` profile
  application-dev.properties    # Local datasource, server port, JWT secret/expiry
```

## Configuration

Configuration is split across Spring profiles. Locally, the `dev` profile (`application-dev.properties`) is active by default and defines:

| Property | Description |
|---|---|
| `spring.datasource.url` | JDBC URL of the PostgreSQL database |
| `spring.datasource.username` / `password` | Database credentials |
| `spring.jpa.hibernate.ddl-auto` | Schema management strategy (`update` in dev) |
| `server.port` | HTTP port the application listens on |
| `app.jwt.secret` | HMAC signing key used to sign/verify JWTs |
| `app.jwt.expiration-days` | Access token lifetime, in days |

> **Note:** The committed `application-dev.properties` contains local development defaults only. For any shared or deployed environment, override these via a separate profile (e.g. `application-prod.properties`) or environment variables, and never commit real secrets or production credentials.

## Setup & Installation

1. Clone the repository:
   ```sh
   git clone https://github.com/YomZsamora/auth-service.git
   cd auth-service
   ```
2. Ensure PostgreSQL is running and a database matching `spring.datasource.url` in `application-dev.properties` exists (create it manually, or update the URL/credentials to match your local setup).
3. Build the project and run tests:
   ```sh
   ./mvnw clean install
   ```
4. Start the application:
   ```sh
   ./mvnw spring-boot:run
   ```
5. On startup, Hibernate (`ddl-auto=update`) will create/update the schema automatically. The API is available at `http://localhost:8082/`.
6. Interact with the API using cURL, Postman, or any HTTP client.

## API Reference

Base path: `/api/v1/auth`. All endpoints are `permitAll()` — no token required.

### `POST /api/v1/auth/basic-registration`

Registers a new user.

**Request body**
```json
{
  "username": "jdoe",
  "name": "Jane Doe",
  "phoneNumber": "+254712345678",
  "email": "jane@example.com",
  "password": "Passw0rd",
  "passwordConfirm": "Passw0rd"
}
```

**Response `201 Created`**
```json
{
  "code": 201,
  "status": "success",
  "message": "jane@example.com has been successfully registered.",
  "data": {
    "id": 1,
    "username": "jdoe",
    "name": "Jane Doe",
    "email": "jane@example.com",
    "phoneNumber": "+254712345678",
    "lastLogin": null,
    "createdAt": "2026-08-27T10:00:00Z",
    "updatedAt": "2026-08-27T10:00:00Z"
  }
}
```

### `POST /api/v1/auth/basic-login`

Authenticates a user with exactly **one** of `email`, `username`, or `phoneNumber`, plus `password`.

**Request body**
```json
{
  "email": "jane@example.com",
  "password": "Passw0rd"
}
```

**Response `200 OK`**
```json
{
  "code": 200,
  "status": "success",
  "message": "Logged in successfully.",
  "data": {
    "id": 1,
    "username": "jdoe",
    "name": "Jane Doe",
    "email": "jane@example.com",
    "phoneNumber": "+254712345678",
    "lastLogin": "2026-08-27T10:05:00Z",
    "createdAt": "2026-08-27T10:00:00Z",
    "updatedAt": "2026-08-27T10:05:00Z",
    "permissions": ["users.read", "users.write"],
    "accessToken": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```

### Authenticated requests

For every other route in the system, send the issued token as a bearer token:

```
Authorization: Bearer <accessToken>
```

Requests without a valid token receive `401 Unauthorized`; requests from an authenticated user lacking the required permission receive `403 Forbidden`. Both follow the standard `ApiResponse` error envelope.

### Error response shape

```json
{
  "code": 400,
  "status": "error",
  "message": "User registration failed due to validation errors.",
  "data": {
    "password": "Password must be at least 6 characters long."
  }
}
```

## Development

Want to contribute? Here's how:

- Fork the repo.
- Create a new branch (`git checkout -b feature-name`).
- Follow the architecture and coding conventions in `AGENTS.md` (feature-first packaging under `app/<name>/`, `ApiResponse<T>` envelopes, controller/service/repository separation, etc.).
- Make your changes and add/update tests.
- Commit using [Conventional Commits](https://www.conventionalcommits.org/) style (`feat:`, `fix:`, `refactor:`, `test:`, `docs:`).
- Push to your branch (`git push origin feature-name`).
- Open a Pull Request against `main` describing what changed and why.

## Running Tests

```sh
./mvnw test
```

## Roadmap

- Refresh token issuance and rotation.
- `@PreAuthorize` permission enforcement on protected endpoints (the security config and JWT filter already populate `GrantedAuthority` from token permissions, ready to be consumed).
- Dockerized local development (`Dockerfile` / `docker-compose.yml`).

## Known Bugs

If you encounter any bugs or issues while using the application, please open an issue on the [GitHub repository](https://github.com/YomZsamora/auth-service/issues). Be sure to include details of the issue and steps to reproduce it.

## License

MIT License

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
