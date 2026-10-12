# Dockerfile for GIK Backend
# Multi-stage build for smaller final image

# Build stage
FROM rust:1.75-slim as builder

WORKDIR /usr/src/gik_backend

# Copy cargo files
COPY Cargo.toml Cargo.lock ./

# Build dependencies
RUN apt-get update && apt-get install -y --no-install-recommends \
    pkg-config \
    libssl-dev \
    && rm -rf /var/lib/apt/lists/*

# Build the application
RUN cargo build --release

# Runtime stage
FROM debian:12-slim

WORKDIR /usr/src/gik_backend

# Install runtime dependencies
RUN apt-get update && apt-get install -y --no-install-recommends \
    ca-certificates \
    && rm -rf /var/lib/apt/lists/*

# Copy built binary from builder
COPY --from=builder /usr/src/gik_backend/target/release/gik_backend /usr/local/bin/gik_backend

# Copy static files
COPY static /usr/src/gik_backend/static

# Copy default database file (will be created if not exists)
COPY gik.db /usr/src/gik_backend/gik.db

# Create data directory
RUN mkdir -p /usr/src/gik_backend/data

# Set environment variables
ENV DATABASE_URL=/usr/src/gik_backend/gik.db
ENV RUST_LOG=actix_web=info

# Expose port
EXPOSE 8080

# Run the application
CMD ["gik_backend"]
