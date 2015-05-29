#!/bin/bash

##
# Test that the amplitude jars compile against a real project.
# Tests both a local jar and the gradle SNAPSHOT release. This should be run
# after uploading a new release to sonatype and staging the build on github
# and before publishing the build.
##

set -e

if [ $# -eq 0 ]; then
    echo "./amplitude-test [amplitude version]"
    exit 1
fi

VERSION=$1
BASE_VERSION=`echo $1 | sed -e "s/-SNAPSHOT//"`
DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
JAR="amplitude-android-${BASE_VERSION}-with-dependencies.jar"

cd ${DIR}

wget "https://github.com/amplitude/Amplitude-Android/blob/v${BASE_VERSION}-snapshot/${JAR}?raw=true" -O ${DIR}/app/libs/${JAR}

echo "ext.amplitudeVersion = '${BASE_VERSION}'" > ${DIR}/config.gradle
echo "ext.amplitudeUseLocal = true" >> ${DIR}/config.gradle
./gradlew assembleRelease

echo "ext.amplitudeVersion = '${VERSION}'" > ${DIR}/config.gradle
echo "ext.amplitudeUseLocal = false" >> ${DIR}/config.gradle
./gradlew assembleRelease
