#!/bin/bash

set -e

yes | sdkmanager "${EMULATOR}" # Install emulator system image

# emulator instance
echo no | android create avd --force -n ${EMULATOR_NAME} -k "${EMULATOR}"

# Run emulator in a subshell, this seems to solve the travis QT issue
( cd "$(dirname "$(which emulator)")" && ./emulator -avd ${EMULATOR_NAME} -verbose -show-kernel -selinux permissive -no-audio -no-window -no-boot-anim -wipe-data & )

# Wait for adb to make a connection with the emulator
android-wait-for-emulator

# Disable animations that may slow down tests
adb shell settings put global window_animation_scale 0 &
adb shell settings put global transition_animation_scale 0 &
adb shell settings put global animator_duration_scale 0 &

sleep 30
adb shell input keyevent 82 &

# Print devices
adb devices

# Build APK test
./gradlew build assembleAndroidTest -PtestCoverageEnabled='true'
retval=$?
if [$retval -ne 0]; then
    echo "error on assembling, exit code: "$retval
    exit $retval
fi

echo "Emulator tests not fully implemented!"
