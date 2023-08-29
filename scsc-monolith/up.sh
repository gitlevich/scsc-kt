#!/bin/bash
# Builds the containers and starts the services.

docker volume prune -f
docker compose up -d --build
