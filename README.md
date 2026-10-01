# Ecommerce API

A Spring Boot REST API for managing customers and orders. It uses Spring MVC for HTTP endpoints, Spring Data JPA/Hibernate for persistence, and MySQL as its database.

## Architecture

```mermaid
flowchart LR
    Client[API client / Swagger UI] -->|HTTP JSON| Controllers

    subgraph App[Spring Boot application]
        Controllers[CustomerController<br/>OrderController]
        Repositories[Spring Data repositories]
        Entities[Customer<br/>Order<br/>OrderItem]
        Scheduler[OrderStatusScheduler<br/>every 5 minutes]
        Controllers --> Repositories
        Repositories --> Entities
        Scheduler --> Repositories
    end

    Repositories -->|JPA / Hibernate| DB[(MySQL)]
    Scheduler -->|PENDING → PROCESSING| Entities
```

`Order` contains a collection of `OrderItem` records. The scheduler runs five minutes after application startup and then every five minutes, moving all pending orders to processing and refreshing their `updatedAt` timestamp.

## Requirements

- JDK 17 or later
- MySQL 8
- Maven (or the included Maven wrapper)
- Docker (optional, for containerized startup)

## Configuration and startup

1. Create the database:

   ```sql
   CREATE DATABASE ecommerce;
   ```

2. Configure the MySQL URL, username, and password in `src/main/resources/application.properties` for your local environment. Do not commit credentials.
3. Start the application from the project root:

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

   Alternatively, use `mvn spring-boot:run` if Maven is installed.

The API uses the `/api` base path. OpenAPI documentation is available at [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html), with the OpenAPI document at `/v3/api-docs`.

   ### Run with Docker

   Build the image from the project root:

   ```powershell
   docker build -t ecommerce-api .
   ```

   The MySQL database must be running and the `ecommerce` database must already exist. For MySQL running on the same Windows host, set the database password in the current PowerShell session and start the container:

   ```powershell
   $env:SPRING_DATASOURCE_PASSWORD = Read-Host "MySQL password"
   docker run --rm -p 8080:8080 `
     -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/ecommerce `
     -e SPRING_DATASOURCE_USERNAME=root `
     -e SPRING_DATASOURCE_PASSWORD=$env:SPRING_DATASOURCE_PASSWORD `
     ecommerce-api
   ```

   Set the datasource environment variables to match your database. Avoid putting real passwords in source files or shell history.

   ## API endpoints

All request and response bodies use JSON. Order IDs are UUIDs; customer and product IDs in the order API are integers.

| Method | Endpoint | Description |
| --- | --- | --- |
| `GET` | `/api/customers` | List customers |
| `GET` | `/api/customers/{id}` | Get a customer |
| `POST` | `/api/customers` | Create a customer |
| `PUT` | `/api/customers/{id}` | Update a customer |
| `DELETE` | `/api/customers/{id}` | Delete a customer |
| `GET` | `/api/orders` | List orders; optionally filter by `status` and/or `customerId` |
| `GET` | `/api/orders/{id}` | Get an order |
| `POST` | `/api/orders` | Create an order |
| `PUT` | `/api/orders/{id}` | Update an order |
| `POST` | `/api/orders/{id}/cancel` | Cancel an order if it is pending |
| `DELETE` | `/api/orders/{id}` | Delete an order |

Order filters can be combined, for example:

```text
GET /api/orders?status=PENDING&customerId=42
```

Supported order statuses are `PENDING`, `PROCESSING`, `SHIPPED`, `DELIVERED`, and `CANCELLED`. A newly created order defaults to `PENDING`. The cancel endpoint returns `409 Conflict` when the order is not pending and `404 Not Found` when it does not exist.

Example order creation request:

```json
{
  "customerId": 42,
  "items": [
    {
      "productId": 7,
      "productName": "Widget",
      "quantity": 2,
      "unitPrice": 12.50
    }
  ]
}
```

The order total and each item subtotal are calculated by the API. Customer requests use `name` and `email`.

## Tests and coverage

Run the test suite and generate the JaCoCo coverage report:

```powershell
.\mvnw.cmd verify
```

Open `target/site/jacoco/index.html` in a browser to view the HTML report. The test suite's Spring application-context test connects to the configured MySQL database, so the database must be available when running the full suite.
