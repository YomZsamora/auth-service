# AGENTS.md — auth-service

This file provides guidance for AI coding agents (e.g. GitHub Copilot, Claude, Codex) working on this project.

## Project Overview

`auth-service` is a Spring Boot application that provides OAuth2-based authentication and user management features, ensuring secure access.

## Tech Stack

- **Language:** Java 21
- **Framework:** Spring Boot 4.0.6
- **Web layer:** Spring Web MVC (`spring-boot-starter-web`)
- **Persistence:** Spring Data JPA (`spring-boot-starter-data-jpa`) + PostgreSQL (`postgresql`)
- **Validation:** Bean Validation (`spring-boot-starter-validation`)
- **Utilities:** Lombok (`lombok`) — used for `@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` on entities
- **Build tool:** Maven (use the Maven wrapper `./mvnw`, never a system-installed Maven)
- **Testing:** JUnit / Spring Boot Test (`spring-boot-starter-test`)
- **Dev tooling:** Spring Boot DevTools (`spring-boot-devtools`)

## Repository Layout

```
src/
  main/
    java/com/samora/authservice/
      AuthServiceApplication.java         # @SpringBootApplication entry point
      config/                             # @Configuration classes
      exception/                          # Custom exceptions + GlobalExceptionHandler
      utils/                              # Stateless helper/utility classes
      common/                             # Shared DTOs, enums, constants
      app/
        user/                            # User entity, controller, service, repository, DTOs
        auth/                            # Authentication entity, controller, service, repository, DTOs
    resources/
      application.properties
      application-dev.properties
      application-prod.properties
      static/                             # CSS, JS, images
  test/
    java/com/samora/authservice/
      app/
        user/
        auth/
      AuthServiceApplicationTests.java
```

## Architecture Rules

1. **Feature-first packaging** — all code for a feature (controller, service, repository, entity, DTO, mapper) lives under `app/<name>/`.
2. **REST controller** (`@RestController`) — JSON API endpoints only; wrap every response in `ApiResponse<T>` and return `ResponseEntity`; no business logic.
3. **Service interface + Impl** — business logic lives exclusively in `*ServiceImpl`; always inject the interface, never the implementation.
4. **Repository** (`@Repository`) — extends `JpaRepository`; use Spring Data method derivation before writing custom JPQL.
5. **Cross-cutting code** — `config/`, `exception/`, `utils/`, `common/` at the top package level.

## Coding Conventions

| Subject | Rule                                                                                         |
|---------|----------------------------------------------------------------------------------------------|
| Class / interface names | `PascalCase`                                                                                 |
| Interface prefix | None (`AuthService`, **not** `IAuthService`)                                                 |
| Implementation suffix | `Impl` (`AuthServiceImpl`)                                                                   |
| Exception suffix | `Exception` (`ResourceNotFoundException`)                                                    |
| Test suffix | `Test` (`AuthControllerTest`)                                                                |
| Methods & variables | `camelCase`, verb-first (`createUser`, `updateUser`, `getUserById`)                          |
| Boolean methods | `is*`, `has*`, `can*` prefix                                                                 |
| Constants | `UPPER_SNAKE_CASE`                                                                           |
| Packages | lowercase, reverse domain, underscores only where the artifact name requires (`authservice`) |

## Database Naming Conventions

| Subject | Rule | Example                      |
|---------|------|------------------------------|
| Table names | `snake_case`, plural | `group_permissions`, `users` |
| Column names | `snake_case`, singular | `profile_image`, `username`  |
| Primary keys | `id` | `id`                         |
| Foreign keys | `referenced_table_singular_id` | `user_id`, `group_id`        |

- Use `@Table(name = "...")` on any entity where the class name doesn't match the plural `snake_case` table name (e.g. `User` → `@Table(name = "users")`).
- Hibernate's default naming strategy automatically converts `camelCase` Java field names to `snake_case` column names — only use `@Column(name = "...")` when you need to override this or set constraints (`nullable`, `length`, etc.).
- Never use reserved SQL keywords as table names (e.g. `ORDER`, `GROUP`, `USER`) — prefix or rename instead (`user_interests`, `user_accounts`).

## API Response Conventions


### API Response Conventions

All REST endpoints (`@RestController`) must return responses wrapped in `ApiResponse<T>` (from the `common/` package) and use `ResponseEntity` for HTTP status control. This ensures a consistent, predictable, and client-friendly API contract.

#### Standard Response Structure

Every API response must follow this JSON structure:

```json
{
  "code": 200,
  "status": "success",
  "message": "User retrieved successfully.",
  "data": { ... }
}
```

- `code`: The HTTP status code (e.g., 200, 201, 400, 404, 500)
- `status`: "success" or "error"
- `message`: Human-readable summary of the result
- `data`: The response payload (object, array, or `null`). This field is always present, even for errors or empty results.

#### Factory Methods for `ApiResponse<T>`

| Method                                      | When to Use                                 |
|----------------------------------------------|---------------------------------------------|
| `ApiResponse.success(code, message, data)`   | Successful response with a body             |
| `ApiResponse.success(code, message)`         | Successful response with no body (e.g. delete) |
| `ApiResponse.error(code, message, null)`     | Error response (data is always `null`)      |

#### Serialization Rules

- The `data` field is always serialized (`@JsonInclude(ALWAYS)`), even when its value is `null`.

#### HTTP Status Codes

| Status Code | When Used                                 |
|-------------|-------------------------------------------|
| 201 Created | Resource created (POST)                   |
| 200 OK      | Successful read, update, or delete        |
| 400 Bad Request | Validation failure or malformed body   |
| 404 Not Found | Resource not found                      |
| 500 Internal Server Error | Unhandled exception          |

#### Validation Error Example

For validation errors, the `data` field contains a map of field names to error messages:

```json
{
  "code": 400,
  "status": "error",
  "message": "User registration failed due to validation errors.",
  "data": {
    "phoneNumber": "Phone number provided is not valid.",
    "password": "Password must be at least 6 characters long.",
    ...
  }
}
```

## Global Exception Handling


### Global Exception Handling

All exception-to-API-response mapping is centralized in `GlobalExceptionHandler` (`@RestControllerAdvice`) under the `exception/` package. Controllers must never catch exceptions directly; instead, they should allow exceptions to propagate so that consistent error responses are generated.

#### Registered Exception Handlers

| Exception Type                        | HTTP Status | Response Behavior                                                                 |
|---------------------------------------|-------------|-----------------------------------------------------------------------------------|
| `MethodArgumentNotValidException`     | 400         | Returns field-level validation errors as a `Map<String, String>` in the `data` field |
| `HttpMessageNotReadableException`     | 400         | Indicates malformed or unparseable request body; returns `data: null`                |
| `Exception` (catch-all)               | 500         | Generic fallback for unhandled exceptions; returns `data: null`                      |

#### Validation Message Resolution Protocol

When adding a new request record type, you must register a new `case` in the `resolveValidationMessage` method within `GlobalExceptionHandler`. The key should be the camelCase simple name of the request record (e.g., `"tacoCreateRequest"`). This ensures that validation error responses are domain-specific and user-friendly.

## Validation & Request DTO Conventions

- Request DTOs are Java **`record`** types, not classes.
- Name pattern: `Create*Request` (POST), `Update*Request` (PATCH/PUT).
- Bean Validation annotations (`@NotNull`, `@Size`, `@NotBlank`, etc.) go directly on record components.
- The `@Valid` annotation is placed on the `@RequestBody` parameter in the controller method — never inside the service.
- The service never re-validates; it trusts that the controller layer has already validated the incoming data.

**Example:**
```java
public record CreateVenueRequest(

        @NotBlank(message = "Venue name is required.")
        @Size(min = 3, max = 100, message = "Venue name must be between 3 and 100 characters.")
        @Pattern(regexp = "^[a-zA-Z\\s\\-&'.]+$", message = "Venue name must contain only letters and common punctuation.")
        String venueName,

        @NotBlank(message = "Email is required.")
        @Email(message = "A valid email address is required.")
        @Pattern(regexp = "^[a-zA-Z0-9._%+\\-]+@(?!\\[)[a-zA-Z0-9\\-]+(\\.[a-zA-Z0-9\\-]+)*\\.[a-zA-Z]{2,6}$", message = "Email must have a valid domain and recognized top-level domain.")
        String email,

        @NotBlank(message = "Phone number must not be blank.")
        @Size(max = 13, message = "Phone number must not exceed 13 characters.")
        @Pattern(regexp = "^\\+?[0-9]{7,12}$", message = "Phone number must contain only digits and may start with '+'.")
        String phoneNumber,

        @NotBlank(message = "Address is required.")
        @Size(min = 5, max = 255, message = "Address must be between 5 and 255 characters.")
        String address,

        @NotBlank(message = "Opening hour is required.")
        @Pattern(regexp = "^([01]\\d|2[0-3]):([0-5]\\d):([0-5]\\d)$", message = "Opening hours must be in format HH:mm:ss.")
        String openHours,

        @NotBlank(message = "Closing hour is required.")
        @Pattern(regexp = "^([01]\\d|2[0-3]):([0-5]\\d):([0-5]\\d)$", message = "Closing hours must be in format HH:mm:ss.")
        String closingHours,

        @NotNull(message = "Latitude is required.")
        @DecimalMin(value = "-90.0", message = "Latitude must be a decimal between -90 and 90.")
        @DecimalMax(value = "90.0",  message = "Latitude must be a decimal between -90 and 90.")
        BigDecimal latitude,

        @NotNull(message = "Longitude is required.")
        @DecimalMin(value = "-180.0", message = "Longitude must be a decimal between -180 and 180.")
        @DecimalMax(value = "180.0",  message = "Longitude must be a decimal between -180 and 180.")
        BigDecimal longitude,

        @Positive(message = "Capacity must be a positive number.")
        @Max(value = 100_000, message = "Capacity must not exceed 100,000.")
        Integer capacity,

        @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters.")
        String description,

        @Pattern(regexp = "^https?://(([a-zA-Z0-9\\-]+\\.)+[a-zA-Z]{2,}|(\\d{1,3}\\.){3}\\d{1,3})(:\\d{1,5})?(/[^\\s]*)?$", message = "Website must be a valid http or https URL.")
        @Size(max = 255, message = "Website URL must not exceed 255 characters.")
        String website,

        @DecimalMin(value = "0.0", inclusive = false, message = "Average budget must be a positive value.")
        @Digits(integer = 8, fraction = 2, message = "Average budget must have at most 2 decimal places.")
        BigDecimal averageBudget,

        @Pattern(regexp = "^(true|false)$", message = "Must be a boolean value (true or false).")
        String allowsDirectBookings,

        List<@Pattern(
                regexp = "^https?://[\\w\\-]+(\\.[\\w\\-]+)+.*$",
                message = "Each social link must be a valid http or https URL."
        ) String> socialLinks

) {}
```


## JPA Entity Relationships

### ID Generation for Entities

- All JPA entities must use the following annotations for primary key generation:
  ```java
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "<table>_id_seq")
  @SequenceGenerator(name = "<table>_id_seq", sequenceName = "<table>_id_seq", allocationSize = 1)
  private Long id;
  ```
- Replace `<table>` with the actual table/entity name in snake_case (e.g. `venues`).
- The corresponding sequence must exist in the database and follow the naming pattern `<table>_id_seq`.


**Bidirectional `@OneToMany` / `@ManyToOne`:**
- The `@ManyToOne` side is the **owning side** and holds `@JoinColumn(name = "...")` — this is what creates the FK column in the database.
- The `@OneToMany` side uses `mappedBy = "<field>"` to reference the owning side field.
- Use `cascade = CascadeType.ALL` on `@OneToMany` when the parent lifecycle should control the children (e.g. saving a `EventOrder` saves its `Event` children).

```
Venue       ──@OneToMany(mappedBy = "order", cascade = ALL)──▶  Event
Event       ──@ManyToOne @JoinColumn(name = "venue_id")──▶  Venue  (owning side)
```

**`@ManyToMany`:** use on the owning side (no `mappedBy`); JPA creates the join table automatically.

**ID generation:** use `@GeneratedValue(strategy = GenerationType.AUTO)` on `Long id` primary keys.

**`@Enumerated(EnumType.STRING)`:** always use `STRING` (not `ORDINAL`) for enum columns so values remain readable and stable.

## Service Layer Patterns

- Service methods that look up a resource return `Optional<T>` — never `null`.
- Controllers use `.map() / .orElseGet()` to convert `Optional` results to the appropriate `ResponseEntity`.
- Service methods that perform a delete return `boolean` — `true` if deleted, `false` if not found.
- Cross-feature repository access is allowed within a service.
- Inject all dependencies via **constructor injection** — never field injection (`@Autowired` on a field).

## PATCH vs PUT Update Strategy

- **`PATCH`** — partial update. Apply only the fields present in the request record to the existing entity and save. The service fetches the entity, mutates only the relevant fields, then saves.
- **`PUT`** — full replacement. Replace all mutable fields with the values from the request.
- The current convention uses `PATCH` for in-place field updates.
- Service methods for PATCH return `Optional<T>` — `Optional.empty()` when the resource does not exist; the controller maps that to a `404`.

## Things Agents Must NOT Do

- Add business logic to controllers.
- Use Hungarian notation on interfaces (`IAuthService`).
- Create generic utility classes (`Utilities`, `Helper`) — name by responsibility (`PriceUtils`, `SlugUtils`).
- Use abbreviated identifiers (`authSvc`, `ctrl`, `mgr`).
- Use underscores in class or method names.
- Run Maven commands without the wrapper (`./mvnw`).
- Commit secrets, credentials, or environment-specific values to `application.properties` — use profiles (`application-dev.properties`, `application-prod.properties`).
- Use `@Controller` for JSON/REST endpoints — use `@RestController`.
- Catch exceptions inside controllers — let `GlobalExceptionHandler` handle them.
- Add a new request record type without registering a `case` in `resolveValidationMessage()`.
- Return raw entities from REST endpoints without wrapping them in `ApiResponse<T>`.

## Useful Commands

```bash
# Start the application
./mvnw spring-boot:run

# Run all tests
./mvnw test

# Build a runnable JAR
./mvnw clean package

# Run the JAR
java -jar target/auth-service-0.0.1-SNAPSHOT.jar

# Clean build artifacts
./mvnw clean
```

## Testing Guidelines

- Unit-test service logic in `*ServiceTest` classes using Mockito to mock the repository.
- Integration / slice-test controllers in `*ControllerTest` classes using `@WebMvcTest` and `MockMvc`.
- Test class names must match the class under test with a `Test` suffix.
- Every public service method and every controller endpoint should have at least one test.

## Pull Requests & Commits

- Keep commits small and focused; use [Conventional Commits](https://www.conventionalcommits.org/) style (`feat:`, `fix:`, `refactor:`, `test:`, `docs:`).
- Open a PR against `main`; include a short description of what changed and why.
- All tests must pass before merging.

****