#!/bin/bash
# Tears down containers specified in docker-compose.yaml and brings everything down.

docker compose down
docker volume prune -f
