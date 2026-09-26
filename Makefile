# JBH IAM Project Database Management Makefile
# Usage: make <target>
# For tasks requiring password: make db-create PASSWORD=your_password

# Variables
DB_NAME = jbh
DB_USER = jbh_admin
DB_HOST = localhost
DB_PORT = 5432
GRADLE = ./gradlew

# Colors for output
RED = \033[0;31m
GREEN = \033[0;32m
YELLOW = \033[1;33m
BLUE = \033[0;34m
NC = \033[0m # No Color

# Default target
.PHONY: help
help:
	@echo "$(BLUE)JBH IAM Database Management Commands$(NC)"
	@echo ""
	@echo "$(GREEN)Database Setup:$(NC)"
	@echo "  make db-create PASSWORD=<password>  - Create database and user with specified password"
	@echo "  make db-drop                        - Drop the database"
	@echo "  make db-reset PASSWORD=<password>   - Drop and recreate database"
	@echo ""
	@echo "$(GREEN)Database Migration:$(NC)"
	@echo "  make db-migrate                     - Run database migrations"
	@echo "  make db-rollback                    - Rollback last migration"
	@echo "  make db-status                      - Check migration status"
	@echo "  make db-validate                    - Validate migrations"
	@echo ""
	@echo "$(GREEN)Development:$(NC)"
	@echo "  make dev-setup PASSWORD=<password>  - Complete development setup"
	@echo "  make build                          - Build all modules"
	@echo "  make clean                          - Clean build artifacts"
	@echo "  make test                           - Run tests"
	@echo ""
	@echo "$(GREEN)Database Info:$(NC)"
	@echo "  make db-connect                     - Connect to database"
	@echo "  make db-info                        - Show database information"

# Database Creation
.PHONY: db-create
db-create:
	@if [ -z "$(PASSWORD)" ]; then \
		echo "$(RED)Error: PASSWORD parameter is required$(NC)"; \
		echo "$(YELLOW)Usage: make db-create PASSWORD=your_password$(NC)"; \
		exit 1; \
	fi
	@echo "$(BLUE)Creating database $(DB_NAME) with user $(DB_USER)...$(NC)"
	@psql postgres -c "CREATE ROLE $(DB_USER);" || echo "$(YELLOW)Role may already exist$(NC)"
	@psql postgres -c "ALTER ROLE $(DB_USER) WITH PASSWORD '$(PASSWORD)';"
	@psql postgres -c "ALTER ROLE $(DB_USER) CREATEDB;"
	@psql postgres -c "ALTER ROLE $(DB_USER) CREATEROLE;"
	@psql postgres -c "ALTER ROLE $(DB_USER) LOGIN;"
	@psql postgres -c "CREATE DATABASE \"$(DB_NAME)\" WITH OWNER = $(DB_USER) TEMPLATE = template0 ENCODING = 'UTF8' LC_COLLATE = 'en_US.UTF-8' LC_CTYPE = 'en_US.UTF-8' TABLESPACE = pg_default CONNECTION LIMIT = -1;" || echo "$(YELLOW)Database may already exist$(NC)"
	@psql postgres -c "GRANT ALL PRIVILEGES ON DATABASE \"$(DB_NAME)\" TO $(DB_USER);"
	@psql "$(DB_NAME)" -c "GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA \"public\" TO $(DB_USER);"
	@psql postgres -c "GRANT CONNECT ON DATABASE \"$(DB_NAME)\" TO $(DB_USER);"
	@psql "$(DB_NAME)" -c "GRANT ALL PRIVILEGES ON SCHEMA \"public\" TO $(DB_USER);"
	@psql "$(DB_NAME)" -c "ALTER DATABASE \"$(DB_NAME)\" SET TIME ZONE 'America/Bogota';"
	@psql "$(DB_NAME)" -c "CREATE EXTENSION IF NOT EXISTS pgcrypto;"
	@echo "$(GREEN)Database $(DB_NAME) created successfully!$(NC)"
	@echo "$(BLUE)To connect: psql -h $(DB_HOST) -p $(DB_PORT) -U $(DB_USER) -d $(DB_NAME)$(NC)"

# Database Removal
.PHONY: db-drop
db-drop:
	@echo "$(YELLOW)Dropping database $(DB_NAME)...$(NC)"
	@psql postgres -c "DROP DATABASE IF EXISTS \"$(DB_NAME)\";"
	@psql postgres -c "DROP ROLE IF EXISTS $(DB_USER);"
	@echo "$(GREEN)Database dropped successfully!$(NC)"

# Database Reset
.PHONY: db-reset
db-reset:
	@if [ -z "$(PASSWORD)" ]; then \
		echo "$(RED)Error: PASSWORD parameter is required$(NC)"; \
		echo "$(YELLOW)Usage: make db-reset PASSWORD=your_password$(NC)"; \
		exit 1; \
	fi
	@echo "$(YELLOW)Resetting database...$(NC)"
	@make db-drop
	@make db-create PASSWORD=$(PASSWORD)
	@make db-migrate

# Database Migration Tasks
.PHONY: db-migrate
db-migrate:
	@echo "$(BLUE)Running database migrations...$(NC)"
	@$(GRADLE) :jbh-iam-api:liquibaseUpdate

.PHONY: db-rollback
db-rollback:
	@echo "$(YELLOW)Rolling back last migration...$(NC)"
	@$(GRADLE) :jbh-iam-api:liquibaseRollbackCount -PliquibaseCommandValue=1

.PHONY: db-status
db-status:
	@echo "$(BLUE)Checking migration status...$(NC)"
	@$(GRADLE) :jbh-iam-api:liquibaseStatus

.PHONY: db-validate
db-validate:
	@echo "$(BLUE)Validating migrations...$(NC)"
	@$(GRADLE) :jbh-iam-api:liquibaseValidate

# Database Connection
.PHONY: db-connect
db-connect:
	@echo "$(BLUE)Connecting to database $(DB_NAME)...$(NC)"
	@psql -h $(DB_HOST) -p $(DB_PORT) -U $(DB_USER) -d $(DB_NAME)

# Database Information
.PHONY: db-info
db-info:
	@echo "$(BLUE)Database Information:$(NC)"
	@echo "  Database: $(DB_NAME)"
	@echo "  User: $(DB_USER)"
	@echo "  Host: $(DB_HOST)"
	@echo "  Port: $(DB_PORT)"
	@psql postgres -c "\l" | grep $(DB_NAME) || echo "$(YELLOW)Database not found$(NC)"

# Development Setup
.PHONY: dev-setup
dev-setup:
	@if [ -z "$(PASSWORD)" ]; then \
		echo "$(RED)Error: PASSWORD parameter is required$(NC)"; \
		echo "$(YELLOW)Usage: make dev-setup PASSWORD=your_password$(NC)"; \
		exit 1; \
	fi
	@echo "$(BLUE)Setting up development environment...$(NC)"
	@make db-create PASSWORD=$(PASSWORD)
	@make db-migrate
	@make build
	@echo "$(GREEN)Development environment setup complete!$(NC)"

# Build Tasks
.PHONY: build
build:
	@echo "$(BLUE)Building all modules...$(NC)"
	@$(GRADLE) build

.PHONY: clean
clean:
	@echo "$(BLUE)Cleaning build artifacts...$(NC)"
	@$(GRADLE) clean

.PHONY: test
test:
	@echo "$(BLUE)Running tests...$(NC)"
	@$(GRADLE) test

# Utility to check prerequisites
.PHONY: check-prereqs
check-prereqs:
	@echo "$(BLUE)Checking prerequisites...$(NC)"
	@command -v psql >/dev/null 2>&1 || { echo "$(RED)PostgreSQL client (psql) is required but not installed.$(NC)"; exit 1; }
	@psql postgres -c "SELECT version();" >/dev/null 2>&1 || { echo "$(RED)Cannot connect to PostgreSQL.$(NC)"; exit 1; }
	@echo "$(GREEN)All prerequisites met!$(NC)"