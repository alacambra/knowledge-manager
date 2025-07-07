# Knowledge Manager

A modern knowledge management application built with Quarkus backend and Preact frontend, featuring PostgreSQL with vector extensions for AI-powered search and content organization.

## Features

- **Quarkus Backend**: Fast, lightweight Java framework
- **PostgreSQL with pgvector**: Vector database for AI/ML applications
- **jOOQ**: Type-safe SQL queries
- **Flyway**: Database migrations
- **OpenAPI**: Automatic API documentation
- **Preact Frontend**: Lightweight React alternative
- **Jakarta EE**: Modern enterprise Java APIs

## Prerequisites

- Java 17+
- Maven 3.8+
- Docker & Docker Compose
- Node.js (for Preact frontend)

## Quick Start

### 1. Clone and Setup

```bash
# Use the provided Maven archetype command from the setup file
# Then navigate to the project directory
cd knowledge-manager

# Copy environment template and configure
cp .env.backend.example .env.backend
# Edit .env.backend with your preferred values
```

### 2. Start PostgreSQL

```bash
# Start PostgreSQL with pgvector extension
docker-compose up -d postgres

# Wait for database to be ready
docker-compose logs -f postgres
```

### 3. Run the Application

```bash
# Development mode with live reload
mvn quarkus:dev

# The application will be available at:
# - Main app: http://localhost:8080
# - OpenAPI spec: http://localhost:8080/openapi
# - Swagger UI: http://localhost:8080/swagger-ui
# - Health check: http://localhost:8080/health
```

## Development

### Database Operations

```bash
# Run Flyway migrations manually
mvn flyway:migrate

# Generate jOOQ classes (after database is running)
mvn generate-sources

# Reset database
mvn flyway:clean flyway:migrate
```

### API Documentation

- OpenAPI specification: `http://localhost:8080/openapi`
- Swagger UI: `http://localhost:8080/swagger-ui`

### Frontend Development

The Preact application should be built and placed in `src/main/resources/META-INF/resources/`.

```bash
# Example workflow for Preact app
cd frontend  # Your Preact project directory
npm run build
cp -r dist/* ../src/main/resources/META-INF/resources/
```

## Project Structure

```
knowledge-manager/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── tech/lacambra/kmanager/
│   │   │       ├── config/           # Configuration classes
│   │   │       ├── resource/         # REST endpoints
│   │   │       └── generated/        # jOOQ generated classes
│   │   └── resources/
│   │       ├── META-INF/resources/   # Static files (Preact app)
│   │       ├── db/migration/         # Flyway migrations
│   │       └── application.properties
│   └── test/
├── docker-compose.yml
├── pom.xml
└── README.md
```

## Configuration

### Database Configuration

The application uses environment variables for configuration. Copy the template and modify as needed:

```bash
cp .env.backend.example .env.backend
```

Key environment variables in `.env.backend`:
```properties
POSTGRES_USER=kmuser
POSTGRES_PASSWORD=your-secure-password
POSTGRES_PORT=5432
POSTGRES_DATA=postgres_data
DATABASE_URL=jdbc:postgresql://localhost:5432/knowledge_manager
LOG_LEVEL=INFO
CORS_ORIGINS=http://localhost:3000,http://localhost:8080,https://knowledge-manager.lacambra.tech
```

The application.properties file uses these environment variables with sensible defaults.

### Vector Search

The application includes pgvector support for AI/ML applications:

- Vector similarity search
- Embedding storage (1536 dimensions for OpenAI)
- Metadata indexing with JSONB

### jOOQ Code Generation

Configure database connection in `pom.xml` jOOQ plugin section, then:

```bash
mvn generate-sources
```

## API Endpoints

- `GET /api/documents` - List all documents
- `GET /api/documents/{id}` - Get document by ID
- `POST /api/documents` - Create new document
- `POST /api/documents/search` - Vector similarity search

## Production Build

```bash
# Build JAR
mvn clean package

# Build native executable
mvn package -Pnative

# Run JAR
java -jar target/quarkus-app/quarkus-run.jar
```

## Docker Deployment

```bash
# Build image
docker build -t knowledge-manager .

# Run with docker-compose
docker-compose up
```

## Testing

```bash
# Run tests
mvn test

# Run integration tests
mvn verify
```

## Monitoring

- Health checks: `http://localhost:8080/health`
- Metrics: `http://localhost:8080/metrics`
- Prometheus metrics: `http://localhost:8080/metrics/prometheus`