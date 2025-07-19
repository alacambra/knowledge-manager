#!/bin/bash

# Find and kill processes running on port 8080
PID=$(lsof -ti :8080)

if [ -n "$PID" ]; then
    echo "Killing process $PID running on port 8080"
    kill -9 $PID
    echo "Process killed"
else
    echo "No process found running on port 8080"
fi