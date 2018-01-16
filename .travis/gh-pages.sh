#!/bin/bash

# This script was reallocated from squidfunk/mkdocs-material: https://github.com/squidfunk/mkdocs-material/blob/master/.travis.sh
CURRENT_TAG=`git describe --exact-match --abbrev=0 --tags`

# Exit if any command fails
set -e

# Deploy documentation to GitHub pages
if [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$TRAVIS_BRANCH" == "master" -o "$CURRENT_TAG" != "" ] ; then
  REMOTE="https://${GH_TOKEN}@github.com/moezbhatti/qksms"

  # Set configuration for repository and deploy documentation
  git config --global user.name "${GH_NAME}"
  git config --global user.email "${GH_EMAIL}"
  git remote set-url origin $REMOTE
  mkdocs gh-deploy --force
else
  echo "Not deploying docs. On branch $TRAVIS_BRANCH, should be on master. Is this a pull request? $TRAVIS_PULL_REQUEST"
fi
