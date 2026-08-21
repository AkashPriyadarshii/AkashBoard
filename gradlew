#!/bin/sh

#
# Gradle wrapper script for AkashBoard
# Uses local Gradle installation
#

# Find Gradle
GRADLE_HOME="/c/tools/gradle-8.12"
GRADLE_CMD="$GRADLE_HOME/bin/gradle"

if [ ! -f "$GRADLE_CMD" ]; then
    echo "ERROR: Gradle not found at $GRADLE_CMD"
    exit 1
fi

exec "$GRADLE_CMD" "$@"
