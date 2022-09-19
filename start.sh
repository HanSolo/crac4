#!/bin/bash

# Output timestamp
echo "$(date +'%H:%M:%S.%3N') Call jar file"

# Output epoch seconds before starting the application
# java -XX:+UnlockDiagnosticVMOptions -Djdk.crac.trace-startup-time=true -XX:+CRTraceStartupTime -XX:CRaCCheckpointTo=/home/hansolo/crac-files -jar build/libs/crac4-17.0.0.jar

# Output compilation log that can be analyzed with JITWatch
# java -XX:CRaCCheckpointTo=/home/hansolo/crac-files -XX:+UnlockDiagnosticVMOptions -XX:+LogCompilation -jar build/libs/crac4-17.0.0.jar

# Check whether crac-files folder is present not empty and if so, restore from checkpoint, otherwise start from jar
if [ -d "crac-files" ]; then
  if [ "$(ls -A crac-files)" ]; then
    echo "Restore from checkpoint"
    java -XX:CRaCRestoreFrom=crac-files
  else
    echo "Standard start from jar file"
    java -XX:CRaCCheckpointTo=crac-files -jar build/libs/crac4-17.0.0.jar
  fi
else
  echo "No crac-files folder found"
  mkdir crac-files
  echo "Standard start from jar file"
  java -XX:CRaCCheckpointTo=crac-files -jar build/libs/crac4-17.0.0.jar
fi

