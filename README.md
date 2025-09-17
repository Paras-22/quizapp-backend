# QuizApp (Backend)

Spring Boot REST API for a quiz tournament app.

## Run
- Java 17+
- MySQL running on XAMPP
- `mvn spring-boot:run`

## Endpoints (sample)
- `POST /users/register`
- `POST /users/login`
- `POST /questions/add`
- `POST /tournaments/create`
- `POST /tournaments/like/{id}`

## DB
Configured via `src/main/resources/application.properties`.
