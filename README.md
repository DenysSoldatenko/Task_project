# TaskManager Application

The TaskManager (Task_project) is a Java-based RESTful service that helps users organize and manage tasks through a set of API endpoints. It provides functionality for creating, reading, updating, and deleting tasks, with support for user roles, file attachments, and caching. The project follows best practices in Java and Spring Boot development, making it suitable for production use.

## Table of Contents

- Features
- Technologies & Tools
- System Requirements
- Installation
- Configuration
- Usage
- Architecture
- Testing
- Contributing
- License
- Contact

## Features

- **Task Management**: Create, retrieve, update, and delete tasks via RESTful endpoints.
- **Task Comments**: Manage task comments with create, update, delete, and paginated retrieval secured by role-based and ownership access control.
- **User Roles and Permissions**: Role-based access control with fine-grained permission checks enforced via Spring Security expressions.
- **Authentication**: Uses **Keycloak** for OAuth2-based authentication and authorization, replacing custom JWT implementations for robust and centralized security management.
- **File Attachments**: Tasks can include image uploads stored using MinIO.
- **Event Streaming**: Integrated with **Apache Kafka** to send events when a task is approved. These events trigger checks for achievement unlocks related to the new task.
- **Logging and Monitoring**: Production-level logging and monitoring using **Promtail** for log collection, **Loki** for log aggregation, **Prometheus** for metrics gathering, and **Grafana** for visualization and alerting dashboards.
- **Swagger API Documentation**: Interactive API docs for all endpoints including tasks and comments.
- **High Performance**: Uses asynchronous I/O and efficient data access with Spring Data JPA and PostgreSQL.
- **Comprehensive Reporting**: Generate secured, filterable, downloadable PDF reports on user task performance, team progress, project status, top performers, and task progress within date ranges.

## Technologies & Tools

- **Language & Frameworks**
    - **Java 17+**: Primary language
    - **Spring Boot**: For REST APIs, GraphQL, OAuth2 security, Actuator, and data access (JPA)
    - **MapStruct**: Compile-time mapping between DTOs and entities
    - **GraphQL**: Query support using Spring Boot GraphQL and `graphql-java-servlet`

- **Authentication & Authorization**
    - **Keycloak**: OAuth2/OpenID Connect identity provider replacing custom JWT handling
    - **Spring Security**: Resource server and client support for secure endpoints

- **Data & Storage**
    - **PostgreSQL**: Primary relational database
    - **MinIO**: S3-compatible object storage for task-related file uploads
    - **Liquibase**: Database schema versioning and migrations

- **Event Streaming & Messaging**
    - **Apache Kafka**: Used to publish events when tasks are approved, triggering achievement checks

- **Monitoring, Logging & Observability**
    - **Prometheus**: Metrics collection
    - **Grafana**: Dashboards and visualizations for application metrics
    - **Loki & Promtail**: Centralized logging infrastructure for production-grade observability
    - **Micrometer**: Metric instrumentation integrated with Prometheus

- **API Documentation**
    - **SpringDoc + OpenAPI**: Generates and serves Swagger UI for REST endpoints

- **Validation & Utilities**
    - **Jakarta Validation**: For validating DTOs (JSR 380)
    - **Hibernate Validator**: Implementation of Jakarta Bean Validation
    - **Slugify**: URL-safe slugs for entity identifiers
    - **DataFaker**: Used to generate realistic fake data for testing or seeding
    - **JSoup**: HTML parsing and sanitization
    - **Flying Saucer**: PDF generation from XHTML documents

- **Resilience & Retry**
    - **Resilience4j**: Retry, CircuitBreaker, RateLimiter patterns

- **Build & Containerization**
    - **Gradle**: Project build tool (wrapper included)
    - **Docker & Docker Compose**: Multi-container orchestration for services like PostgreSQL, Kafka, Keycloak, Grafana, etc.

- **Testing & Dev Tools**
    - **JUnit**: Unit and integration testing
    - **Testcontainers**: Spinning up PostgreSQL and other services in containers for isolated tests
    - **Lombok**: Reduces boilerplate in Java classes
    - **Git**: Version control system

## Configuration

Configuration is managed via environment variables. A `.env.default` file is included as a reference. To run the application, create a `.env` file in the root directory and configure the required values.

The system will **fail to start** if any essential environment variable is missing or misconfigured.

### 🔧 Core Configuration

#### **PostgreSQL Database**
- `SPRING_DATASOURCE_HOST`: Database host (e.g., `localhost`)
- `SPRING_DATASOURCE_PORT`: Database port (default: `5432`)
- `SPRING_DATASOURCE_DATABASE`: Name of the database (e.g., `taskdb`)
- `SPRING_DATASOURCE_USERNAME`: DB username
- `SPRING_DATASOURCE_PASSWORD`: DB password
- `SPRING_DATASOURCE_SCHEMA`: (Optional) Schema name (e.g., `task_list`)

#### **Kafka / Zookeeper**
- `BOOTSTRAP_SERVER`: Kafka bootstrap server (e.g., `localhost:29092`)
- `KAFKA_BROKER_ID`: Kafka broker ID
- `KAFKA_ZOOKEEPER_CONNECT`: Zookeeper connection string
- `KAFKA_ADVERTISED_LISTENERS`: Advertised Kafka listeners
- `KAFKA_LISTENER_SECURITY_PROTOCOL_MAP`: Protocol map
- `KAFKA_INTER_BROKER_LISTENER_NAME`: Internal broker listener
- `KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR`: Replication factor (usually `1` in dev)
- `KAFKA_BROKERCONNECT`: Broker connect string (e.g., `localhost:9092`)
- `ZOOKEEPER_CLIENT_PORT`: Zookeeper port
- `ZOOKEEPER_TICK_TIME`: Zookeeper tick time (e.g., `2000`)

#### **MinIO Object Storage**
- `MINIO_URL`: MinIO server URL (e.g., `http://localhost:9000`)
- `MINIO_ACCESS_KEY`: MinIO access key (e.g., `minioadmin`)
- `MINIO_SECRET_KEY`: MinIO secret key (e.g., `minioadmin`)
- `MINIO_BUCKET`: Bucket name (e.g., `images`)

#### **Keycloak Authentication**
- `KEYCLOAK_ADMIN_SERVER_URL`: Admin server URL (e.g., `http://localhost:8081`)
- `KEYCLOAK_ADMIN_REALM`: Admin realm (e.g., `master`)
- `KEYCLOAK_TARGET_REALM`: Application realm (e.g., `task-realm`)
- `KEYCLOAK_ADMIN_CLIENT_ID`: Admin client ID (e.g., `admin-cli`)
- `KEYCLOAK_ADMIN_USERNAME`: Admin username (e.g., `admin`)
- `KEYCLOAK_ADMIN_PASSWORD`: Admin password (e.g., `admin`)
- `KEYCLOAK_CLIENT_ID`: Application client ID
- `KEYCLOAK_CLIENT_SECRET`: Client secret for public clients
- `KEYCLOAK_ISSUER_URI`: Issuer URI for token validation
- `KEYCLOAK_TOKEN_URI`: Token endpoint
- `KEYCLOAK_AUTH_URI`: Authorization endpoint

---

> ✅ All configuration variables should be placed in a `.env` file and referenced using `env_file` in Docker Compose for consistent environment replication.










