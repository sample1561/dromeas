#!/bin/sh
# format.sh - auto-format code with Spotless

echo "Running Spotless apply..."
mvn spotless:apply

RESULT=$?
if [ $RESULT -ne 0 ]; then
    echo "Spotless apply failed!"
    exit 1
fi
