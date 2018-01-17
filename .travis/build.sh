#!/bin/bash

# Make sure we're in the build dir, because Murphy
cd ${TRAVIS_BUILD_DIR}

# Print gradle version to terminal
./gradlew --version

./gradlew clean

./gradlew assembleDebug --no-daemon --stacktrace
# TODO: Build, sign, and depoy APK
