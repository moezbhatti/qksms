#!/bin/bash

# Make sure we're in the build dir, because Murphy
cd ${TRAVIS_BUILD_DIR}

# Print gradle version to terminal
./gradlew --version
# Perform a clean gradle build
./gradlew clean build

# Build APK test
./gradlew build assembleAndroidTest -PtestCoverageEnabled='true'
retval=$?
if [$retval -ne 0]; then
    echo "error on assembling, exit code: "$retval
    exit $retval
fi

# TODO: Build, sign, and depoy APK
