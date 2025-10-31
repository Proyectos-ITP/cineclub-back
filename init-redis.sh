#!/bin/bash

# Script to initialize Redis with ACL using environment variables from .env

set -e

echo "==> Initializing Redis with ACL..."

# Validate required environment variables
if [ -z "$SPRING_REDIS_USER" ]; then
    echo "ERROR: SPRING_REDIS_USER is not set!"
    exit 1
fi

if [ -z "$SPRING_REDIS_PASSWORD" ]; then
    echo "ERROR: SPRING_REDIS_PASSWORD is not set!"
    exit 1
fi

echo "==> Creating ACL file with user: $SPRING_REDIS_USER"

# Create users.acl file dynamically in /data volume (persisted)
cat > /data/users.acl <<EOF
user default off
user $SPRING_REDIS_USER on >$SPRING_REDIS_PASSWORD ~* &* +@all
EOF

echo "==> ACL file created successfully at /data/users.acl"
echo "==> User: $SPRING_REDIS_USER"
echo "==> Starting Redis server..."

# Start Redis server
exec redis-server /usr/local/etc/redis/redis.conf
