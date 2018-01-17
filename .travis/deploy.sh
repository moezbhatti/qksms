#!/bin/bash

CURRENT_TAG=`git describe --exact-match --abbrev=0 --tags`

if [[ "$CURRENT_TAG" == "" ]]; then
  echo "No tags found, no need for a release."
  exit 0
fi

PREVIOUS_TAG=`git describe HEAD^1 --abbrev=0 --tags`
GIT_HISTORY=`git log --no-merges --format="- %s" $PREVIOUS_TAG..HEAD`

if [[ "$PREVIOUS_TAG" == "" ]]; then
  GIT_HISTORY=`git log --no-merges --format="- %s"`
fi

echo "Current Tag: $CURRENT_TAG"
echo "Previous Tag: $PREVIOUS_TAG"
echo "Release Notes: $GIT_HISTORY"

# Make sure we're in the build dir, because Murphy
cd ${TRAVIS_BUILD_DIR}

# TODO: Build, sign, and depoy APK
echo "FIXME: Build, sign, and deploy APK to GH"

# Some references:
# https://github.com/LawnchairLauncher/Lawnchair/blob/alpha/.travis.yml
# https://github.com/timusus/Shuttle/blob/dev/.travis.yml
# https://docs.travis-ci.com/user/deployment/releases
# https://docs.travis-ci.com/user/encrypting-files


