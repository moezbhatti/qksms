#!/bin/bash

# This script was reallocated from squidfunk/mkdocs-material: https://github.com/squidfunk/mkdocs-material/blob/master/.travis.sh
CURRENT_TAG=`git describe --exact-match --abbrev=0 --tags`

# Exit if any command fails
set -e

# Make sure we're in the build dir, because Murphy
cd ${TRAVIS_BUILD_DIR}

# Deploy documentation to GitHub pages
if [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$TRAVIS_BRANCH" == "master" -o "$CURRENT_TAG" != "" ] ; then
  REMOTE="https://${GH_TOKEN}@github.com/moezbhatti/qksms"

  # Let's build the docs
  ./gradlew dokka

  # FIXME: The following command utilizes a theme hack in mkdocs, as mkdocs v0.x does not
  #        recognize files that are not listed in pages. mkdocs v1.0 will include files
  #        that are not listed in pages. Maybe an extension would be suitable?
  ./.travis/mkdocs_populate.sh

  cd docs
  # Set configuration for repository and deploy documentation
  git config --global user.name "${GH_NAME}"
  git config --global user.email "${GH_EMAIL}"
  git remote set-url origin $REMOTE
  mkdocs gh-deploy --force
else
  echo "Not deploying docs. On branch $TRAVIS_BRANCH, should be on master. Is this a pull request? $TRAVIS_PULL_REQUEST"
fi
