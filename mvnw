#!/bin/sh
# ----------------------------------------------------------------------------
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#    https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
# ----------------------------------------------------------------------------

# ----------------------------------------------------------------------------
# Apache Maven Wrapper startup batch script, version 3.3.2
# ----------------------------------------------------------------------------

set -euf
[ "${MVNW_VERBOSE-}" != true ] || set -x

# OS specific support.
native_path() { printf %s\\n "$1"; }
case "$(uname)" in
(CYGWIN*|MINGW*|MSYS*) natvie_path() { cygpath --path --windows "$1"; } ;;
esac

# Resolve the directory of this script
MAVEN_PROJECTBASEDIR="${MAVEN_PROJECTBASEDIR:-$(cd -- "$(dirname -- "$0")" && pwd -P)}"
MVNW_WRAPPERJAR="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"
MVNW_PROPERTIESFILE="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.properties"

# Prepare arguments
WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

# Download wrapper jar if missing
if [ ! -s "$MVNW_WRAPPERJAR" ]; then
    mkdir -p "$(dirname "$MVNW_WRAPPERJAR")"
    wrapperUrl="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar"
    
    echo "Downloading Maven Wrapper..."
    
    if command -v wget > /dev/null 2>&1; then
        wget -q -O "$MVNW_WRAPPERJAR" "$wrapperUrl"
    elif command -v curl > /dev/null 2>&1; then
        curl -fsSL -o "$MVNW_WRAPPERJAR" "$wrapperUrl"
    else
        echo "Error: Neither wget nor curl found. Cannot download maven-wrapper.jar"
        exit 1
    fi
fi

# Find java
if [ -n "${JAVA_HOME-}" ]; then
    JAVA_EXE="$JAVA_HOME/bin/java"
    if [ ! -x "$JAVA_EXE" ]; then
        echo "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME"
        exit 1
    fi
else
    JAVA_EXE=java
    if ! command -v java > /dev/null 2>&1; then
        echo "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH."
        exit 1
    fi
fi

# Run Maven
exec "$JAVA_EXE" \
    ${MAVEN_OPTS-} \
    -classpath "$MVNW_WRAPPERJAR" \
    $WRAPPER_LAUNCHER "$@"
