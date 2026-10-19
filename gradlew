#!/usr/bin/env sh

##############################################################################
##
##  Gradle start up script for UN*X
##
##############################################################################

# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
DEFAULT_JVM_OPTS=""

APP_NAME=Gradle
APP_BASE_NAME=`basename "$0"`

# Resolve links - $0 may be a link
PRG="$0"
while [ -h "$PRG" ] ; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`"/$link"
  fi
done

# Get standard environment variables
PRG_DIR=`dirname "$PRG"`

# Make it possible to run Gradle from the current directory
[ -n "$GRADLE_HOME" ] && GRADLE_HOME=`cd "$GRADLE_HOME"; pwd`
[ -n "$JAVA_HOME" ] && JAVA_HOME=`cd "$JAVA_HOME"; pwd`

CLASSPATH="$PRG_DIR/gradle/wrapper/gradle-wrapper.jar"

# As a last resort, attempt to find java if $JAVA_HOME is not set
if [ -z "$JAVA_HOME" ] ; then
  if [ -n "`which java 2>/dev/null`" ] ; then
    JAVA_CMD=java
  else
    echo "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH."
    exit 1
  fi
else
  JAVA_CMD="$JAVA_HOME/bin/java"
fi

exec "$JAVA_CMD" $DEFAULT_JVM_OPTS -jar "$CLASSPATH" "$@"
