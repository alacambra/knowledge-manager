# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Description

The knoeledge manger tool is a tool that aims to achieve that once an information has been provided it can be used by any other domain without requiring further input of the original writer. As an example, an SME will desicribe how a busniess flow works. This informations will be explained differently to a manager, a software architect or a developer. However, the information will be the same.

The Knowledge manager aims to allow a user to create knowledge units. A knoweldge unit are all pieces of information needed to execute a task. For examplae, a developer developing a feature may need to know hoe the proces works, provided by SME or ba, while understanding the currtent architecture and having knoweldge on the compoany IT tools. Stacks, pieplines, ... His knowledge unit will encloseall this information in appropiate dimensiona anddeepnes, so that an LLM can properly support him. 

This knowedge unit may be created as a big file or sum of files, that will be provided to another chat ot llm tool, or being aggragted to a vector database, that will be queried previous to send the final prompt to an llm api.



**Key Features:**
- **Knowledge Unit Creation**: Aggregate multi-dimensional information needed for specific tasks
- **Cross-Domain Information Reuse**: Same information adapted for different roles (SME, manager, architect, developer)
- **Contextual Knowledge Assembly**: Combine process knowledge, architecture, tools, and domain expertise
- **LLM Integration**: Knowledge units can be fed to LLM tools or stored in vector databases for retrieval
- **Flexible Output**: Generate comprehensive files or query-ready vector embeddings
- **Semantic Search**: Vector embeddings for intelligent information retrieval using pgvector
- **RESTful API**: OpenAPI-documented endpoints for integration with LLM workflows

## Architecture Overview

This is a full-stack knowledge management application with two main components:

### Backend (server/)
- **Framework**: Quarkus (Java 21) with Maven
- **Database**: PostgreSQL with pgvector extension for vector similarity search
- **Key Technologies**:
  - jOOQ for type-safe database queries
  - Flyway for database migrations
  - OpenAPI/Swagger for API documentation
  - Jakarta EE APIs (REST, CDI, Validation)
  - MinIO for file storage
  - ONNX Runtime for AI/ML embeddings

### Frontend (client/)
- **Framework**: Preact with Vite
- **UI Library**: Ant Design
- **Build Tool**: Vite with TypeScript support

## Common Commands

### Backend Development (server/)
```bash
# Start development server (includes database migrations)
mvn quarkus:dev

# Run tests
mvn test

# Run integration tests
mvn verify

# Generate jOOQ models (requires PostgreSQL running)
./src/main/scripts/jooq/generate-jooq-models.sh

# Run Flyway migrations manually
mvn flyway:migrate

# Build for production
mvn clean package

# Build native executable
mvn package -Pnative
```

### Frontend Development (client/)
```bash
# Start development server
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview

# Run linting
npm run lint
```

### Database Operations
```bash
# Start PostgreSQL with pgvector
docker-compose up -d postgres

# Generate jOOQ models after schema changes
cd server && ./src/main/scripts/jooq/generate-jooq-models.sh
```

## Database Schema

The main entity is `documents` table with:
- Vector embeddings (384 dimensions) for similarity search
- JSONB metadata for flexible document properties
- Full-text search indices on title and content
- Automatic timestamp updates via triggers

## Key Configuration Files

- `server/src/main/resources/application.properties` - Quarkus configuration
- `server/pom.xml` - Maven dependencies and jOOQ configuration
- `client/package.json` - Frontend dependencies and scripts
- `server/src/main/resources/db/migration/` - Flyway migration files

## Environment Variables

The application uses environment variables for configuration:
- `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_JDBC_URL` - Database connection
- `EMBEDDING_MODEL_PATH` - Path to ONNX embedding model
- `CORS_ORIGINS` - Allowed CORS origins
- `LOG_LEVEL` - Logging level

## AI/ML Integration

The application includes:
- Vector similarity search using pgvector
- ONNX Runtime for generating embeddings
- Support for 384-dimensional embeddings (configurable)
- Metadata indexing with JSONB for flexible document properties

## Development Workflow

1. Start PostgreSQL: `docker-compose up -d postgres`
2. Backend development: `cd server && mvn quarkus:dev`
3. Frontend development: `cd client && npm run dev`
4. After schema changes: regenerate jOOQ models using the provided script
5. Database migrations are automatically applied on startup in dev mode

## Testing

- Backend tests use JUnit 5 with Testcontainers for integration tests
- Tests run against PostgreSQL in containers
- Integration tests verify REST endpoints and database operations

## INSTRUCTIONS
1. When providing UI code, always use what is defined in the stack
2. do not immediatly generate code. Instead:
   2.1  ask for clarifications if needed
   2.2 explain the plan
   2.3 ask for permission to proceed
   2.4. proceed if accepted
3. When generating code
3.1.Do not always generate full class or file code.:
3.1.1 if you are complementing previous code, only generater the code of the implied function in case is a function. 
3.2. always provide at the begining if possible the file and the class that where the code belongs 
4. this project uses the following stack
4.1 Frontend: preact with typescript
4.2 backend: quarkus with java
4.3 database: postgres
4.4 vector_database: postgres
5. Commiting messages
5.1. when writting "CM" and nothing else , prepare a commit message.
5.2. never "git add" nor propose "git add"
5.3. do not set claude as co-auhtor
5.4. when wrtitin "CMP" and nothing else, prepare a commit message and propose to commit and push
6. Typescript coding rules:
6.1 avoid the use of "any" always when possible
6.2 if any is used, explain clearlywhy is it needed
6.3 always use explicit types 
7. General coding rules
7.1 Comments are only allowed for public or exported classes and methods. 
7.2. If there is a need in the code for an explanation, do not create a comment. Create instead a method/function witha self explanatory name.
