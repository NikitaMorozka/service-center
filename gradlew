#!/bin/sh

##############################################################################
# Gradle start script
##############################################################################

APP_NAME="Gradle"
APP_BASE_NAME=${0##*/}

# Resolve links
APP_HOME=`cd "$(dirname "$0")" && pwd`

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

# Determine Java
if [ -n "$JAVA_HOME" ] ; then
  JAVACMD="$JAVA_HOME/bin/java"
else
  JAVACMD="java"
fi

exec "$JAVACMD" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
