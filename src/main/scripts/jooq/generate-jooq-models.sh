#!/bin/bash

set -e

cleanup() {
    if [ ! -z "$CONTAINER_ID" ]; then
        echo "Cleaning up container..."
        docker stop "$CONTAINER_ID" >/dev/null 2>&1
        docker rm "$CONTAINER_ID" >/dev/null 2>&1
    fi
}

trap cleanup EXIT

main() {
    echo "Starting jOOQ Code Generation Process for Knowledge Manager"

    echo "USER: '$POSTGRES_USER'"

    # Check if environment variables are loaded
    if [ -z "$POSTGRES_USER" ] || [ -z "$POSTGRES_PASSWORD" ]; then
        echo "❌ Environment variables not found!"
        echo "Please source your environment file first:"
        echo "export \$(cat .env.backend | grep -v '^#' | xargs)"
        exit 1
    fi

    echo "📦 Using environment variables:"
    echo "   POSTGRES_USER: $POSTGRES_USER"
    echo "   Database: knowledge_manager"

    echo "🐘 Starting PostgreSQL container with pgvector..."
    CONTAINER_ID=$(docker run -d \
        -e POSTGRES_USER="$POSTGRES_USER" \
        -e POSTGRES_PASSWORD="$POSTGRES_PASSWORD" \
        -e POSTGRES_DB=knowledge_manager \
        -p 5432 \
        pgvector/pgvector:pg17)

    echo "⏳ Waiting for container to initialize..."
    sleep 15

    MAPPED_PORT=$(docker port "$CONTAINER_ID" 5432/tcp | cut -d':' -f2)
    export POSTGRES_JDBC_URL="jdbc:postgresql://${POSTGRES_HOST}:${MAPPED_PORT}/knowledge_manager"

    echo "✅ Container started. JDBC URL: $JDBC_URL"

    echo "🚀 Running Flyway migrations..."
    mvn flyway:migrate \
        -Dflyway.locations=filesystem:src/main/resources/db/migration \
        -Dflyway.url="$POSTGRES_JDBC_URL" \
        -Dflyway.user="$POSTGRES_USER" \
        -Dflyway.password="$POSTGRES_PASSWORD"

    echo "🏗️  Generating jOOQ code..."
    echo "🏗️  Generating jOOQ code..."
    mvn org.jooq:jooq-codegen-maven:generate \
        -Djooq.jdbc.driver=org.postgresql.Driver \
        -Djooq.jdbc.url="$POSTGRES_JDBC_URL" \
        -Djooq.jdbc.user="$POSTGRES_USER" \
        -Djooq.jdbc.password="$POSTGRES_PASSWORD" \
        -Djooq.generator.database.name=org.jooq.meta.postgres.PostgresDatabase \
        -Djooq.generator.database.inputSchema=public \
        -Djooq.generator.database.excludes=flyway_schema_history \
        -Djooq.generator.target.packageName=tech.lacambra.kmanager.generated.jooq \
        -Djooq.generator.target.directory=target/generated-sources/jooq

    echo "🗑️  Removing existing jOOQ classes..."
    rm -rf src/main/java/tech/lacambra/kmanager/generated/

    echo "📁 Moving new jOOQ classes..."
    mkdir -p src/main/java/tech/lacambra/kmanager/
    mv target/generated-sources/jooq/tech/lacambra/kmanager/generated/ src/main/java/tech/lacambra/kmanager/

    echo "✅ jOOQ code generation completed successfully!"
    echo "📊 Generated classes are now available in: src/main/java/tech/lacambra/kmanager/generated/"
}

main
