#!/bin/bash

# =============================================================================
# Klassifikator Deployment Script
# =============================================================================
# Автоматический скрипт развёртывания проекта на Timeweb Cloud
# =============================================================================

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Functions
print_header() {
    echo -e "\n${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${BLUE}  $1${NC}"
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}\n"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

# Check if .env file exists
check_env_file() {
    if [ ! -f ".env" ]; then
        print_error ".env file not found!"
        print_info "Copy env.example to .env and fill in the values:"
        print_info "  cp env.example .env"
        print_info "  nano .env"
        exit 1
    fi
    print_success ".env file found"
}

# Check if Google credentials exist
check_google_credentials() {
    if [ ! -f "config/google-credentials.json" ]; then
        print_warning "Google credentials file not found at config/google-credentials.json"
        print_info "Place your Google Service Account JSON file at config/google-credentials.json"
        read -p "Continue without Google Sheets integration? (y/n) " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            exit 1
        fi
    else
        print_success "Google credentials found"
    fi
}

# Build Docker images
build_images() {
    print_header "Building Docker Images"
    
    print_info "Building all services..."
    docker-compose -f docker-compose.prod.yml build --no-cache
    
    print_success "All images built successfully"
}

# Start services
start_services() {
    print_header "Starting Services"
    
    print_info "Starting infrastructure services (PostgreSQL, Redis, MinIO)..."
    docker-compose -f docker-compose.prod.yml up -d postgres redis minio
    
    print_info "Waiting for infrastructure to be ready (30 seconds)..."
    sleep 30
    
    print_info "Starting application services..."
    docker-compose -f docker-compose.prod.yml up -d
    
    print_success "All services started"
}

# Check service health
check_health() {
    print_header "Health Check"
    
    print_info "Waiting for services to be healthy (60 seconds)..."
    sleep 60
    
    services=(
        "api-gateway:8080"
        "landing-service:8081"
        "content-service:8082"
        "template-service:8083"
        "media-service:8084"
        "integration-service:8085"
        "order-service:8086"
    )
    
    for service in "${services[@]}"; do
        IFS=':' read -r name port <<< "$service"
        
        if curl -f -s "http://localhost:${port}/actuator/health" > /dev/null 2>&1; then
            print_success "${name} is healthy"
        else
            print_error "${name} is not healthy"
        fi
    done
}

# Show logs
show_logs() {
    print_header "Recent Logs"
    
    docker-compose -f docker-compose.prod.yml logs --tail=20
}

# Main deployment flow
main() {
    print_header "🚀 Klassifikator Deployment Script"
    
    print_info "Starting deployment process..."
    
    # Pre-flight checks
    check_env_file
    check_google_credentials
    
    # Stop existing containers
    print_info "Stopping existing containers..."
    docker-compose -f docker-compose.prod.yml down || true
    
    # Build and start
    build_images
    start_services
    check_health
    
    # Show final status
    print_header "Deployment Complete! 🎉"
    
    print_success "All services are running!"
    print_info ""
    print_info "Service URLs:"
    print_info "  - API Gateway:         http://localhost:8080"
    print_info "  - Landing Service:     http://localhost:8081"
    print_info "  - Content Service:     http://localhost:8082"
    print_info "  - Template Service:    http://localhost:8083"
    print_info "  - Media Service:       http://localhost:8084"
    print_info "  - Integration Service: http://localhost:8085"
    print_info "  - Order Service:       http://localhost:8086"
    print_info "  - MinIO Console:       http://localhost:9001"
    print_info ""
    print_info "To view logs: docker-compose -f docker-compose.prod.yml logs -f"
    print_info "To stop services: docker-compose -f docker-compose.prod.yml down"
    print_info ""
    
    # Ask to show logs
    read -p "Show recent logs? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        show_logs
    fi
}

# Run main function
main

