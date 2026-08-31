#!/bin/bash

set -e

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

IMAGE="high-performance-api:dev"
CLUSTER="high-performance-api"
DEPLOYMENT="high-performance-api"

cd "$PROJECT_ROOT"

echo "==> Building Spring Boot application..."
./mvnw clean package -DskipTests

echo "==> Building Docker image: $IMAGE"
docker build -t "$IMAGE" .

echo "==> Loading image into Kind..."
kind load docker-image "$IMAGE" --name "$CLUSTER"

echo "==> Restarting Kubernetes deployment..."
kubectl rollout restart deployment/"$DEPLOYMENT"

echo "==> Waiting for rollout..."
kubectl rollout status deployment/"$DEPLOYMENT"

echo "==> Current pods:"
kubectl get pods -l app="$DEPLOYMENT"

echo "==> Development deployment complete."
